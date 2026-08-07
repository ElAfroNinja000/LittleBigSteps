package com.littlebigsteps.app.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.littlebigsteps.app.data.repository.ProgressRepository

class ProgressViewModelFactory(
    private val progressRepository: ProgressRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return ProgressViewModel(progressRepository) as T
    }
}
