package com.littlebigsteps.app.domain.model

/**
 * Cosmétiques exclusifs premium (CLAUDE.md §7) : aucune fonctionnalité
 * débloquée, juste une reconnaissance visuelle de jalons de progression.
 * Évalués uniquement pour les utilisateurs premium, voir
 * domain/BadgeEvaluator.kt et ProgressRepositoryImpl.
 */
enum class Badge {
    STREAK_7,
    STREAK_30,
    TEN_COMPLETIONS,
    FIFTY_COMPLETIONS,
    LEVEL_5_ANY_MEDIUM
}
