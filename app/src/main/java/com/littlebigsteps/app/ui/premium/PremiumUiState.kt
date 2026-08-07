package com.littlebigsteps.app.ui.premium

import com.android.billingclient.api.ProductDetails

data class PremiumUiState(
    val isPremium: Boolean = false,
    val productDetails: ProductDetails? = null,
    val priceLabel: String? = null,
    val isLoading: Boolean = true
)
