package com.littlebigsteps.app.domain.model

/**
 * Niveau de difficulté d'un défi. Seul BEGINNER est utilisé au lancement
 * (voir CLAUDE.md §5) ; les autres valeurs préparent une évolution future.
 */
enum class ChallengeLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}
