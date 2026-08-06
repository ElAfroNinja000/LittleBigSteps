package com.littlebigsteps.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.littlebigsteps.app.domain.model.MediumType

/**
 * Progression XP/niveau pour un médium donné. Une ligne par MediumType, créée
 * pour les 4 médiums au premier lancement (isUnlocked = true seulement pour le
 * médium gratuit choisi à l'onboarding, ou tous si isPremium).
 */
@Entity(tableName = "medium_progress")
data class MediumProgressEntity(
    @PrimaryKey val mediumType: MediumType,
    val xp: Int = 0,
    val level: Int = 1,
    val challengesCompletedCount: Int = 0,
    val isUnlocked: Boolean = false
)
