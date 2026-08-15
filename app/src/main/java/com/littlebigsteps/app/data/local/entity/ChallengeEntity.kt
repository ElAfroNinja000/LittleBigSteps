package com.littlebigsteps.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.littlebigsteps.app.domain.model.ChallengeLevel
import com.littlebigsteps.app.domain.model.MediumType

/**
 * Cache local d'un défi défini dans le contenu JSON distant (voir docs/data-model.md
 * et data/remote/dto). L'id vient tel quel du JSON (ex: "drawing_001"), jamais généré
 * côté app, pour rester stable d'une synchro à l'autre.
 */
@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey val id: String,
    val mediumType: MediumType,
    val title: String,
    val description: String,
    val estimatedMinutes: Int,
    val level: ChallengeLevel,
    val isPremiumOnly: Boolean,
    val tags: List<String>? = null,
    /** 3 conseils facultatifs pour aider à démarrer ce défi précis, éditoriaux
     *  comme le reste du catalogue (voir CLAUDE.md §5). Null/vide tant que non
     *  rédigés : le bouton "Conseils" (popup activité en cours) reste alors
     *  masqué plutôt que d'afficher une popup vide. */
    val tips: List<String>? = null,
    /** Null si le défi appartient au catalogue de base plutôt qu'à un pack
     *  thématique/saisonnier (voir ChallengePackEntity, CLAUDE.md §7). */
    val packId: String? = null
)
