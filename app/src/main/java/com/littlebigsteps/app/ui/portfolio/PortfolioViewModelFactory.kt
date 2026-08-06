package com.littlebigsteps.app.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.littlebigsteps.app.data.repository.ChallengeRepository

class PortfolioViewModelFactory(
    private val challengeRepository: ChallengeRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return PortfolioViewModel(challengeRepository) as T
    }
}
