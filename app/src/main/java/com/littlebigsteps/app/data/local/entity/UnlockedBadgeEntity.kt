package com.littlebigsteps.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.littlebigsteps.app.domain.model.Badge
import kotlinx.datetime.Instant

/** Cosmétique premium débloqué de façon permanente une fois mérité (CLAUDE.md
 *  §7) : jamais supprimé, même si la progression qui l'a déclenché redescend
 *  ensuite (ex: streak rompu après avoir atteint STREAK_7). */
@Entity(tableName = "unlocked_badges")
data class UnlockedBadgeEntity(
    @PrimaryKey val badge: Badge,
    val unlockedAt: Instant
)
