package com.littlebigsteps.app.analytics

/**
 * Configuration PostHog (auto-hébergé recommandé, CLAUDE.md §10). Placeholders
 * à remplacer une fois l'instance déployée — sans clé valide, le SDK n'envoie
 * simplement rien (pas de crash au démarrage).
 */
object AnalyticsConfig {
    const val API_KEY = "TODO-posthog-project-api-key"
    const val HOST = "https://TODO-posthog-host.example.com"
}
