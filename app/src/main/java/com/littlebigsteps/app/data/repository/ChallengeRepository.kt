package com.littlebigsteps.app.data.repository

import androidx.room.withTransaction
import com.littlebigsteps.app.data.local.AppDatabase
import com.littlebigsteps.app.data.local.entity.ChallengeEntity
import com.littlebigsteps.app.data.local.entity.ChallengePackEntity
import com.littlebigsteps.app.data.local.entity.ChallengeProgressEntity
import com.littlebigsteps.app.data.local.entity.CompletedChallengeEntity
import com.littlebigsteps.app.data.local.entity.InProgressChallengeEntity
import com.littlebigsteps.app.data.local.entity.PortfolioEntryEntity
import com.littlebigsteps.app.domain.GamificationRules
import com.littlebigsteps.app.domain.RenewalSchedule
import com.littlebigsteps.app.domain.model.ChallengeStatus
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock

/**
 * Catalogue de défis (cache local du contenu JSON, voir ContentSyncRepository)
 * + historique de complétion (portfolio). L'app propose toujours 2-3 défis à la
 * fois (CLAUDE.md §3) : [pickDailyOptions] encapsule cette règle plutôt que de
 * la laisser au ViewModel.
 */
interface ChallengeRepository {

    fun observeChallenges(mediumType: MediumType): Flow<List<ChallengeEntity>>

    /** Tire `count` défis au hasard dans le catalogue de base du médium (2-3
     *  par défaut, CLAUDE.md §3). Exclut les défis isPremiumOnly tant que
     *  l'utilisateur n'est pas premium, et les défis appartenant à un pack
     *  (voir [packsForMedium]/[challengesInPack] — un pack se parcourt en
     *  entier, il n'est pas mélangé aux suggestions aléatoires du jour).
     *  Renvoie une liste vide (aucune nouvelle activité proposée) tant qu'une
     *  activité de ce médium est "en cours" (voir [observeInProgress]), sauf
     *  si le calendrier de renouvellement (fréquence choisie à l'onboarding,
     *  voir [com.littlebigsteps.app.domain.RenewalSchedule]) est écoulé
     *  depuis le démarrage de la dernière activité en cours — c'est la seule
     *  façon d'accumuler plusieurs activités en cours à la fois. Dès qu'une
     *  activité en cours est finalisée, 3 nouvelles sont proposées
     *  immédiatement, sans attendre ce calendrier. */
    suspend fun pickDailyOptions(mediumType: MediumType, count: Int = 3): List<ChallengeEntity>

    /** Packs thématiques/saisonniers disponibles pour ce médium (CLAUDE.md §7). */
    suspend fun packsForMedium(mediumType: MediumType): List<ChallengePackEntity>

    /** Tous les défis d'un pack donné, dans l'ordre — pas de tirage aléatoire. */
    suspend fun challengesInPack(packId: String): List<ChallengeEntity>

    /** Activités "En cours" pour ce médium (choisies mais pas encore finalisées). */
    fun observeInProgress(mediumType: MediumType): Flow<List<InProgressChallengeEntity>>

    /** Démarre une activité : "Choisir" sur une nouvelle proposition la fait
     *  passer directement en "En cours", statut [ChallengeStatus.DRAFT].
     *  [isSurprise] persiste le tirage "surprise" occasionnel décidé côté
     *  ViewModel (voir ChallengeSelectionViewModel), pour bonus XP à la
     *  finalisation même si l'app est relancée entre-temps. */
    suspend fun startChallenge(challengeId: String, mediumType: MediumType, isSurprise: Boolean = false)

    /** Avance/recule la jauge d'une activité en cours (auto-déclaré, tap direct
     *  sur la jauge — CLAUDE.md §9, aucune vérification). No-op si l'activité
     *  n'a pas été démarrée via [startChallenge]. */
    suspend fun updateChallengeStatus(challengeId: String, status: ChallengeStatus)

    /** Vue chronologique pour le portfolio, titre du défi inclus (CLAUDE.md §4). */
    fun observePortfolio(): Flow<List<PortfolioEntryEntity>>
    fun observePortfolio(mediumType: MediumType): Flow<List<PortfolioEntryEntity>>

    /**
     * Finalise une activité en cours ("Finaliser" dans la popup "Bien joué !") :
     * enregistre le souvenir optionnel (non vérifié, CLAUDE.md §3), met à jour
     * XP/streak et retire la ligne "en cours" — tout dans la même transaction.
     */
    suspend fun completeChallenge(
        challengeId: String,
        mediumType: MediumType,
        souvenirPhotoPath: String? = null,
        souvenirNote: String? = null
    ): ChallengeCompletionResult

