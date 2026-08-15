package com.littlebigsteps.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.littlebigsteps.app.domain.model.MediumType

/**
 * Dernière version de contenu synchronisée pour un médium donné. Comparée à la
 * version annoncée dans manifest.json pour ne retélécharger que ce qui a changé
 * (voir ContentSyncRepository et CLAUDE.md §10).
 *
 * [syncedLocale] est indispensable en plus de la version : le catalogue est servi
 * par sous-dossier de langue (/content/fr, /content/en) avec les mêmes numéros de
 * version de part et d'autre. Sans elle, changer la langue de l'app laissait la
 * synchro conclure "déjà à jour" et le contenu restait dans la langue précédente.
 */
@Entity(tableName = "medium_content_versions")
data class MediumContentVersionEntity(
    @PrimaryKey val mediumType: MediumType,
    val syncedVersion: String,
    val syncedLocale: String
)
