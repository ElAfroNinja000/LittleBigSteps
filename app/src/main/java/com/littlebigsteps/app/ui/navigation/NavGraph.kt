package com.littlebigsteps.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.littlebigsteps.app.ui.onboarding.OnboardingScreen
import com.littlebigsteps.app.ui.onboarding.OnboardingViewModelFactory

private object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
}

/**
 * Point d'entrée de la navigation. Démarre toujours sur l'onboarding pour
 * l'instant — à faire dépendre de UserPreferencesRepository une fois l'écran
 * d'accueil (sélection de défi) prêt, pour sauter l'onboarding déjà fait.
 */
@Composable
fun LittleBigStepsNavGraph(onboardingViewModelFactory: OnboardingViewModelFactory) {
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
            HomePlaceholder()
        }
    }
}

@Composable
private fun HomePlaceholder(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Onboarding terminé 🌱 — sélection de défi à venir",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(24.dp)
        )
    }
}
