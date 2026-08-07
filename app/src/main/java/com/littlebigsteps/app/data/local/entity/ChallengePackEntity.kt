package com.littlebigsteps.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.littlebigsteps.app.domain.model.MediumType

/**
 * Pack thématique/saisonnier de défis, réservé au premium par défaut
 * (CLAUDE.md §7). Synchronisé depuis packs.json — voir ContentSyncRepository.
 * Les défis qui en font partie référencent cet id via ChallengeEntity.packId.
 */
@Entity(tableName = "challenge_packs")
data class ChallengePackEntity(
    @PrimaryKey val id: String,
    val mediumType: MediumType,
    val title: String,
    val description: String,
    val isPremiumOnly: Boolean = true
)
