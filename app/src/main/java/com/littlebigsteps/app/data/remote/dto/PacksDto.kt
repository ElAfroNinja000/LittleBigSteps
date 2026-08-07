package com.littlebigsteps.app.data.remote.dto

import kotlinx.serialization.Serializable

/** Miroir de content/packs.json — les packs thématiques/saisonniers premium
 *  (CLAUDE.md §7), synchronisés à part du catalogue de base par médium. */
@Serializable
data class PacksDto(
    val version: String,
    val packs: List<PackDto>
)

@Serializable
data class PackDto(
    val id: String,
    val mediumId: String,
    val title: String,
    val description: String,
    val isPremiumOnly: Boolean = true
)
