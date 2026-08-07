package com.littlebigsteps.app.ui.premium

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlebigsteps.app.billing.BillingRepository
import com.littlebigsteps.app.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Écran de déblocage premium (CLAUDE.md §7) : affiche l'offre récupérée via
 * Play Billing et déclenche l'achat. L'état isPremium reflète
 * UserPreferencesRepository, mis à jour par BillingRepository lui-même
 * (achat confirmé ou restauration) — ce ViewModel ne fait qu'observer.
 */
class PremiumViewModel(
    private val billingRepository: BillingRepository,
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.observePreferences().collect { prefs ->
                _uiState.value = _uiState.value.copy(isPremium = prefs?.isPremium ?: false)
            }
        }
        viewModelScope.launch {
            val details = billingRepository.queryPremiumProductDetails()
            val price = details?.subscriptionOfferDetails
                ?.firstOrNull()
                ?.pricingPhases
                ?.pricingPhaseList
                ?.firstOrNull()
                ?.formattedPrice
            _uiState.value = _uiState.value.copy(productDetails = details, priceLabel = price, isLoading = false)
        }
    }

    fun purchase(activity: Activity) {
        val details = _uiState.value.productDetails ?: return
        billingRepository.launchPurchaseFlow(activity, details)
    }
}
