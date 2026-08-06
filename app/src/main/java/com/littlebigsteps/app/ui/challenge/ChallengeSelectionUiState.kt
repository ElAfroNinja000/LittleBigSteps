package com.littlebigsteps.app.ui.challenge

import com.littlebigsteps.app.data.local.entity.ChallengeEntity
import com.littlebigsteps.app.data.local.entity.CompletedChallengeEntity
import com.littlebigsteps.app.domain.model.MediumType

data class ChallengeSelectionUiState(
    /** Médium actif pour cette session de sélection. */
    val mediumType: MediumType? = null,
    /** Médiums débloqués parmi lesquels basculer (généralement 1 seul hors premium). */
    val availableMediums: List<MediumType> = emptyList(),
    /** Les 2-3 défis proposés (CLAUDE.md §3.2). */
    val options: List<ChallengeEntity> = emptyList(),
    val selectedChallenge: ChallengeEntity? = null,
    val souvenirNote: String = "",
    val isLoading: Boolean = true,
    val isCompleting: Boolean = false,
    /** Non-null juste après une complétion, pour afficher le récap XP avant de continuer. */
    val lastCompletion: CompletedChallengeEntity? = null
)
