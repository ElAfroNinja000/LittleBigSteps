package com.littlebigsteps.app.ui.onboarding

import com.littlebigsteps.app.domain.model.Frequency
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.datetime.LocalTime

/** Les 4-5 écrans du parcours d'onboarding (CLAUDE.md §3.1).
 *  Pas d'étape "mono vs multi médium" : le choix des médiums (MEDIUM_CHOICE)
 *  est toujours à sélection multiple, un seul démarre débloqué gratuitement
 *  quel que soit le nombre choisi (voir CLAUDE.md §7) — demander le mode à
 *  part n'apportait aucune décision supplémentaire. */
enum class OnboardingStep {
    WELCOME,
    MEDIUM_CHOICE,
    FREE_MEDIUM_CHOICE,
    FREQUENCY_CHOICE,
    // Deux cadrans successifs (heure puis minutes) plutôt qu'un seul écran —
    // voir REMINDER_MINUTE_CHOICE, dernière étape avant DONE.
    REMINDER_HOUR_CHOICE,
    REMINDER_MINUTE_CHOICE,
    DONE
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val selectedMediums: Set<MediumType> = emptySet(),
    val freeMedium: MediumType? = null,
    // La roue de sélection a toujours une valeur affichée (jamais "vide") —
    // 7 fois/semaine par défaut, ajustable, cohérent avec ce type de contrôle.
    val frequency: Frequency = Frequency(7),
    // Idem : la roue heure/minute affiche toujours une valeur, 9h00 par défaut.
    val reminderTime: LocalTime = LocalTime(9, 0),
    val isSaving: Boolean = false,
    val isComplete: Boolean = false
) {
    /** Peut passer à l'étape suivante avec la sélection actuelle de cette étape. */
    val canContinue: Boolean
        get() = when (step) {
            OnboardingStep.WELCOME -> true
            OnboardingStep.MEDIUM_CHOICE -> selectedMediums.isNotEmpty()
            OnboardingStep.FREE_MEDIUM_CHOICE -> freeMedium != null
            OnboardingStep.FREQUENCY_CHOICE -> true // la roue affiche toujours une valeur valide
            OnboardingStep.REMINDER_HOUR_CHOICE -> true // le cadran affiche toujours une valeur valide
            OnboardingStep.REMINDER_MINUTE_CHOICE -> true // le cadran affiche toujours une valeur valide
            OnboardingStep.DONE -> true
        }
}
