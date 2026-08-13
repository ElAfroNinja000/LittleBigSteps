package com.littlebigsteps.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.littlebigsteps.app.data.local.entity.ChallengeProgressEntity
import com.littlebigsteps.app.data.local.entity.InProgressChallengeEntity
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeProgressDao {

    @Upsert
    suspend fun upsert(progress: ChallengeProgressEntity)

    /** Activités "En cours" pour ce médium, la plus récemment démarrée en premier. */
    @Query(
        "SELECT c.*, p.status AS status, p.startedAt AS startedAt FROM challenge_progress p " +
            "INNER JOIN challenges c ON c.id = p.challengeId " +
            "WHERE p.mediumType = :mediumType " +
            "ORDER BY p.startedAt DESC"
    )
    fun observeByMedium(mediumType: MediumType): Flow<List<InProgressChallengeEntity>>

    /** Ids déjà en cours pour ce médium — exclus des nouvelles propositions
     *  (voir ChallengeRepositoryImpl.pickDailyOptions). */
    @Query("SELECT challengeId FROM challenge_progress WHERE mediumType = :mediumType")
    suspend fun getInProgressIds(mediumType: MediumType): List<String>

    @Query("SELECT * FROM challenge_progress WHERE challengeId = :challengeId")
    suspend fun getByChallengeId(challengeId: String): ChallengeProgressEntity?

    /** Supprime la ligne "en cours" une fois l'activité finalisée (ou abandonnée). */
    @Query("DELETE FROM challenge_progress WHERE challengeId = :challengeId")
    suspend fun deleteByChallengeId(challengeId: String)
}
