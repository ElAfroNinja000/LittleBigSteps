package com.littlebigsteps.app.data.repository

import androidx.room.withTransaction
import com.littlebigsteps.app.data.local.AppDatabase
import com.littlebigsteps.app.data.local.entity.ChallengeEntity
import com.littlebigsteps.app.data.local.entity.ChallengePackEntity
import com.littlebigsteps.app.data.local.entity.CompletedChallengeEntity
import com.littlebigsteps.app.data.local.entity.PortfolioEntryEntity
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
     *  entier, il n'est pas mélangé aux suggestions aléatoires du jour). */
    suspend fun pickDailyOptions(mediumType: MediumType, count: Int = 3): List<ChallengeEntity>

    /** Packs thématiques/saisonniers disponibles pour ce médium (CLAUDE.md §7). */
    suspend fun packsForMedium(mediumType: MediumType): List<ChallengePackEntity>

    /** Tous les défis d'un pack donné, dans l'ordre — pas de tirage aléatoire. */
    suspend fun challengesInPack(packId: String): List<ChallengeEntity>

    /** Vue chronologique pour le portfolio, titre du défi inclus (CLAUDE.md §4). */
    fun observePortfolio(): Flow<List<PortfolioEntryEntity>>
    fun observePortfolio(mediumType: MediumType): Flow<List<PortfolioEntryEntity>>

    /**
     * Marque un défi terminé, enregistre le souvenir optionnel (non vérifié,
     * CLAUDE.md §3) et met à jour XP/streak dans la même transaction.
     */
    suspend fun completeChallenge(
        challengeId: String,
        mediumType: MediumType,
        souvenirPhotoPath: String? = null,
        souvenirNote: String? = null
    ): CompletedChallengeEntity
}

class ChallengeRepositoryImpl(
    private val database: AppDatabase,
    private val progressRepository: ProgressRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ChallengeRepository {

    private val challengeDao get() = database.challengeDao()
    private val completedChallengeDao get() = database.completedChallengeDao()

    override fun observeChallenges(mediumType: MediumType): Flow<List<ChallengeEntity>> =
        challengeDao.observeByMedium(mediumType)

    override suspend fun pickDailyOptions(mediumType: MediumType, count: Int): List<ChallengeEntity> {
        val isPremium = userPreferencesRepository.observePreferences().first()?.isPremium ?: false
        return challengeDao.getAllByMedium(mediumType)
            .filter { (!it.isPremiumOnly || isPremium) && it.packId == null }
            .shuffled()
            .take(count)
    }

    override suspend fun packsForMedium(mediumType: MediumType): List<ChallengePackEntity> =
        database.challengePackDao().getByMedium(mediumType)

    override suspend fun challengesInPack(packId: String): List<ChallengeEntity> =
        challengeDao.getByPackId(packId)

    override fun observePortfolio(): Flow<List<PortfolioEntryEntity>> =
        completedChallengeDao.observeAll()

    override fun observePortfolio(mediumType: MediumType): Flow<List<PortfolioEntryEntity>> =
        completedChallengeDao.observeByMedium(mediumType)

    override suspend fun completeChallenge(
        challengeId: String,
        mediumType: MediumType,
        souvenirPhotoPath: String?,
        souvenirNote: String?
    ): CompletedChallengeEntity = database.withTransaction {
        val xpGained = progressRepository.recordCompletion(mediumType)
        val completion = CompletedChallengeEntity(
            challengeId = challengeId,
            mediumType = mediumType,
            completedAt = Clock.System.now(),
            souvenirPhotoPath = souvenirPhotoPath,
            souvenirNote = souvenirNote,
            xpEarned = xpGained
        )
        val id = completedChallengeDao.insert(completion)
        completion.copy(id = id)
    }
}
