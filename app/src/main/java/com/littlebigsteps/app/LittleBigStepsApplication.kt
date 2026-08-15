package com.littlebigsteps.app

import android.app.Application
import android.util.Log
import com.littlebigsteps.app.analytics.AnalyticsTracker
import com.littlebigsteps.app.analytics.PostHogAnalyticsTracker
import com.littlebigsteps.app.billing.BillingRepository
import com.littlebigsteps.app.billing.PlayBillingRepository
import com.littlebigsteps.app.data.local.AppDatabase
import com.littlebigsteps.app.data.local.BundledContentSource
import com.littlebigsteps.app.data.media.InternalSouvenirPhotoStore
import com.littlebigsteps.app.data.media.SouvenirPhotoStore
import com.littlebigsteps.app.data.remote.ContentApiService
import com.littlebigsteps.app.data.remote.NetworkConfig
import com.littlebigsteps.app.data.repository.ChallengeRepository
import com.littlebigsteps.app.data.repository.ChallengeRepositoryImpl
import com.littlebigsteps.app.data.repository.ContentSyncRepository
import com.littlebigsteps.app.data.repository.ContentSyncRepositoryImpl
import com.littlebigsteps.app.data.repository.ProgressRepository
import com.littlebigsteps.app.data.repository.ProgressRepositoryImpl
import com.littlebigsteps.app.data.repository.UserPreferencesRepository
import com.littlebigsteps.app.data.repository.UserPreferencesRepositoryImpl
import com.littlebigsteps.app.export.CanvasProgressExportGenerator
import com.littlebigsteps.app.export.ProgressExportGenerator
import com.littlebigsteps.app.notification.NotificationScheduler
import com.littlebigsteps.app.notification.WorkManagerNotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Point d'accès manuel (service locator léger) aux repositories. Pas de
 * framework DI au stade squelette — à revisiter (Hilt ?) si la complexité le
 * justifie. Les ViewModels récupèrent ces instances via
 * `(application as LittleBigStepsApplication)`.
 */
class LittleBigStepsApplication : Application() {

    // Portée process : la connexion Billing doit survivre au-delà d'un seul
    // écran/ViewModel (PurchasesUpdatedListener reste actif tant que l'app tourne).
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    private val contentApi: ContentApiService by lazy {
        NetworkConfig.buildRetrofit().create(ContentApiService::class.java)
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepositoryImpl(database.userPreferencesDao())
    }

    val progressRepository: ProgressRepository by lazy {
        ProgressRepositoryImpl(database)
    }

    val challengeRepository: ChallengeRepository by lazy {
        ChallengeRepositoryImpl(database, progressRepository, userPreferencesRepository)
    }

    val contentSyncRepository: ContentSyncRepository by lazy {
        ContentSyncRepositoryImpl(contentApi, database, BundledContentSource(this))
    }

    val notificationScheduler: NotificationScheduler by lazy {
        WorkManagerNotificationScheduler(this)
    }

    val progressExportGenerator: ProgressExportGenerator by lazy {
        CanvasProgressExportGenerator(this)
    }

    val souvenirPhotoStore: SouvenirPhotoStore by lazy {
        InternalSouvenirPhotoStore(this)
    }

    val analyticsTracker: AnalyticsTracker by lazy {
        PostHogAnalyticsTracker(this)
    }

    val billingRepository: BillingRepository by lazy {
        PlayBillingRepository(
            this,
            userPreferencesRepository,
            progressRepository,
            analyticsTracker,
            applicationScope
        )
    }

    override fun onCreate() {
        super.onCreate()
        analyticsTracker // force l'init du SDK PostHog dès le lancement de l'app
        // Applique l'opt-out analytics persisté (réglage Paramètres) — le SDK
        // démarre "actif" par défaut, il faut le couper explicitement à chaque
        // lancement si l'utilisateur avait désactivé les statistiques.
        applicationScope.launch {
            val enabled = userPreferencesRepository.observePreferences().filterNotNull().first().analyticsEnabled
            analyticsTracker.setEnabled(enabled)
        }
        // Connexion démarrée tôt pour restaurer un abonnement existant (réinstallation,
        // nouvel appareil) avant même que l'utilisateur ouvre l'écran Premium.
        billingRepository.startConnection()
        syncContent()

        // Flavor de test manuel uniquement (voir app/build.gradle.kts) : simule un
        // achat déjà effectué dès la fin de l'onboarding, sans passer par Play
        // Billing. N'existe pas dans le flavor "free" (FORCE_PREMIUM = false).
        if (BuildConfig.FORCE_PREMIUM) {
            applicationScope.launch {
                userPreferencesRepository.observePreferences().filterNotNull().first()
                userPreferencesRepository.setPremium(true)
            }
        }
    }

    /**
     * Synchro best-effort du contenu (CLAUDE.md §10 : "télécharge le JSON au
     * lancement + périodiquement"). Appelée au lancement, et à chaque changement
     * de langue depuis les Paramètres : le catalogue est servi par sous-dossier de
     * langue, il faut donc le retélécharger pour que titres, descriptions et
     * conseils suivent la nouvelle langue.
     *
     * Lancée sur la portée process et non celle d'un ViewModel : changer la langue
     * recrée l'Activity, ce qui annulerait une synchro portée par l'écran.
     *
     * L'échec ne bloque pas l'app (le cache local existant reste utilisable
     * hors-ligne) mais il est loggé : un échec totalement muet rendait
     * indiagnosticable un premier lancement sans catalogue (URL 404, coupure
     * réseau et JSON invalide donnent le même écran vide).
     */
    fun syncContent() {
        applicationScope.launch {
            runCatching { contentSyncRepository.syncIfNeeded() }
                .onFailure { Log.w("LittleBigSteps", "Échec de la synchro du contenu", it) }
        }
    }
}
