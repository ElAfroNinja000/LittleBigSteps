package com.littlebigsteps.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.littlebigsteps.app.data.local.entity.UnlockedBadgeEntity
import com.littlebigsteps.app.domain.model.Badge
import kotlinx.coroutines.flow.Flow

@Dao
interface BadgeDao {

    @Upsert
    suspend fun upsert(badge: UnlockedBadgeEntity)

    @Query("SELECT * FROM unlocked_badges")
    fun observeAll(): Flow<List<UnlockedBadgeEntity>>

    @Query("SELECT badge FROM unlocked_badges")
    suspend fun getUnlockedBadges(): List<Badge>
}
