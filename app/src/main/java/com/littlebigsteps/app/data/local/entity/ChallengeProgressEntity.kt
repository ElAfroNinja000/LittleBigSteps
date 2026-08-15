package com.littlebigsteps.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.littlebigsteps.app.domain.model.ChallengeStatus
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.datetime.Instant

/**
 * Une activité "En cours" (choisie mais pas encore finalisée) — au plus une
 * ligne par défi (challengeId en clé primaire) : on ne peut pas démarrer deux
 * fois la même activité sans d'abord la finaliser ou l'abandonner.
 *
 * onDelete = CASCADE : si le défi disparaît du catalogue lors d'une synchro,
 * une activité en cours dessus n'a plus de sens.
 */
@Entity(
    tableName = "challenge_progress",
    foreignKeys = [
        ForeignKey(
            entity = ChallengeEntity::class,
            parentColumns = ["id"],
            childColumns = ["challengeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("mediumType")]
)
data class ChallengeProgressEntity(
    @PrimaryKey val challengeId: String,
    val mediumType: MediumType, // dénormalisé pour filtrer sans jointure
    val status: ChallengeStatus,
    val startedAt: Instant,
    /** Défi "surprise" occasionnel de ce tirage (bonus XP à la finalisation,
     *  voir GamificationRules.SURPRISE_XP_BONUS) — décidé côté ViewModel au
     *  moment du tirage, persisté ici pour survivre à une reprise d'app. */
    val isSurprise: Boolean = false
)
