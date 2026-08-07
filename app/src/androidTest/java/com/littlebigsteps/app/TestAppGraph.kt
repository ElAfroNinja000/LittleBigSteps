package com.littlebigsteps.app

import android.content.Context
import androidx.room.Room
import com.littlebigsteps.app.data.local.AppDatabase
import com.littlebigsteps.app.data.media.InternalSouvenirPhotoStore
import com.littlebigsteps.app.data.repository.ChallengeRepositoryImpl
import com.littlebigsteps.app.data.repository.ProgressRepositoryImpl
import com.littlebigsteps.app.data.repository.UserPreferencesRepositoryImpl
import com.littlebigsteps.app.export.CanvasProgressExportGenerator
import com.littlebigsteps.app.fakes.FakeAnalyticsTracker
import com.littlebigsteps.app.fakes.FakeBillingRepository
import com.littlebigsteps.app.fakes.FakeNotificationScheduler
import com.littlebigsteps.app.ui.challenge.ChallengeSelectionViewModelFactory
import com.littlebigsteps.app.ui.onboarding.OnboardingViewModelFactory
import com.littlebigsteps.app.ui.portfolio.PortfolioViewModelFactory
import com.littlebigsteps.app.ui.premium.PremiumViewModelFactory
import com.littlebigsteps.app.ui.progress.ProgressViewModelFactory

/**
 * Graphe de dépendances de test : mêmes repositories/implémentations que
 * [LittleBigStepsApplication] (Room, ChallengeRepository, ProgressRepository...),
 * base en mémoire fraîche à chaque instance, et fakes uniquement pour les
 * vraies frontières externes (Billing, notifications, analytics) — voir
 * CLAUDE.md §11 pour la stratégie de test.
 */
class TestAppGraph(context: Context) {

    val database: AppDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()

    val userPreferencesRepository = UserPreferencesRepositoryImpl(database.userPreferencesDao())
    val progressRepository = ProgressRepositoryImpl(database)
    val challengeRepository = ChallengeRepositoryImpl(database, progressRepository)
    val souvenirPhotoStore = InternalSouvenirPhotoStore(context)
    val exportGenerator = CanvasProgressExportGenerator(context)

    val notificationScheduler = FakeNotificationScheduler()
    val analyticsTracker = FakeAnalyticsTracker()
    val billingRepository = FakeBillingRepository()

    val onboardingViewModelFactory = OnboardingViewModelFactory(
        userPreferencesRepository = userPreferencesRepository,
        progressRepository = progressRepository,
        notificationScheduler = notificationScheduler,
        analyticsTracker = analyticsTracker
    )
    val challengeSelectionViewModelFactory = ChallengeSelectionViewModelFactory(
        challengeRepository = challengeRepository,
        progressRepository = progressRepository,
        souvenirPhotoStore = souvenirPhotoStore,
        analyticsTracker = analyticsTracker
    )
    val portfolioViewModelFactory = PortfolioViewModelFactory(
        challengeRepository = challengeRepository
    )
    val progressViewModelFactory = ProgressViewModelFactory(
        progressRepository = progressRepository,
        challengeRepository = challengeRepository,
        exportGenerator = exportGenerator,
        analyticsTracker = analyticsTracker
    )
    val premiumViewModelFactory = PremiumViewModelFactory(
        billingRepository = billingRepository,
        userPreferencesRepository = userPreferencesRepository
    )
}
