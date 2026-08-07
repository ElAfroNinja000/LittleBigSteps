package com.littlebigsteps.app.fakes

import com.littlebigsteps.app.analytics.AnalyticsTracker
import com.littlebigsteps.app.domain.model.Frequency
import com.littlebigsteps.app.domain.model.MediumType

/** Enregistre les événements localement au lieu de les envoyer à PostHog — sert
 *  aussi à vérifier qu'un événement attendu a bien été déclenché (CLAUDE.md §11). */
class FakeAnalyticsTracker : AnalyticsTracker {

    val events = mutableListOf<String>()

    override fun trackOnboardingCompleted(isMultiMedium: Boolean, mediumCount: Int, frequency: Frequency) {
        events += "onboarding_completed"
    }

    override fun trackChallengeCompleted(mediumType: MediumType, hasSouvenir: Boolean, currentStreak: Int) {
        events += "challenge_completed"
    }

    override fun trackPremiumPurchaseStarted() {
        events += "premium_purchase_started"
    }

    override fun trackPremiumUnlocked() {
        events += "premium_unlocked"
    }

    override fun trackExport(format: String) {
        events += "progress_exported:$format"
    }
}
