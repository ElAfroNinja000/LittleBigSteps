package com.littlebigsteps.app.ui.onboarding

import com.littlebigsteps.app.domain.model.Frequency
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.datetime.LocalTime

/** Les 4-5 écrans du parcours d'onboarding (CLAUDE.md §3.1). */
enum class OnboardingStep {
    MODE_CHOICE,
    MEDIUM_CHOICE,
    FREE_MEDIUM_CHOICE,
    FREQUENCY_CHOICE,
    REMINDER_TIME_CHOICE,
    DONE
}

enum class OnboardingMode { MONO, MULTI }

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.MODE_CHOICE,
    val mode: OnboardingMode? = null,
    val selectedMediums: Set<MediumType> = emptySet(),
    val freeMedium: MediumType? = null,
    val frequency: Frequency? = null,
    val reminderTime: LocalTime? = null,
    val isSaving: Boolean = false,
    val isComplete: Boolean = false
) {
    /** Peut passer à l'étape suivante avec la sélection actuelle de cette étape. */
    val canContinue: Boolean
        get() = when (step) {
            OnboardingStep.MODE_CHOICE -> mode != null
            OnboardingStep.MEDIUM_CHOICE -> selectedMediums.isNotEmpty()
            OnboardingStep.FREE_MEDIUM_CHOICE -> freeMedium != null
            OnboardingStep.FREQUENCY_CHOICE -> frequency != null
            OnboardingStep.REMINDER_TIME_CHOICE -> reminderTime != null
            OnboardingStep.DONE -> true
        }
}
