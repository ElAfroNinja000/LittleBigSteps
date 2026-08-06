package com.littlebigsteps.app.domain.model

/**
 * Fréquence de rappel choisie à l'onboarding, utilisée pour programmer
 * les notifications locales (WorkManager).
 */
enum class Frequency {
    DAILY,
    FEW_TIMES_WEEK,
    WEEKLY
}
