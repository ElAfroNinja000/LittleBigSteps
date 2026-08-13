package com.littlebigsteps.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlebigsteps.app.BuildConfig
import com.littlebigsteps.app.analytics.AnalyticsTracker
import com.littlebigsteps.app.data.repository.ProgressRepository
import com.littlebigsteps.app.data.repository.UserPreferencesRepository
import com.littlebigsteps.app.domain.model.Frequency
import com.littlebigsteps.app.domain.model.MediumType
import com.littlebigsteps.app.notification.NotificationScheduler
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
    private val progressRepository: ProgressRepository,
    private val notificationScheduler: NotificationScheduler,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    /** Premium : sélection multiple (rien n'empêche d'en cocher plusieurs pour
     *  les voir apparaître verrouillés). Free : sélection unique — un seul
     *  médium sera de toute façon débloqué gratuitement (CLAUDE.md §7), inutile
     *  de faire croire qu'on peut en garder plusieurs. */
    fun toggleMedium(medium: MediumType) {
        val state = _uiState.value
        val newSelection = if (BuildConfig.FORCE_PREMIUM) {
            if (medium in state.selectedMediums) state.selectedMediums - medium
            else state.selectedMediums + medium
        } else {
            setOf(medium)
        }
        _uiState.value = state.copy(
            selectedMediums = newSelection,
            // Un seul médium sélectionné : forcément celui-là le médium gratuit.
            // Plusieurs : on laisse FREE_MEDIUM_CHOICE trancher.
            freeMedium = if (newSelection.size == 1) newSelection.first() else state.freeMedium
        )
    }

    fun selectFreeMedium(medium: MediumType) {
        _uiState.value = _uiState.value.copy(freeMedium = medium)
    }

    fun selectFrequency(timesPerWeek: Int) {
        _uiState.value = _uiState.value.copy(frequency = Frequency(timesPerWeek))
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
        OnboardingStep.WELCOME -> OnboardingStep.MEDIUM_CHOICE
        OnboardingStep.MEDIUM_CHOICE ->
            if (state.selectedMediums.size > 1) {
                OnboardingStep.FREE_MEDIUM_CHOICE
            } else {
                OnboardingStep.FREQUENCY_CHOICE
            }
        OnboardingStep.FREE_MEDIUM_CHOICE -> OnboardingStep.FREQUENCY_CHOICE
        OnboardingStep.FREQUENCY_CHOICE -> OnboardingStep.REMINDER_HOUR_CHOICE
        OnboardingStep.REMINDER_HOUR_CHOICE -> OnboardingStep.REMINDER_MINUTE_CHOICE
        OnboardingStep.REMINDER_MINUTE_CHOICE -> OnboardingStep.DONE
        OnboardingStep.DONE -> OnboardingStep.DONE
    }

    private fun previousStepFor(state: OnboardingUiState): OnboardingStep = when (state.step) {
        OnboardingStep.WELCOME -> OnboardingStep.WELCOME
        OnboardingStep.MEDIUM_CHOICE -> OnboardingStep.MEDIUM_CHOICE
        OnboardingStep.FREE_MEDIUM_CHOICE -> OnboardingStep.MEDIUM_CHOICE
        OnboardingStep.FREQUENCY_CHOICE ->
            if (state.selectedMediums.size > 1) {
                OnboardingStep.FREE_MEDIUM_CHOICE
            } else {
                OnboardingStep.MEDIUM_CHOICE
            }
        OnboardingStep.REMINDER_HOUR_CHOICE -> OnboardingStep.FREQUENCY_CHOICE
        OnboardingStep.REMINDER_MINUTE_CHOICE -> OnboardingStep.REMINDER_HOUR_CHOICE
        OnboardingStep.DONE -> OnboardingStep.REMINDER_MINUTE_CHOICE
    }

    private fun submit() {
        val state = _uiState.value
        val freeMedium = state.freeMedium ?: state.selectedMediums.firstOrNull() ?: return
        val frequency = state.frequency
        val reminderTime = state.reminderTime

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
            notificationScheduler.scheduleReminders(frequency, reminderTime)
            analyticsTracker.trackOnboardingCompleted(
                isMultiMedium = state.selectedMediums.size > 1,
                mediumCount = state.selectedMediums.size,
                frequency = frequency
            )
            _uiState.value = _uiState.value.copy(isSaving = false, isComplete = true)
        }
    }
}
