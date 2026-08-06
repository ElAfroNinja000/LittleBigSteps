package com.littlebigsteps.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.littlebigsteps.app.domain.model.MediumType

/**
 * Dernière version de contenu synchronisée pour un médium donné. Comparée à la
 * version annoncée dans manifest.json pour ne retélécharger que ce qui a changé
 * (voir ContentSyncRepository et CLAUDE.md §10).
 */
@Entity(tableName = "medium_content_versions")
data class MediumContentVersionEntity(
    @PrimaryKey val mediumType: MediumType,
    val syncedVersion: String
)
