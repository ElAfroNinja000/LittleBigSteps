package com.littlebigsteps.app.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.littlebigsteps.app.data.repository.ChallengeRepository
import com.littlebigsteps.app.data.repository.ProgressRepository
import com.littlebigsteps.app.export.ProgressExportGenerator

class ProgressViewModelFactory(
    private val progressRepository: ProgressRepository,
    private val challengeRepository: ChallengeRepository,
    private val exportGenerator: ProgressExportGenerator
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return ProgressViewModel(progressRepository, challengeRepository, exportGenerator) as T
    }
}