    /** Réinitialisation manuelle depuis les Paramètres : vide le Portfolio et
     *  abandonne les activités en cours. Ne supprime pas les fichiers photo
     *  eux-mêmes (voir CompletedChallengeDao.deleteAll). Appelé avec
     *  ProgressRepository.resetProgress depuis SettingsViewModel. */
    suspend fun clearHistory()
}

/** [leveledUp]/[newLevel] portent la montée de niveau éventuelle du médium
 *  concerné (voir CompletionOutcome) — popup dédiée côté ChallengeSelectionViewModel. */
data class ChallengeCompletionResult(
    val completion: CompletedChallengeEntity,
    val newLevel: Int,
    val leveledUp: Boolean
)

class ChallengeRepositoryImpl(
    private val database: AppDatabase,
    private val progressRepository: ProgressRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ChallengeRepository {

    private val challengeDao get() = database.challengeDao()
    private val completedChallengeDao get() = database.completedChallengeDao()
    private val challengeProgressDao get() = database.challengeProgressDao()

    override fun observeChallenges(mediumType: MediumType): Flow<List<ChallengeEntity>> =
        challengeDao.observeByMedium(mediumType)

    override suspend fun pickDailyOptions(mediumType: MediumType, count: Int): List<ChallengeEntity> {
        val inProgressIds = challengeProgressDao.getInProgressIds(mediumType)
        if (inProgressIds.isNotEmpty()) {
            val lastStartedAt = challengeProgressDao.getMostRecentStartedAt(mediumType)
            val prefs = userPreferencesRepository.observePreferences().first()
            val renewalDue = lastStartedAt != null &&
                RenewalSchedule.isRenewalDue(lastStartedAt, prefs?.reminderFrequency?.timesPerWeek ?: 7)
            if (!renewalDue) return emptyList()
        }
        val isPremium = userPreferencesRepository.observePreferences().first()?.isPremium ?: false
        return challengeDao.getAllByMedium(mediumType)
            .filter { (!it.isPremiumOnly || isPremium) && it.packId == null && it.id !in inProgressIds }
            .shuffled()
            .take(count)
    }

    override suspend fun packsForMedium(mediumType: MediumType): List<ChallengePackEntity> =
        database.challengePackDao().getByMedium(mediumType)

    override suspend fun challengesInPack(packId: String): List<ChallengeEntity> =
        challengeDao.getByPackId(packId)

    override fun observeInProgress(mediumType: MediumType): Flow<List<InProgressChallengeEntity>> =
        challengeProgressDao.observeByMedium(mediumType)

    override suspend fun startChallenge(challengeId: String, mediumType: MediumType, isSurprise: Boolean) {
        challengeProgressDao.upsert(
            ChallengeProgressEntity(
                challengeId = challengeId,
                mediumType = mediumType,
                status = ChallengeStatus.DRAFT,
                startedAt = Clock.System.now(),
                isSurprise = isSurprise
            )
        )
    }

    override suspend fun updateChallengeStatus(challengeId: String, status: ChallengeStatus) {
        val current = challengeProgressDao.getByChallengeId(challengeId) ?: return
        challengeProgressDao.upsert(current.copy(status = status))
    }

    override fun observePortfolio(): Flow<List<PortfolioEntryEntity>> =
        completedChallengeDao.observeAll()

    override fun observePortfolio(mediumType: MediumType): Flow<List<PortfolioEntryEntity>> =
        completedChallengeDao.observeByMedium(mediumType)

    override suspend fun completeChallenge(
        challengeId: String,
        mediumType: MediumType,
        souvenirPhotoPath: String?,
        souvenirNote: String?
    ): ChallengeCompletionResult = database.withTransaction {
        // isSurprise lu avant la suppression de la ligne "en cours" ci-dessous.
        val isSurprise = challengeProgressDao.getByChallengeId(challengeId)?.isSurprise ?: false
        val xpBonus = if (isSurprise) GamificationRules.SURPRISE_XP_BONUS else 0
        val outcome = progressRepository.recordCompletion(mediumType, xpBonus)
        val completion = CompletedChallengeEntity(
            challengeId = challengeId,
            mediumType = mediumType,
            completedAt = Clock.System.now(),
            souvenirPhotoPath = souvenirPhotoPath,
            souvenirNote = souvenirNote,
            xpEarned = outcome.xpGained
        )
        val id = completedChallengeDao.insert(completion)
        challengeProgressDao.deleteByChallengeId(challengeId)
        ChallengeCompletionResult(
            completion = completion.copy(id = id),
            newLevel = outcome.newLevel,
            leveledUp = outcome.leveledUp
        )
    }

    override suspend fun clearHistory() = database.withTransaction {
        completedChallengeDao.deleteAll()
        challengeProgressDao.deleteAll()
    }
}
