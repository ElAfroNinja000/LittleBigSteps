package com.littlebigsteps.app.data.repository

import androidx.room.withTransaction
import com.littlebigsteps.app.data.local.AppDatabase
import com.littlebigsteps.app.data.local.entity.ChallengeEntity
import com.littlebigsteps.app.data.local.entity.CompletedChallengeEntity
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

/**
 * Catalogue de défis (cache local du contenu JSON, voir ContentSyncRepository)
 * + historique de complétion (portfolio). L'app propose toujours 2-3 défis à la
 * fois (CLAUDE.md §3) : [pickDailyOptions] encapsule cette règle plutôt que de
 * la laisser au ViewModel.
 */
interface ChallengeRepository {

    fun observeChallenges(mediumType: MediumType): Flow<List<ChallengeEntity>>

    /** Tire `count` défis au hasard dans le médium (2-3 par défaut, CLAUDE.md §3). */
    suspend fun pickDailyOptions(mediumType: MediumType, count: Int = 3): List<ChallengeEntity>

    fun observePortfolio(): Flow<List<CompletedChallengeEntity>>
    fun observePortfolio(mediumType: MediumType): Flow<List<CompletedChallengeEntity>>

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
    private val progressRepository: ProgressRepository
) : ChallengeRepository {

    private val challengeDao get() = database.challengeDao()
    private val completedChallengeDao get() = database.completedChallengeDao()

    override fun observeChallenges(mediumType: MediumType): Flow<List<ChallengeEntity>> =
        challengeDao.observeByMedium(mediumType)

    override suspend fun pickDailyOptions(mediumType: MediumType, count: Int): List<ChallengeEntity> =
        challengeDao.getAllByMedium(mediumType).shuffled().take(count)

    override fun observePortfolio(): Flow<List<CompletedChallengeEntity>> =
        completedChallengeDao.observeAll()

    override fun observePortfolio(mediumType: MediumType): Flow<List<CompletedChallengeEntity>> =
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
