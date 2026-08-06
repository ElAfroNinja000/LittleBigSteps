package com.littlebigsteps.app.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Miroir de manifest.json (voir /content à la racine du repo). Référence chaque
 * fichier médium et sa version, pour ne retélécharger que ce qui a changé.
 */
@Serializable
data class ManifestDto(
    val version: String,
    val mediums: List<MediumManifestEntryDto>
)

@Serializable
data class MediumManifestEntryDto(
    val id: String,
    val file: String,
    val version: String
)
