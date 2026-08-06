package com.littlebigsteps.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.littlebigsteps.app.domain.model.ChallengeLevel
import com.littlebigsteps.app.domain.model.MediumType

/**
 * Cache local d'un défi défini dans le contenu JSON distant (voir docs/data-model.md
 * et data/remote/dto). L'id vient tel quel du JSON (ex: "drawing_001"), jamais généré
 * côté app, pour rester stable d'une synchro à l'autre.
 */
@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey val id: String,
    val mediumType: MediumType,
    val title: String,
    val description: String,
    val estimatedMinutes: Int,
    val level: ChallengeLevel,
    val isPremiumOnly: Boolean,
    val tags: List<String>? = null
)
