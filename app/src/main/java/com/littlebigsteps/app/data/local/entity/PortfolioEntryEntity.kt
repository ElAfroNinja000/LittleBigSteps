package com.littlebigsteps.app.data.local.entity

import androidx.room.Embedded

/**
 * Projection de requête (pas une table) : une complétion + le titre du défi
 * associé, résolu par jointure avec `challenges` (voir CompletedChallengeDao).
 * challengeTitle est null si le défi a depuis disparu du catalogue — l'historique
 * de complétion survit quand même (voir CompletedChallengeEntity, onDelete SET_NULL).
 */
data class PortfolioEntryEntity(
    @Embedded val completion: CompletedChallengeEntity,
    val challengeTitle: String?
)
