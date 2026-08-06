package com.littlebigsteps.app.data.remote.dto

import kotlinx.serialization.Serializable

/** Miroir d'un fichier de contenu par médium, ex: content/drawing.json. */
@Serializable
data class MediumContentDto(
    val mediumId: String,
    val challenges: List<ChallengeDto>
)

@Serializable
data class ChallengeDto(
    val id: String,
    val title: String,
    val description: String,
    val estimatedMinutes: Int,
    val level: String,
    val isPremiumOnly: Boolean = false,
    val tags: List<String>? = null
)
