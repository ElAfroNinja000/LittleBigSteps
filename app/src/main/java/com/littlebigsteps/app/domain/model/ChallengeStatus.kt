package com.littlebigsteps.app.domain.model

/**
 * Statut d'une activité "En cours" (voir ChallengeProgressEntity). Progression
 * volontairement auto-déclarée par l'utilisateur (tap sur la jauge) — aucune
 * vérification, cohérent avec le reste de l'app (CLAUDE.md §9).
 */
enum class ChallengeStatus {
    DRAFT,
    IN_PROGRESS,
    DONE
}
