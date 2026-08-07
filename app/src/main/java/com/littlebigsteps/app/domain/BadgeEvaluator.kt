package com.littlebigsteps.app.domain

import com.littlebigsteps.app.data.local.entity.GlobalProgressEntity
import com.littlebigsteps.app.data.local.entity.MediumProgressEntity
import com.littlebigsteps.app.domain.model.Badge

/**
 * Détermine quels badges sont mérités à un instant donné, à partir de la
 * progression actuelle. Les seuils sont volontairement simples pour le MVP —
 * à retoucher ici uniquement si l'équilibrage change (CLAUDE.md §7).
 */
object BadgeEvaluator {
    fun evaluate(global: GlobalProgressEntity, mediums: List<MediumProgressEntity>): Set<Badge> {
        val earned = mutableSetOf<Badge>()
        if (global.currentStreak >= 7) earned += Badge.STREAK_7
        if (global.currentStreak >= 30) earned += Badge.STREAK_30
        if (global.totalChallengesCompleted >= 10) earned += Badge.TEN_COMPLETIONS
        if (global.totalChallengesCompleted >= 50) earned += Badge.FIFTY_COMPLETIONS
        if (mediums.any { it.level >= 5 }) earned += Badge.LEVEL_5_ANY_MEDIUM
        return earned
    }
}
