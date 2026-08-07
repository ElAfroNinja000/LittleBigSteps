package com.littlebigsteps.app.ui.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.littlebigsteps.app.analytics.AnalyticsTracker
import com.littlebigsteps.app.data.media.SouvenirPhotoStore
import com.littlebigsteps.app.data.repository.ChallengeRepository
import com.littlebigsteps.app.data.repository.ProgressRepository
import com.littlebigsteps.app.data.repository.UserPreferencesRepository

class ChallengeSelectionViewModelFactory(
    private val challengeRepository: ChallengeRepository,
    private val progressRepository: ProgressRepository,
    private val souvenirPhotoStore: SouvenirPhotoStore,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val analyticsTracker: AnalyticsTracker
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return ChallengeSelectionViewModel(
            challengeRepository,
            progressRepository,
            souvenirPhotoStore,
            userPreferencesRepository,
            analyticsTracker
        ) as T
    }
}
