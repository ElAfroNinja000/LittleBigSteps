package com.littlebigsteps.app.ui.challenge

import com.littlebigsteps.app.data.local.entity.ChallengeEntity
import com.littlebigsteps.app.data.local.entity.ChallengePackEntity
import com.littlebigsteps.app.data.local.entity.CompletedChallengeEntity
import com.littlebigsteps.app.data.local.entity.InProgressChallengeEntity
import com.littlebigsteps.app.data.media.SouvenirPhotoTarget
import com.littlebigsteps.app.domain.model.MediumType

/** Au plus une popup ouverte à la fois — un seul champ scellé plutôt que
 *  plusieurs booléens/nullables qui pourraient se contredire. */
sealed interface ChallengeDialog {
    /** Nouvelle activité pas encore choisie : description + Retour/Choisir. */
    data class NewChallenge(val challenge: ChallengeEntity) : ChallengeDialog

    /** Activité déjà en cours : description + jauge Brouillon/En cours/Terminé. */
    data class InProgress(val entry: InProgressChallengeEntity) : ChallengeDialog

    /** "Bien joué !" — photo/légende avant de finaliser (jauge à Terminé). */
    data class Finalize(val entry: InProgressChallengeEntity) : ChallengeDialog

    /** Conseils spécifiques au défi, accessibles depuis la popup "En cours"
     *  (icône ampoule, seulement si challenge.tips n'est pas vide). Fermer
     *  cette popup revient à InProgress plutôt qu'à null (voir
     *  ChallengeSelectionViewModel.closeTips) : c'est un niveau de détail
     *  supplémentaire, pas un nouveau parcours. */
    data class Tips(val entry: InProgressChallengeEntity) : ChallengeDialog
}

/** Montée de niveau détectée à la finalisation — popup dédiée (confettis),
 *  affichée à la place du snackbar "+N XP" habituel pour cette complétion. */
data class LevelUpEvent(val mediumType: MediumType, val newLevel: Int)

data class ChallengeSelectionUiState(
    /** Médium actif pour cette session de sélection. */
    val mediumType: MediumType? = null,
    /** Médiums débloqués parmi lesquels basculer (généralement 1 seul hors premium). */
    val availableMediums: List<MediumType> = emptyList(),
    val isPremium: Boolean = false,
    /** Les 2-3 nouvelles propositions du jour (CLAUDE.md §3.2), ou le contenu
     *  complet du pack actif si [activePack] n'est pas null. Exclut les
     *  activités déjà "en cours" (voir [inProgress]). */
    val newOptions: List<ChallengeEntity> = emptyList(),
    /** Activités choisies mais pas encore finalisées, section "En cours". */
    val inProgress: List<InProgressChallengeEntity> = emptyList(),
    /** Packs thématiques/saisonniers disponibles pour le médium actif (CLAUDE.md §7). */
    val availablePacks: List<ChallengePackEntity> = emptyList(),
    /** Non-null pendant qu'on parcourt un pack plutôt que les suggestions du jour. */
    val activePack: ChallengePackEntity? = null,
    /** Id du défi "surprise" occasionnel de ce tirage parmi [newOptions] (bonus
     *  XP, mise en valeur visuelle) — jamais au sein d'un pack, voir loadOptions. */
    val surpriseChallengeId: String? = null,
    val dialog: ChallengeDialog? = null,
    val souvenirNote: String = "",
    /** Chemin local (stockage interne) de la photo souvenir une fois confirmée. */
    val souvenirPhotoPath: String? = null,
    /** Fichier en attente pendant qu'une prise de photo caméra est en cours. */
    val pendingCameraTarget: SouvenirPhotoTarget? = null,
    val isLoading: Boolean = true,
    val isCompleting: Boolean = false,
    /** Non-null juste après une finalisation, pour afficher le récap XP avant
     *  de continuer — jamais en même temps que [lastLevelUp] (la montée de
     *  niveau remplace le snackbar XP pour cette complétion, pas de doublon). */
    val lastCompletion: CompletedChallengeEntity? = null,
    /** Non-null juste après une finalisation qui fait monter de niveau. */
    val lastLevelUp: LevelUpEvent? = null
)
