package com.littlebigsteps.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.littlebigsteps.app.data.repository.ProgressRepository
import com.littlebigsteps.app.data.repository.UserPreferencesRepository

/** Pas de framework DI au stade squelette (voir LittleBigStepsApplication) : les
 *  dépendances sont passées à la main via cette factory. */
class OnboardingViewModelFactory(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val progressRepository: ProgressRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return OnboardingViewModel(userPreferencesRepository, progressRepository) as T
    }
}
