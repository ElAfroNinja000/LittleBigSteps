package com.littlebigsteps.app.ui.common

import com.littlebigsteps.app.domain.model.Badge
import com.littlebigsteps.app.domain.model.ChallengeStatus
import com.littlebigsteps.app.domain.model.Frequency
import com.littlebigsteps.app.domain.model.MediumType

/** Libellés d'affichage FR, centralisés pour que chaque écran ne les redéfinisse pas. */
fun MediumType.label(): String = when (this) {
    MediumType.PHOTO -> "Photographie"
    MediumType.DRAWING -> "Dessin"
    MediumType.WRITING -> "Écriture"
    MediumType.CRAFT -> "Artisanat"
}

fun Frequency.label(): String = when (timesPerWeek) {
    7 -> "Tous les jours"
    1 -> "1 fois par semaine"
    else -> "$timesPerWeek fois par semaine"
}

fun ChallengeStatus.label(): String = when (this) {
    ChallengeStatus.DRAFT -> "Brouillon"
    ChallengeStatus.IN_PROGRESS -> "En cours"
    ChallengeStatus.DONE -> "Terminé"
}

fun Badge.label(): String = when (this) {
    Badge.STREAK_7 -> "Une semaine de suite"
    Badge.STREAK_30 -> "Un mois de suite"
    Badge.TEN_COMPLETIONS -> "Dix défis complétés"
    Badge.FIFTY_COMPLETIONS -> "Cinquante défis complétés"
    Badge.LEVEL_5_ANY_MEDIUM -> "Niveau 5 atteint"
}
