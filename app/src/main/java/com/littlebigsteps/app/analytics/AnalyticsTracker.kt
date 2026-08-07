package com.littlebigsteps.app.analytics

import com.littlebigsteps.app.domain.model.Frequency
import com.littlebigsteps.app.domain.model.MediumType

/**
 * Suivi anonyme des métriques clés (CLAUDE.md §8). Aucune PII : pas de compte
 * dans l'app, l'ID reste celui géré automatiquement par le SDK (device-scoped,
 * jamais lié à une identité). Rétention J1/J7/J30 et activation (premier défi
 * sous 48h) se lisent directement dans PostHog à partir des événements de
 * cycle de vie automatiques ($app_opened...) combinés à onboarding_completed /
 * challenge_completed ci-dessous — pas besoin d'appel dédié pour ça.
 */
interface AnalyticsTracker {
    fun trackOnboardingCompleted(isMultiMedium: Boolean, mediumCount: Int, frequency: Frequency)

    /** hasSouvenir alimente le proxy "croissance du portfolio" (CLAUDE.md §8). */
    fun trackChallengeCompleted(mediumType: MediumType, hasSouvenir: Boolean, currentStreak: Int)

    fun trackPremiumPurchaseStarted()
    fun trackPremiumUnlocked()
    fun trackExport(format: String)
}
