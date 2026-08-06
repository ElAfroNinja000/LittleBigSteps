package com.littlebigsteps.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlebigsteps.app.data.repository.ProgressRepository
import com.littlebigsteps.app.data.repository.UserPreferencesRepository
import com.littlebigsteps.app.domain.model.Frequency
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime

/**
 * Pilote le parcours d'onboarding (CLAUDE.md §3.1) : mono vs multi médium,
 * fréquence, heure de rappel. Volontairement court : 3 à 5 écrans max, aucune
 * donnée envoyée nulle part avant [submit] (pas de compte, tout est local).
 */
class OnboardingViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun selectMode(mode: OnboardingMode) {
        val state = _uiState.value
        _uiState.value = state.copy(
            mode = mode,
            // repartir d'une sélection propre si l'utilisateur change d'avis de mode
            selectedMediums = emptySet(),
            freeMedium = null
        )
    }

    fun toggleMedium(medium: MediumType) {
        val state = _uiState.value
        val mode = state.mode ?: return
        val newSelection = when (mode) {
            OnboardingMode.MONO -> setOf(medium) // un seul choix possible
            OnboardingMode.MULTI ->
                if (medium in state.selectedMediums) state.selectedMediums - medium
                else state.selectedMediums + medium
        }
        _uiState.value = state.copy(
            selectedMediums = newSelection,
            freeMedium = if (mode == OnboardingMode.MONO) newSelection.firstOrNull() else state.freeMedium
        )
    }

    fun selectFreeMedium(medium: MediumType) {
        _uiState.value = _uiState.value.copy(freeMedium = medium)
    }

    fun selectFrequency(frequency: Frequency) {
        _uiState.value = _uiState.value.copy(frequency = frequency)
    }

    fun selectReminderTime(time: LocalTime) {
        _uiState.value = _uiState.value.copy(reminderTime = time)
    }

    fun goToPreviousStep() {
        val state = _uiState.value
        _uiState.value = state.copy(step = previousStepFor(state))
    }

    fun goToNextStep() {
        val state = _uiState.value
        if (!state.canContinue) return
        val next = nextStepFor(state)
        _uiState.value = state.copy(step = next)
        if (next == OnboardingStep.DONE) submit()
    }

    /** Saute FREE_MEDIUM_CHOICE si un seul médium a été sélectionné : il est
     *  alors forcément le médium gratuit, pas besoin de le redemander. */
    private fun nextStepFor(state: OnboardingUiState): OnboardingStep = when (state.step) {
        OnboardingStep.MODE_CHOICE -> OnboardingStep.MEDIUM_CHOICE
        OnboardingStep.MEDIUM_CHOICE ->
            if (state.mode == OnboardingMode.MULTI && state.selectedMediums.size > 1) {
                OnboardingStep.FREE_MEDIUM_CHOICE
            } else {
                OnboardingStep.FREQUENCY_CHOICE
            }
        OnboardingStep.FREE_MEDIUM_CHOICE -> OnboardingStep.FREQUENCY_CHOICE
        OnboardingStep.FREQUENCY_CHOICE -> OnboardingStep.REMINDER_TIME_CHOICE
        OnboardingStep.REMINDER_TIME_CHOICE -> OnboardingStep.DONE
        OnboardingStep.DONE -> OnboardingStep.DONE
    }

    private fun previousStepFor(state: OnboardingUiState): OnboardingStep = when (state.step) {
        OnboardingStep.MODE_CHOICE -> OnboardingStep.MODE_CHOICE
        OnboardingStep.MEDIUM_CHOICE -> OnboardingStep.MODE_CHOICE
        OnboardingStep.FREE_MEDIUM_CHOICE -> OnboardingStep.MEDIUM_CHOICE
        OnboardingStep.FREQUENCY_CHOICE ->
            if (state.mode == OnboardingMode.MULTI && state.selectedMediums.size > 1) {
                OnboardingStep.FREE_MEDIUM_CHOICE
            } else {
                OnboardingStep.MEDIUM_CHOICE
            }
        OnboardingStep.REMINDER_TIME_CHOICE -> OnboardingStep.FREQUENCY_CHOICE
        OnboardingStep.DONE -> OnboardingStep.REMINDER_TIME_CHOICE
    }

    private fun submit() {
        val state = _uiState.value
        val freeMedium = state.freeMedium ?: state.selectedMediums.firstOrNull() ?: return
        val frequency = state.frequency ?: return
        val reminderTime = state.reminderTime ?: return

        _uiState.value = state.copy(isSaving = true)
        viewModelScope.launch {
            userPreferencesRepository.completeOnboarding(
                selectedMediums = state.selectedMediums.toList(),
                freeMedium = freeMedium,
                reminderFrequency = frequency,
                reminderTime = reminderTime
            )
            // Seul freeMedium démarre débloqué ; les autres médiums sélectionnés
            // restent visibles mais verrouillés tant qu'il n'y a pas de premium.
            progressRepository.ensureMediumRowsExist(unlockedMediums = setOf(freeMedium))
            _uiState.value = _uiState.value.copy(isSaving = false, isComplete = true)
        }
    }
}
