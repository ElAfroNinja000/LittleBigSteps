package com.littlebigsteps.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.littlebigsteps.app.data.repository.ChallengeRepository
import com.littlebigsteps.app.data.repository.ProgressRepository
import com.littlebigsteps.app.data.repository.UserPreferencesRepository
import com.littlebigsteps.app.notification.NotificationScheduler

class SettingsViewModelFactory(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val progressRepository: ProgressRepository,
    private val challengeRepository: ChallengeRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return SettingsViewModel(
            userPreferencesRepository,
            progressRepository,
            challengeRepository,
            notificationScheduler
        ) as T
    }
}
