package com.littlebigsteps.app.ui.challenge

import com.littlebigsteps.app.data.local.entity.ChallengeEntity
import com.littlebigsteps.app.data.local.entity.ChallengePackEntity
import com.littlebigsteps.app.data.local.entity.CompletedChallengeEntity
import com.littlebigsteps.app.data.media.SouvenirPhotoTarget
import com.littlebigsteps.app.domain.model.MediumType

data class ChallengeSelectionUiState(
    /** Médium actif pour cette session de sélection. */
    val mediumType: MediumType? = null,
    /** Médiums débloqués parmi lesquels basculer (généralement 1 seul hors premium). */
    val availableMediums: List<MediumType> = emptyList(),
    val isPremium: Boolean = false,
    /** Les 2-3 défis proposés (CLAUDE.md §3.2), ou le contenu complet du pack
     *  actif si [activePack] n'est pas null. */
    val options: List<ChallengeEntity> = emptyList(),
    /** Packs thématiques/saisonniers disponibles pour le médium actif (CLAUDE.md §7). */
    val availablePacks: List<ChallengePackEntity> = emptyList(),
    /** Non-null pendant qu'on parcourt un pack plutôt que les suggestions du jour. */
    val activePack: ChallengePackEntity? = null,
    val selectedChallenge: ChallengeEntity? = null,
    val souvenirNote: String = "",
    /** Chemin local (stockage interne) de la photo souvenir une fois confirmée. */
    val souvenirPhotoPath: String? = null,
    /** Fichier en attente pendant qu'une prise de photo caméra est en cours. */
    val pendingCameraTarget: SouvenirPhotoTarget? = null,
    val isLoading: Boolean = true,
    val isCompleting: Boolean = false,
    /** Non-null juste après une complétion, pour afficher le récap XP avant de continuer. */
    val lastCompletion: CompletedChallengeEntity? = null
)
