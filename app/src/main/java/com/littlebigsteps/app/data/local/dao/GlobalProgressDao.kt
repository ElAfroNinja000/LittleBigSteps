package com.littlebigsteps.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.littlebigsteps.app.data.local.entity.GlobalProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GlobalProgressDao {

    @Upsert
    suspend fun upsert(progress: GlobalProgressEntity)

    @Query("SELECT * FROM global_progress WHERE id = ${GlobalProgressEntity.SINGLETON_ID}")
    fun observe(): Flow<GlobalProgressEntity?>

    /** Lecture ponctuelle utilisée par ProgressRepositoryImpl pour calculer le streak. */
    @Query("SELECT * FROM global_progress WHERE id = ${GlobalProgressEntity.SINGLETON_ID}")
    suspend fun getOnce(): GlobalProgressEntity?
}
