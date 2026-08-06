package com.littlebigsteps.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.littlebigsteps.app.ui.challenge.ChallengeSelectionScreen
import com.littlebigsteps.app.ui.challenge.ChallengeSelectionViewModelFactory
import com.littlebigsteps.app.ui.onboarding.OnboardingScreen
import com.littlebigsteps.app.ui.onboarding.OnboardingViewModelFactory

private object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
}

/**
 * Point d'entrée de la navigation. Démarre toujours sur l'onboarding pour
 * l'instant — à faire dépendre de UserPreferencesRepository une fois qu'on
 * veut sauter l'onboarding déjà fait lors d'un relancement de l'app.
 */
@Composable
fun LittleBigStepsNavGraph(
    onboardingViewModelFactory: OnboardingViewModelFactory,
    challengeSelectionViewModelFactory: ChallengeSelectionViewModelFactory
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.ONBOARDING) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                factory = onboardingViewModelFactory,
                onOnboardingComplete = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            ChallengeSelectionScreen(factory = challengeSelectionViewModelFactory)
        }
    }
}
