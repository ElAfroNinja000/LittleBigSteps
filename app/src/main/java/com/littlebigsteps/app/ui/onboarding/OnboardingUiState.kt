package com.littlebigsteps.app.ui.onboarding

import com.littlebigsteps.app.domain.model.Frequency
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.datetime.LocalTime

/** Les 4 écrans du parcours d'onboarding (CLAUDE.md §3.1).
 *  Pas d'étape "mono vs multi médium" : le choix des médiums (MEDIUM_CHOICE)
 *  est à sélection unique hors premium (un seul médium est de toute façon
 *  débloqué gratuitement, voir CLAUDE.md §7) et à sélection multiple en
 *  premium (tous les médiums sont accessibles) — demander le mode à part
 *  n'apportait aucune décision supplémentaire.
 *
 *  Pas d'étape "quel médium gratuit ?" non plus : elle n'avait de sens dans
 *  aucun des deux cas. Hors premium la sélection est déjà unique, donc le
 *  médium gratuit est connu ; en premium tous les médiums sont débloqués,
 *  donc la question n'a pas d'objet. */
enum class OnboardingStep {
    WELCOME,
    MEDIUM_CHOICE,
    FREQUENCY_CHOICE,
    REMINDER_TIME_CHOICE,
    DONE
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val selectedMediums: Set<MediumType> = emptySet(),
    /** Médium débloqué gratuitement. Hors premium c'est le seul sélectionné ;
     *  en premium tous sont débloqués et celui-ci ne sert que de repli si
     *  l'abonnement s'interrompt (voir PlayBillingRepository.relockToFreeMediumOnly). */
    val freeMedium: MediumType? = null,
    /** Premium déjà actif au moment de l'onboarding (abonnement restauré après
     *  une réinstallation, ou flavor de test) : la sélection des médiums passe
     *  alors en multiple et tous sont débloqués à la fin du parcours. */
    val isPremium: Boolean = false,
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
            OnboardingStep.FREQUENCY_CHOICE -> true // le sélecteur affiche toujours une valeur valide
            OnboardingStep.REMINDER_TIME_CHOICE -> true // les roues affichent toujours une valeur valide
            OnboardingStep.DONE -> true
        }
}
