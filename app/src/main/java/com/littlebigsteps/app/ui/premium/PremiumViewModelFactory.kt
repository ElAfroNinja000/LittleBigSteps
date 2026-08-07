package com.littlebigsteps.app.ui.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.littlebigsteps.app.billing.BillingRepository
import com.littlebigsteps.app.data.repository.UserPreferencesRepository

class PremiumViewModelFactory(
    private val billingRepository: BillingRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return PremiumViewModel(billingRepository, userPreferencesRepository) as T
    }
}
