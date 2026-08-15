package com.littlebigsteps.app.ui.progress

import com.littlebigsteps.app.data.local.entity.MediumProgressEntity

data class ProgressUiState(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalChallengesCompleted: Int = 0,
    /** Activités complétées dans le mois calendaire courant — métrique
     *  cumulative qui ne redescend jamais en cours de mois, contrairement au
     *  streak (voir ProgressScreen). */
    val activitiesThisMonth: Int = 0,
    /** XP cumulé tous médiums confondus. */
    val totalXp: Int = 0,
    val mediumProgress: List<MediumProgressEntity> = emptyList(),
    val isPremium: Boolean = false,
    val isLoading: Boolean = true
)
