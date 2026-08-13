package com.littlebigsteps.app.data.local.entity

import androidx.room.Embedded
import com.littlebigsteps.app.domain.model.ChallengeStatus
import kotlinx.datetime.Instant

/**
 * Projection de requête (pas une table) : une activité "En cours" + le défi
 * associé, résolus par jointure avec `challenges` (voir ChallengeProgressDao).
 */
data class InProgressChallengeEntity(
    @Embedded val challenge: ChallengeEntity,
    val status: ChallengeStatus,
    val startedAt: Instant
)
