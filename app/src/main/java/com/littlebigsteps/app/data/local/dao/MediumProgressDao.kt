package com.littlebigsteps.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.littlebigsteps.app.data.local.entity.MediumProgressEntity
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.coroutines.flow.Flow

@Dao
interface MediumProgressDao {

    @Upsert
    suspend fun upsert(progress: MediumProgressEntity)

    @Query("SELECT * FROM medium_progress WHERE mediumType = :mediumType")
    fun observe(mediumType: MediumType): Flow<MediumProgressEntity?>

    @Query("SELECT * FROM medium_progress")
    fun observeAll(): Flow<List<MediumProgressEntity>>

    /** Incrément atomique lors d'une complétion ; le niveau se dérive de xp en couche service. */
    @Query(
        "UPDATE medium_progress SET xp = xp + :xpGained, " +
            "challengesCompletedCount = challengesCompletedCount + 1 " +
            "WHERE mediumType = :mediumType"
    )
    suspend fun addCompletion(mediumType: MediumType, xpGained: Int)
}
