package com.littlebigsteps.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.littlebigsteps.app.data.local.entity.CompletedChallengeEntity
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletedChallengeDao {

    @Insert
    suspend fun insert(completedChallenge: CompletedChallengeEntity): Long

    /** Portfolio complet, le plus récent en premier. */
    @Query("SELECT * FROM completed_challenges ORDER BY completedAt DESC")
    fun observeAll(): Flow<List<CompletedChallengeEntity>>

    @Query("SELECT * FROM completed_challenges WHERE mediumType = :mediumType ORDER BY completedAt DESC")
    fun observeByMedium(mediumType: MediumType): Flow<List<CompletedChallengeEntity>>

    @Query(
        "SELECT * FROM completed_challenges " +
            "WHERE souvenirPhotoPath IS NOT NULL OR souvenirNote IS NOT NULL " +
            "ORDER BY completedAt DESC"
    )
    fun observeWithSouvenirs(): Flow<List<CompletedChallengeEntity>>
}
