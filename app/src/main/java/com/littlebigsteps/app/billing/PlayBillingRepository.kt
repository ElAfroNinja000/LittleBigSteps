package com.littlebigsteps.app.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.littlebigsteps.app.analytics.AnalyticsTracker
import com.littlebigsteps.app.data.repository.ProgressRepository
import com.littlebigsteps.app.data.repository.UserPreferencesRepository
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ID du produit d'abonnement tel que créé dans Play Console. Un seul niveau
 * premium au MVP (CLAUDE.md §7) — pas de tiers multiples.
 */
private const val PREMIUM_SUBSCRIPTION_ID = "premium_subscription"

class PlayBillingRepository(
    context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val progressRepository: ProgressRepository,
    private val analyticsTracker: AnalyticsTracker,
    private val externalScope: CoroutineScope
) : BillingRepository, PurchasesUpdatedListener {

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    override fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    externalScope.launch { restorePurchases() }
                }
            }

            // Pas de retry/backoff au stade squelette : une reconnexion se
            // fera naturellement au prochain lancement de l'app ou appel explicite.
            override fun onBillingServiceDisconnected() = Unit
        })
    }

    override fun endConnection() {
        billingClient.endConnection()
    }

    override suspend fun queryPremiumProductDetails(): ProductDetails? {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PREMIUM_SUBSCRIPTION_ID)
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()
        return billingClient.queryProductDetails(params).productDetailsList?.firstOrNull()
    }

    override fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails) {
        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offerToken)
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()
        analyticsTracker.trackPremiumPurchaseStarted()
        billingClient.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK || purchases == null) return
        externalScope.launch { purchases.forEach { handlePurchase(it) } }
    }

    override suspend fun restorePurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        val result = billingClient.queryPurchasesAsync(params)
        val hasActivePremium = result.purchasesList.any {
            it.purchaseState == Purchase.PurchaseState.PURCHASED && PREMIUM_SUBSCRIPTION_ID in it.products
        }
        result.purchasesList.forEach { handlePurchase(it) }
        if (!hasActivePremium) relockToFreeMediumOnly()
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (PREMIUM_SUBSCRIPTION_ID !in purchase.products) return
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return

        if (!purchase.isAcknowledged) {
            val ackParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(ackParams)
        }

        // handlePurchase() est aussi appelé par restorePurchases() à chaque
        // lancement d'app pour un abonné déjà premium : ne compter la conversion
        // que si l'app ne le savait pas encore, pour ne pas polluer le funnel
        // avec un "premium_unlocked" à chaque session (CLAUDE.md §8).
        val wasAlreadyPremium = userPreferencesRepository.observePreferences().first()?.isPremium == true
        unlockPremium()
        if (!wasAlreadyPremium) analyticsTracker.trackPremiumUnlocked()
    }

    private suspend fun unlockPremium() {
        userPreferencesRepository.setPremium(true)
        progressRepository.ensureMediumRowsExist(unlockedMediums = MediumType.entries.toSet())
    }

    /** Reverrouille sur le seul médium gratuit choisi à l'onboarding — appelé
     *  quand aucun abonnement actif n'est trouvé (jamais acheté, ou expiré). */
    private suspend fun relockToFreeMediumOnly() {
        userPreferencesRepository.setPremium(false)
        val freeMedium = userPreferencesRepository.observePreferences().first()?.freeMedium ?: return
        progressRepository.ensureMediumRowsExist(unlockedMediums = setOf(freeMedium))
    }
}
