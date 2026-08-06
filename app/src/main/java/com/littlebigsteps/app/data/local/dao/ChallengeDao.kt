package com.littlebigsteps.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.littlebigsteps.app.data.local.entity.ChallengeEntity
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {

    /** Insère/remplace en masse lors d'une synchro de contenu (voir ContentManifestEntity). */
    @Upsert
    suspend fun upsertAll(challenges: List<ChallengeEntity>)

    @Query("SELECT * FROM challenges WHERE mediumType = :mediumType ORDER BY id")
    fun observeByMedium(mediumType: MediumType): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE id = :id")
    suspend fun getById(id: String): ChallengeEntity?

    @Query("DELETE FROM challenges WHERE mediumType = :mediumType")
    suspend fun deleteByMedium(mediumType: MediumType)
}
