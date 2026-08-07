package com.littlebigsteps.app.data.repository

import androidx.room.withTransaction
import com.littlebigsteps.app.data.local.AppDatabase
import com.littlebigsteps.app.data.local.entity.GlobalProgressEntity
import com.littlebigsteps.app.data.local.entity.MediumProgressEntity
import com.littlebigsteps.app.data.local.entity.UnlockedBadgeEntity
import com.littlebigsteps.app.domain.BadgeEvaluator
import com.littlebigsteps.app.domain.GamificationRules
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

/**
 * Streak global + XP/niveau par médium. Les deux évoluent ensemble à chaque
 * complétion ([recordCompletion]) mais restent des concepts distincts : le
 * streak est transversal à tous les médiums, l'XP est propre à chacun (voir
 * CLAUDE.md §4 et docs/data-model.md). Évalue aussi les badges premium (§7)
 * à chaque complétion.
 */
interface ProgressRepository {
    fun observeGlobalProgress(): Flow<GlobalProgressEntity?>
    fun observeMediumProgress(mediumType: MediumType): Flow<MediumProgressEntity?>
    fun observeAllMediumProgress(): Flow<List<MediumProgressEntity>>
    fun observeUnlockedBadges(): Flow<List<UnlockedBadgeEntity>>

    /** Crée/actualise les lignes des 4 médiums (idempotent), ex. après l'onboarding
     *  ou un changement de statut premium qui débloque de nouveaux médiums. */
    suspend fun ensureMediumRowsExist(unlockedMediums: Set<MediumType>)

    /** Met à jour XP/niveau du médium et le streak global pour une complétion.
     *  Renvoie l'XP gagné (utile pour l'afficher immédiatement à l'écran). */
    suspend fun recordCompletion(mediumType: MediumType): Int
}

class ProgressRepositoryImpl(
    private val database: AppDatabase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ProgressRepository {

    private val globalProgressDao get() = database.globalProgressDao()
    private val mediumProgressDao get() = database.mediumProgressDao()
    private val badgeDao get() = database.badgeDao()

    override fun observeGlobalProgress(): Flow<GlobalProgressEntity?> = globalProgressDao.observe()

    override fun observeMediumProgress(mediumType: MediumType): Flow<MediumProgressEntity?> =
        mediumProgressDao.observe(mediumType)

    override fun observeAllMediumProgress(): Flow<List<MediumProgressEntity>> =
        mediumProgressDao.observeAll()

    override fun observeUnlockedBadges(): Flow<List<UnlockedBadgeEntity>> = badgeDao.observeAll()

    override suspend fun ensureMediumRowsExist(unlockedMediums: Set<MediumType>) {
        MediumType.entries.forEach { medium ->
            val existing = mediumProgressDao.getOnce(medium)
            mediumProgressDao.upsert(
                (existing ?: MediumProgressEntity(mediumType = medium))
                    .copy(isUnlocked = medium in unlockedMediums)
            )
        }
    }

    override suspend fun recordCompletion(mediumType: MediumType): Int = database.withTransaction {
        val xpGained = GamificationRules.XP_PER_COMPLETION
        val current = mediumProgressDao.getOnce(mediumType)
            ?: MediumProgressEntity(mediumType = mediumType, isUnlocked = true)
        val newXp = current.xp + xpGained
        mediumProgressDao.upsert(
            current.copy(
                xp = newXp,
                level = GamificationRules.levelForXp(newXp),
                challengesCompletedCount = current.challengesCompletedCount + 1
            )
        )
        updateStreak()
        maybeUnlockBadges()
        xpGained
    }

    /**
     * Sans pénalité (CLAUDE.md §4) : un jour manqué remet juste currentStreak à 1
     * au prochain défi, pas de message culpabilisant, pas de blocage.
     */
    private suspend fun updateStreak() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val existing = globalProgressDao.getOnce()

        val updated = when {
            existing == null -> GlobalProgressEntity(
                currentStreak = 1,
                longestStreak = 1,
                lastCompletionDate = today,
                totalChallengesCompleted = 1
            )
            existing.lastCompletionDate == today -> existing.copy(
                totalChallengesCompleted = existing.totalChallengesCompleted + 1
            )
            else -> {
                val yesterday = today.minus(1, DateTimeUnit.DAY)
                val newStreak = if (existing.lastCompletionDate == yesterday) {
                    existing.currentStreak + 1
                } else {
                    1
                }
                existing.copy(
                    currentStreak = newStreak,
                    longestStreak = maxOf(existing.longestStreak, newStreak),
                    lastCompletionDate = today,
                    totalChallengesCompleted = existing.totalChallengesCompleted + 1
                )
            }
        }
        globalProgressDao.upsert(updated)
    }

    /** Cosmétiques exclusifs premium (CLAUDE.md §7) : rien n'est évalué pour
     *  un utilisateur gratuit. Un badge déjà débloqué n'est jamais réévalué
     *  (permanent, voir UnlockedBadgeEntity). */
    private suspend fun maybeUnlockBadges() {
        val isPremium = userPreferencesRepository.observePreferences().first()?.isPremium ?: false
        if (!isPremium) return

        val global = globalProgressDao.getOnce() ?: return
        val mediums = mediumProgressDao.observeAll().first()
        val alreadyUnlocked = badgeDao.getUnlockedBadges().toSet()
        val newlyEarned = BadgeEvaluator.evaluate(global, mediums) - alreadyUnlocked
        val now = Clock.System.now()
        newlyEarned.forEach { badge -> badgeDao.upsert(UnlockedBadgeEntity(badge, now)) }
    }
}
