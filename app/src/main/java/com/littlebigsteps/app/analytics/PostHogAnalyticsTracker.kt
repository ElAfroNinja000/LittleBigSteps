package com.littlebigsteps.app.analytics

import android.content.Context
import com.littlebigsteps.app.domain.model.Frequency
import com.littlebigsteps.app.domain.model.MediumType
import com.posthog.PostHog
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig

class PostHogAnalyticsTracker(context: Context) : AnalyticsTracker {

    init {
        val config = PostHogAndroidConfig(
            apiKey = AnalyticsConfig.API_KEY,
            host = AnalyticsConfig.HOST
        ).apply {
            // $app_opened/$app_backgrounded automatiques -> base de la rétention
            // J1/J7/J30 (CLAUDE.md §8), pas besoin d'event dédié pour ça.
            captureApplicationLifecycleEvents = true
            // Rien qui ressemble à de la capture d'écran/session : suivi
            // exclusivement événementiel, cohérent avec l'absence de compte.
            sessionReplay = false
        }
        PostHogAndroid.setup(context, config)
    }

    override fun trackOnboardingCompleted(isMultiMedium: Boolean, mediumCount: Int, frequency: Frequency) {
        PostHog.capture(
            event = "onboarding_completed",
            properties = mapOf(
                "is_multi_medium" to isMultiMedium,
                "medium_count" to mediumCount,
                "reminder_frequency" to frequency.timesPerWeek
            )
        )
    }

    override fun trackChallengeCompleted(mediumType: MediumType, hasSouvenir: Boolean, currentStreak: Int) {
        PostHog.capture(
            event = "challenge_completed",
            properties = mapOf(
                "medium" to mediumType.name,
                "has_souvenir" to hasSouvenir,
                "current_streak" to currentStreak
            )
        )
    }

    override fun trackPremiumPurchaseStarted() {
        PostHog.capture(event = "premium_purchase_started")
    }

    override fun trackPremiumUnlocked() {
        PostHog.capture(event = "premium_unlocked")
    }

    override fun trackExport(format: String) {
        PostHog.capture(event = "progress_exported", properties = mapOf("format" to format))
    }
}
