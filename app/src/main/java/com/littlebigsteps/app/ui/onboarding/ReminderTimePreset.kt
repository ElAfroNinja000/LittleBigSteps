package com.littlebigsteps.app.ui.onboarding

import kotlinx.datetime.LocalTime

/**
 * Simplifie le choix de l'heure de rappel à 3 créneaux plutôt qu'un sélecteur
 * libre, cohérent avec la promesse d'un onboarding en moins d'une minute
 * (CLAUDE.md §3.1).
 */
enum class ReminderTimePreset(val label: String, val time: LocalTime) {
    MORNING("Matin (9h)", LocalTime(9, 0)),
    AFTERNOON("Après-midi (14h)", LocalTime(14, 0)),
    EVENING("Soir (19h)", LocalTime(19, 0))
}
