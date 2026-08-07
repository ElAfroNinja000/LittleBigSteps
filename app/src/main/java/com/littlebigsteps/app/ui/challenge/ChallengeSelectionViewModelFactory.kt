package com.littlebigsteps.app.ui.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.littlebigsteps.app.data.media.SouvenirPhotoStore
import com.littlebigsteps.app.data.repository.ChallengeRepository
import com.littlebigsteps.app.data.repository.ProgressRepository

class ChallengeSelectionViewModelFactory(
    private val challengeRepository: ChallengeRepository,
    private val progressRepository: ProgressRepository,
    private val souvenirPhotoStore: SouvenirPhotoStore
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return ChallengeSelectionViewModel(challengeRepository, progressRepository, souvenirPhotoStore) as T
    }
}
