package com.littlebigsteps.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

/**
 * Suivi de la dernière synchro du contenu JSON distant (manifest.json), pour ne
 * retélécharger que les fichiers médium dont la version a changé.
 */
@Entity(tableName = "content_manifest")
data class ContentManifestEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val contentVersion: String,
    /** Même rôle que MediumContentVersionEntity.syncedLocale : les packs sont
     *  eux aussi servis par sous-dossier de langue, à version identique. */
    val syncedLocale: String,
    val lastSyncAt: Instant
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}
