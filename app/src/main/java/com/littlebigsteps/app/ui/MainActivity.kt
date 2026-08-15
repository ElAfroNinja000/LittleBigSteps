package com.littlebigsteps.app.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.littlebigsteps.app.LittleBigStepsApplication
import com.littlebigsteps.app.ui.challenge.ChallengeSelectionViewModelFactory
import com.littlebigsteps.app.ui.navigation.LittleBigStepsNavGraph
import com.littlebigsteps.app.ui.onboarding.OnboardingViewModelFactory
import com.littlebigsteps.app.ui.portfolio.PortfolioViewModelFactory
import com.littlebigsteps.app.ui.premium.PremiumViewModelFactory
import com.littlebigsteps.app.ui.progress.ProgressViewModelFactory
import com.littlebigsteps.app.ui.settings.SettingsViewModelFactory
import com.littlebigsteps.app.ui.theme.LittleBigStepsTheme

/**
 * Point d'entrée unique de l'app (une seule Activity, navigation gérée en Compose).
 *
 * AppCompatActivity et non ComponentActivity : le sélecteur de langue des
 * Paramètres passe par AppCompatDelegate.setApplicationLocales, qui ne fait
 * silencieusement rien tant qu'aucun délégué AppCompat n'est enregistré
 * (getLocaleManagerForApplication parcourt sActivityDelegates et renvoie null
 * si l'ensemble est vide, y compris sur API 33+). Impose que le thème du
 * manifeste descende d'AppCompat, voir res/values/themes.xml.
 */
class MainActivity : AppCompatActivity() {

    // Le refus n'est pas traité comme une erreur : les rappels restent silencieux
    // (voir NotificationHelper.showReminder), cohérent avec l'absence de
    // culpabilisation du produit (CLAUDE.md §4).
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as LittleBigStepsApplication
        val onboardingViewModelFactory = OnboardingViewModelFactory(
            userPreferencesRepository = app.userPreferencesRepository,
            progressRepository = app.progressRepository,
            notificationScheduler = app.notificationScheduler,
            analyticsTracker = app.analyticsTracker
        )
        val challengeSelectionViewModelFactory = ChallengeSelectionViewModelFactory(
            challengeRepository = app.challengeRepository,
            progressRepository = app.progressRepository,
            souvenirPhotoStore = app.souvenirPhotoStore,
            userPreferencesRepository = app.userPreferencesRepository,
            analyticsTracker = app.analyticsTracker
        )
        val portfolioViewModelFactory = PortfolioViewModelFactory(
            challengeRepository = app.challengeRepository
        )
        val progressViewModelFactory = ProgressViewModelFactory(
            progressRepository = app.progressRepository,
            challengeRepository = app.challengeRepository,
            exportGenerator = app.progressExportGenerator,
            userPreferencesRepository = app.userPreferencesRepository,
            analyticsTracker = app.analyticsTracker
        )
        val premiumViewModelFactory = PremiumViewModelFactory(
            billingRepository = app.billingRepository,
            userPreferencesRepository = app.userPreferencesRepository
        )
        val settingsViewModelFactory = SettingsViewModelFactory(
            userPreferencesRepository = app.userPreferencesRepository,
            progressRepository = app.progressRepository,
            challengeRepository = app.challengeRepository,
            notificationScheduler = app.notificationScheduler
        )

        setContent {
            LittleBigStepsTheme {
                LittleBigStepsNavGraph(
                    userPreferencesRepository = app.userPreferencesRepository,
                    onboardingViewModelFactory = onboardingViewModelFactory,
                    challengeSelectionViewModelFactory = challengeSelectionViewModelFactory,
                    portfolioViewModelFactory = portfolioViewModelFactory,
                    progressViewModelFactory = progressViewModelFactory,
                    premiumViewModelFactory = premiumViewModelFactory,
                    settingsViewModelFactory = settingsViewModelFactory,
                    onOnboardingComplete = ::requestNotificationPermissionIfNeeded
                )
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
