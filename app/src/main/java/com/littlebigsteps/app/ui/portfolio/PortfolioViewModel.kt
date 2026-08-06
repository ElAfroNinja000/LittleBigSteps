package com.littlebigsteps.app.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlebigsteps.app.data.repository.ChallengeRepository
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/**
 * Vue chronologique des défis complétés, filtrable par médium et par date
 * (CLAUDE.md §4). Le filtrage se fait côté client sur la liste déjà chargée :
 * sans compte ni cloud, le volume par utilisateur reste modeste (un appareil).
 */
class PortfolioViewModel(
    challengeRepository: ChallengeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PortfolioUiState())
    val uiState: StateFlow<PortfolioUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            challengeRepository.observePortfolio().collect { entries ->
                _uiState.value = _uiState.value.copy(allEntries = entries, isLoading = false)
            }
        }
    }

    fun setMediumFilter(medium: MediumType?) {
        _uiState.value = _uiState.value.copy(mediumFilter = medium)
    }

    fun setDateFilter(date: LocalDate?) {
        _uiState.value = _uiState.value.copy(dateFilter = date)
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(mediumFilter = null, dateFilter = null)
    }
}
