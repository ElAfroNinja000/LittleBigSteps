package com.littlebigsteps.app.ui.progress

import com.littlebigsteps.app.data.local.entity.MediumProgressEntity

data class ProgressUiState(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalChallengesCompleted: Int = 0,
    val mediumProgress: List<MediumProgressEntity> = emptyList(),
    val isLoading: Boolean = true
)
