package com.littlebigsteps.app.ui.settings

import com.littlebigsteps.app.domain.model.Frequency
import kotlinx.datetime.LocalTime

/** Au plus une popup ouverte à la fois, comme ChallengeDialog (voir
 *  ui/challenge/ChallengeSelectionUiState.kt). */
sealed interface SettingsDialog {
    data object Frequency : SettingsDialog
    data object ReminderTime : SettingsDialog
    data object Language : SettingsDialog
    data object ConfirmReset : SettingsDialog
}

data class SettingsUiState(
    val frequency: Frequency = Frequency(7),
    val reminderTime: LocalTime = LocalTime(9, 0),
    val notificationsEnabled: Boolean = true,
    val isPremium: Boolean = false,
    val isLoading: Boolean = true,
    val dialog: SettingsDialog? = null
)
