package com.littlebigsteps.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.littlebigsteps.app.ui.challenge.ChallengeSelectionScreen
import com.littlebigsteps.app.ui.challenge.ChallengeSelectionViewModelFactory
import com.littlebigsteps.app.ui.onboarding.OnboardingScreen
import com.littlebigsteps.app.ui.onboarding.OnboardingViewModelFactory
import com.littlebigsteps.app.ui.portfolio.PortfolioScreen
import com.littlebigsteps.app.ui.portfolio.PortfolioViewModelFactory
import com.littlebigsteps.app.ui.progress.ProgressScreen
import com.littlebigsteps.app.ui.progress.ProgressViewModelFactory

private object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val PORTFOLIO = "portfolio"
    const val PROGRESS = "progress"
}

private data class BottomDestination(val route: String, val label: String, val icon: ImageVector)

private val bottomDestinations = listOf(
    BottomDestination(Routes.HOME, "Défis", Icons.Filled.AutoAwesome),
    BottomDestination(Routes.PORTFOLIO, "Portfolio", Icons.Filled.Collections),
    BottomDestination(Routes.PROGRESS, "Progression", Icons.Filled.Insights)
)

/**
 * Point d'entrée de la navigation. Démarre toujours sur l'onboarding pour
 * l'instant — à faire dépendre de UserPreferencesRepository une fois qu'on
 * veut sauter l'onboarding déjà fait lors d'un relancement de l'app. La barre
 * de navigation basse n'apparaît que sur les écrans post-onboarding.
 */
@Composable
fun LittleBigStepsNavGraph(
    onboardingViewModelFactory: OnboardingViewModelFactory,
    challengeSelectionViewModelFactory: ChallengeSelectionViewModelFactory,
    portfolioViewModelFactory: PortfolioViewModelFactory,
    progressViewModelFactory: ProgressViewModelFactory,
    onOnboardingComplete: () -> Unit = {}
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomDestinations.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.ONBOARDING,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    factory = onboardingViewModelFactory,
                    onOnboardingComplete = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                        onOnboardingComplete()
                    }
                )
            }
            composable(Routes.HOME) {
                ChallengeSelectionScreen(factory = challengeSelectionViewModelFactory)
            }
            composable(Routes.PORTFOLIO) {
                PortfolioScreen(factory = portfolioViewModelFactory)
            }
            composable(Routes.PROGRESS) {
                ProgressScreen(factory = progressViewModelFactory)
            }
        }
    }
}
