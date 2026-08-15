package com.littlebigsteps.app.data.repository

import androidx.room.withTransaction
import com.littlebigsteps.app.data.local.AppDatabase
import com.littlebigsteps.app.data.local.entity.GlobalProgressEntity
import com.littlebigsteps.app.data.local.entity.MediumProgressEntity
import com.littlebigsteps.app.domain.GamificationRules
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

/**
 * Streak global + XP/niveau par médium. Les deux évoluent ensemble à chaque
 * complétion ([recordCompletion]) mais restent des concepts distincts : le
 * streak est transversal à tous les médiums, l'XP est propre à chacun (voir
 * CLAUDE.md §4 et docs/data-model.md).
 */
interface ProgressRepository {
    fun observeGlobalProgress(): Flow<GlobalProgressEntity?>
    fun observeMediumProgress(mediumType: MediumType): Flow<MediumProgressEntity?>
    fun observeAllMediumProgress(): Flow<List<MediumProgressEntity>>

    /** Crée/actualise les lignes des 4 médiums (idempotent), ex. après l'onboarding
     *  ou un changement de statut premium qui débloque de nouveaux médiums. */
    suspend fun ensureMediumRowsExist(unlockedMediums: Set<MediumType>)

    /** Met à jour XP/niveau du médium et le streak global pour une complétion.
     *  [xpBonus] s'ajoute au barème standard (défi "surprise", voir
     *  GamificationRules.SURPRISE_XP_BONUS). Renvoie l'XP gagné et si le
     *  niveau du médium vient de changer (utile pour la popup de montée de
     *  niveau, voir ChallengeSelectionViewModel). */
    suspend fun recordCompletion(mediumType: MediumType, xpBonus: Int = 0): CompletionOutcome

    /** Réinitialisation manuelle depuis les Paramètres : remet streak/XP/niveaux
     *  à zéro sans toucher aux médiums débloqués (statut premium indépendant de
     *  la progression). Le Portfolio est vidé séparément, voir
     *  ChallengeRepository.clearHistory — les deux sont appelés ensemble
     *  depuis SettingsViewModel. */
    suspend fun resetProgress()
}

/** [newLevel] est le niveau du médium après la complétion, quel que soit
 *  [leveledUp] — pratique pour l'afficher même si on ne s'en sert que quand
 *  [leveledUp] est vrai. */
data class CompletionOutcome(val xpGained: Int, val newLevel: Int, val leveledUp: Boolean)

class ProgressRepositoryImpl(
    private val database: AppDatabase
) : ProgressRepository {

    private val globalProgressDao get() = database.globalProgressDao()
    private val mediumProgressDao get() = database.mediumProgressDao()

    override fun observeGlobalProgress(): Flow<GlobalProgressEntity?> = globalProgressDao.observe()

    override fun observeMediumProgress(mediumType: MediumType): Flow<MediumProgressEntity?> =
        mediumProgressDao.observe(mediumType)

    override fun observeAllMediumProgress(): Flow<List<MediumProgressEntity>> =
        mediumProgressDao.observeAll()

    override suspend fun ensureMediumRowsExist(unlockedMediums: Set<MediumType>) {
        MediumType.entries.forEach { medium ->
            val existing = mediumProgressDao.getOnce(medium)
            mediumProgressDao.upsert(
                (existing ?: MediumProgressEntity(mediumType = medium))
                    .copy(isUnlocked = medium in unlockedMediums)
            )
        }
    }

    override suspend fun recordCompletion(mediumType: MediumType, xpBonus: Int): CompletionOutcome = database.withTransaction {
        val xpGained = GamificationRules.XP_PER_COMPLETION + xpBonus
        val current = mediumProgressDao.getOnce(mediumType)
            ?: MediumProgressEntity(mediumType = mediumType, isUnlocked = true)
        val newXp = current.xp + xpGained
        val newLevel = GamificationRules.levelForXp(newXp)
        mediumProgressDao.upsert(
            current.copy(
                xp = newXp,
                level = newLevel,
                challengesCompletedCount = current.challengesCompletedCount + 1
            )
        )
        updateStreak()
        CompletionOutcome(xpGained = xpGained, newLevel = newLevel, leveledUp = newLevel > current.level)
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

    override suspend fun resetProgress() = database.withTransaction {
        globalProgressDao.deleteAll()
        mediumProgressDao.resetAll()
    }
}
