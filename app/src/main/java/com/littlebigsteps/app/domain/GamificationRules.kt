package com.littlebigsteps.app.domain

/**
 * Règles de progression, centralisées pour rester faciles à retoucher
 * (équilibrage) sans toucher aux repositories. Voir CLAUDE.md §4.
 */
object GamificationRules {

    /** XP gagné par défi complété. Volontairement plat pour le MVP : pas de
     *  barème par niveau de difficulté tant qu'un seul niveau (BEGINNER) existe. */
    const val XP_PER_COMPLETION = 20

    /** Bonus XP du défi "surprise" occasionnel (ChallengeProgressEntity.isSurprise) —
     *  double l'XP de la complétion (voir ChallengeRepositoryImpl.completeChallenge). */
    const val SURPRISE_XP_BONUS = 20

    private const val XP_PER_LEVEL = 100

    /** Niveau 1 à 0 xp, +1 niveau tous les XP_PER_LEVEL xp. */
    fun levelForXp(xp: Int): Int = (xp / XP_PER_LEVEL) + 1

    /** Progression (0f-1f) vers le niveau suivant, pour une barre de progression. */
    fun progressToNextLevel(xp: Int): Float = (xp % XP_PER_LEVEL) / XP_PER_LEVEL.toFloat()
}
