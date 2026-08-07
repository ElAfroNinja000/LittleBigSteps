package com.littlebigsteps.app.billing

import android.app.Activity
import com.android.billingclient.api.ProductDetails

/**
 * Abonnement premium via Google Play Billing Library directement — un seul
 * store à gérer (CLAUDE.md §7, §10). Le prix et l'offre viennent entièrement
 * du Play Console, jamais codés en dur ici.
 */
interface BillingRepository {
    /** À appeler tôt (voir LittleBigStepsApplication.onCreate) pour restaurer
     *  un abonnement existant dès le lancement, avant toute interaction. */
    fun startConnection()
    fun endConnection()

    suspend fun queryPremiumProductDetails(): ProductDetails?
    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails)

    /** Resynchronise l'état premium local avec Play Billing (débloque ou
     *  reverrouille selon qu'un abonnement actif existe ou non). */
    suspend fun restorePurchases()
}
