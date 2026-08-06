package com.littlebigsteps.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

/**
 * Streak global de régularité, tous médiums confondus (singleton, id fixé).
 * Pas de pénalité : un oubli remet juste currentStreak à 0, sans message
 * culpabilisant (voir CLAUDE.md §4).
 */
@Entity(tableName = "global_progress")
data class GlobalProgressEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletionDate: LocalDate? = null,
    val totalChallengesCompleted: Int = 0
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}
