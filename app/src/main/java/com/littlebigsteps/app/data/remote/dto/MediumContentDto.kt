package com.littlebigsteps.app.data.remote.dto

import kotlinx.serialization.Serializable

/** Miroir d'un fichier de contenu par médium, ex: content/fr/drawing.json. */
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
    val tags: List<String>? = null,
    /** 3 conseils facultatifs pour aider à démarrer ce défi précis (popup
     *  "Conseils" accessible depuis l'activité en cours). Absent/vide tant que
     *  le contenu éditorial n'a pas été rédigé — voir ChallengeEntity.tips. */
    val tips: List<String>? = null,
    /** Null si ce défi appartient au catalogue de base plutôt qu'à un pack
     *  thématique/saisonnier (voir PacksDto, CLAUDE.md §7). */
    val packId: String? = null
)
