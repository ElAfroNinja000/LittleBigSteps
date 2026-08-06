package com.littlebigsteps.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.datetime.Instant

/**
 * Une ligne par complétion de défi. Un même défi peut être refait plusieurs fois
 * (le catalogue est volontairement limité, ~30-50 défis/médium) : pas de contrainte
 * d'unicité sur challengeId.
 *
 * Le souvenir est entièrement optionnel, non vérifié, et photo + note peuvent
 * coexister librement (aucune des deux ne dépend de l'autre).
 *
 * onDelete = SET_NULL : si un défi disparaît du catalogue lors d'une synchro de
 * contenu, l'historique de complétion (XP, streak, souvenir) reste intact.
 */
@Entity(
    tableName = "completed_challenges",
    foreignKeys = [
        ForeignKey(
            entity = ChallengeEntity::class,
            parentColumns = ["id"],
            childColumns = ["challengeId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("challengeId"), Index("mediumType"), Index("completedAt")]
)
data class CompletedChallengeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val challengeId: String?,
    val mediumType: MediumType, // dénormalisé pour filtrer le portfolio sans jointure
    val completedAt: Instant,
    val souvenirPhotoPath: String? = null,
    val souvenirNote: String? = null,
    val xpEarned: Int
)
