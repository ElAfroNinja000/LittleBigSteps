package com.littlebigsteps.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.littlebigsteps.app.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferencesDao {

    @Upsert
    suspend fun upsert(preferences: UserPreferencesEntity)

    @Query("SELECT * FROM user_preferences WHERE id = ${UserPreferencesEntity.SINGLETON_ID}")
    fun observe(): Flow<UserPreferencesEntity?>
}
