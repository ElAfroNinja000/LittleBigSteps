package com.littlebigsteps.app

import android.app.Application
import com.littlebigsteps.app.billing.BillingRepository
import com.littlebigsteps.app.billing.PlayBillingRepository
import com.littlebigsteps.app.data.local.AppDatabase
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
        ChallengeRepositoryImpl(database, progressRepository)
    }

    val contentSyncRepository: ContentSyncRepository by lazy {
        ContentSyncRepositoryImpl(contentApi, database)
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

    val billingRepository: BillingRepository by lazy {
        PlayBillingRepository(this, userPreferencesRepository, progressRepository, applicationScope)
    }

    override fun onCreate() {
        super.onCreate()
        // Connexion démarrée tôt pour restaurer un abonnement existant (réinstallation,
        // nouvel appareil) avant même que l'utilisateur ouvre l'écran Premium.
        billingRepository.startConnection()
    }
}
