package com.littlebigsteps.app.ui.progress

import com.littlebigsteps.app.data.local.entity.MediumProgressEntity
import com.littlebigsteps.app.domain.model.Badge

data class ProgressUiState(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalChallengesCompleted: Int = 0,
    val mediumProgress: List<MediumProgressEntity> = emptyList(),
    /** Cosmétiques exclusifs premium (CLAUDE.md §7) — vide tant que non premium. */
    val unlockedBadges: Set<Badge> = emptySet(),
    val isPremium: Boolean = false,
    val isLoading: Boolean = true
)
