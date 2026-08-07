package com.littlebigsteps.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.littlebigsteps.app.LittleBigStepsApplication
import com.littlebigsteps.app.ui.challenge.ChallengeSelectionViewModelFactory
import com.littlebigsteps.app.ui.navigation.LittleBigStepsNavGraph
import com.littlebigsteps.app.ui.onboarding.OnboardingViewModelFactory
import com.littlebigsteps.app.ui.portfolio.PortfolioViewModelFactory
import com.littlebigsteps.app.ui.progress.ProgressViewModelFactory
import com.littlebigsteps.app.ui.theme.LittleBigStepsTheme

/** Point d'entrée unique de l'app (une seule Activity, navigation gérée en Compose). */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as LittleBigStepsApplication
        val onboardingViewModelFactory = OnboardingViewModelFactory(
            userPreferencesRepository = app.userPreferencesRepository,
            progressRepository = app.progressRepository
        )
        val challengeSelectionViewModelFactory = ChallengeSelectionViewModelFactory(
            challengeRepository = app.challengeRepository,
            progressRepository = app.progressRepository
        )
        val portfolioViewModelFactory = PortfolioViewModelFactory(
            challengeRepository = app.challengeRepository
        )
        val progressViewModelFactory = ProgressViewModelFactory(
            progressRepository = app.progressRepository
        )

        setContent {
            LittleBigStepsTheme {
                LittleBigStepsNavGraph(
                    onboardingViewModelFactory = onboardingViewModelFactory,
                    challengeSelectionViewModelFactory = challengeSelectionViewModelFactory,
                    portfolioViewModelFactory = portfolioViewModelFactory,
                    progressViewModelFactory = progressViewModelFactory
                )
            }
        }
    }
}
