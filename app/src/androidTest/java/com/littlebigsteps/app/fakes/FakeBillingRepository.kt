package com.littlebigsteps.app.fakes

import android.app.Activity
import com.android.billingclient.api.ProductDetails
import com.littlebigsteps.app.billing.BillingRepository

/** Pas de vraie Play Billing en test instrumenté : aucune offre disponible,
 *  l'écran Premium doit se dégrader proprement (voir PremiumScreen). */
class FakeBillingRepository : BillingRepository {
    var purchaseFlowLaunchCount = 0
        private set

    override fun startConnection() = Unit
    override fun endConnection() = Unit
    override suspend fun queryPremiumProductDetails(): ProductDetails? = null

    override fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails) {
        purchaseFlowLaunchCount++
    }

    override suspend fun restorePurchases() = Unit
}
