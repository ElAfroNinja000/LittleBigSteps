package com.littlebigsteps.app.data.local.entity

import androidx.room.Embedded

/**
 * Projection de requête (pas une table) : une complétion + le titre/la
 * description du défi associé, résolus par jointure avec `challenges` (voir
 * CompletedChallengeDao). Tous deux null si le défi a depuis disparu du
 * catalogue — l'historique de complétion survit quand même (voir
 * CompletedChallengeEntity, onDelete SET_NULL).
 */
data class PortfolioEntryEntity(
    @Embedded val completion: CompletedChallengeEntity,
    val challengeTitle: String?,
    val challengeDescription: String?
)
