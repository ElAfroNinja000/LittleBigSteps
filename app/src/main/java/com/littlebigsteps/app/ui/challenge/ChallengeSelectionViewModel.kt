package com.littlebigsteps.app.ui.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlebigsteps.app.data.local.entity.ChallengeEntity
import com.littlebigsteps.app.data.repository.ChallengeRepository
import com.littlebigsteps.app.data.repository.ProgressRepository
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Étapes "Découverte du défi" et "Réalisation & complétion" du parcours
 * (CLAUDE.md §3.2-3.3) : propose toujours 2-3 défis, laisse en choisir un,
 * puis le marquer terminé avec un souvenir optionnel et non vérifié.
 */
class ChallengeSelectionViewModel(
    private val challengeRepository: ChallengeRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengeSelectionUiState())
    val uiState: StateFlow<ChallengeSelectionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val unlocked = progressRepository.observeAllMediumProgress().first()
                .filter { it.isUnlocked }
                .map { it.mediumType }
            val defaultMedium = unlocked.firstOrNull()
            _uiState.value = _uiState.value.copy(availableMediums = unlocked, mediumType = defaultMedium)
            if (defaultMedium != null) loadOptions(defaultMedium) else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun selectMedium(medium: MediumType) {
        _uiState.value = _uiState.value.copy(mediumType = medium, selectedChallenge = null, souvenirNote = "")
        loadOptions(medium)
    }

    /** Retire un nouveau lot de défis pour le même médium, sans le changer. */
    fun refreshOptions() {
        _uiState.value.mediumType?.let { loadOptions(it) }
    }

    fun selectChallenge(challenge: ChallengeEntity) {
        _uiState.value = _uiState.value.copy(selectedChallenge = challenge, souvenirNote = "")
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedChallenge = null, souvenirNote = "")
    }

    fun updateSouvenirNote(note: String) {
        _uiState.value = _uiState.value.copy(souvenirNote = note)
    }

    fun completeSelectedChallenge() {
        val state = _uiState.value
        val challenge = state.selectedChallenge ?: return
        val medium = state.mediumType ?: return

        _uiState.value = state.copy(isCompleting = true)
        viewModelScope.launch {
            // souvenirPhotoPath reste null pour l'instant : la capture photo
            // (permissions caméra/galerie, écriture en stockage interne) n'est
            // pas encore branchée.
            val completion = challengeRepository.completeChallenge(
                challengeId = challenge.id,
                mediumType = medium,
                souvenirNote = state.souvenirNote.ifBlank { null }
            )
            _uiState.value = _uiState.value.copy(
                isCompleting = false,
                lastCompletion = completion,
                selectedChallenge = null,
                souvenirNote = ""
            )
        }
    }

    /** Ferme le récap de complétion et retire un nouveau lot de défis pour continuer. */
    fun dismissCompletion() {
        _uiState.value = _uiState.value.copy(lastCompletion = null)
        _uiState.value.mediumType?.let { loadOptions(it) }
    }

    private fun loadOptions(mediumType: MediumType) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val options = challengeRepository.pickDailyOptions(mediumType)
            _uiState.value = _uiState.value.copy(options = options, isLoading = false)
        }
    }
}
