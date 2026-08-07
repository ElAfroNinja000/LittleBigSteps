package com.littlebigsteps.app.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlebigsteps.app.data.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Streak global + XP/niveau par médium (CLAUDE.md §4), simple lecture réactive
 * de ProgressRepository : aucune logique de calcul ici, c'est déjà fait côté
 * repository/GamificationRules au moment de la complétion.
 */
class ProgressViewModel(
    progressRepository: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                progressRepository.observeGlobalProgress(),
                progressRepository.observeAllMediumProgress()
            ) { global, mediums ->
                ProgressUiState(
                    currentStreak = global?.currentStreak ?: 0,
                    longestStreak = global?.longestStreak ?: 0,
                    totalChallengesCompleted = global?.totalChallengesCompleted ?: 0,
                    mediumProgress = mediums.sortedBy { it.mediumType.ordinal },
                    isLoading = false
                )
            }.collect { _uiState.value = it }
        }
    }
}
