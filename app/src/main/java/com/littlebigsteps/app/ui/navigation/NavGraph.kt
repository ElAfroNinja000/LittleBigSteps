package com.littlebigsteps.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.littlebigsteps.app.R
import com.littlebigsteps.app.data.repository.UserPreferencesRepository
import com.littlebigsteps.app.ui.challenge.ChallengeSelectionScreen
import com.littlebigsteps.app.ui.challenge.ChallengeSelectionViewModelFactory
import com.littlebigsteps.app.ui.onboarding.OnboardingScreen
import com.littlebigsteps.app.ui.onboarding.OnboardingViewModelFactory
import com.littlebigsteps.app.ui.portfolio.PortfolioScreen
import com.littlebigsteps.app.ui.portfolio.PortfolioViewModelFactory
import com.littlebigsteps.app.ui.premium.PremiumScreen
import com.littlebigsteps.app.ui.premium.PremiumViewModelFactory
import com.littlebigsteps.app.ui.progress.ProgressScreen
import com.littlebigsteps.app.ui.progress.ProgressViewModelFactory
import com.littlebigsteps.app.ui.settings.SettingsScreen
import com.littlebigsteps.app.ui.settings.SettingsViewModelFactory
import kotlinx.coroutines.flow.first

private object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val PORTFOLIO = "portfolio"
    const val PROGRESS = "progress"
    const val PREMIUM = "premium"
    const val SETTINGS = "settings"
}

private data class BottomDestination(val route: String, @StringRes val labelRes: Int, val icon: ImageVector)

private val bottomDestinations = listOf(
    // "Portfolio"/"Progression" réutilisent le libellé du titre d'écran
    // correspondant (même mot exact) plutôt que dupliquer la traduction.
    BottomDestination(Routes.HOME, R.string.nav_activities, Icons.Filled.AutoAwesome),
    BottomDestination(Routes.PORTFOLIO, R.string.portfolio_title, Icons.Filled.Collections),
    BottomDestination(Routes.PROGRESS, R.string.progress_title, Icons.Filled.Insights)
)

/**
 * Point d'entrée de la navigation. Saute l'onboarding déjà fait lors d'un
 * relancement de l'app (onOnboardingCompletedAt non-null en local, voir
 * UserPreferencesRepository) — le NavHost n'est créé qu'une fois cette
 * vérification faite, pour lui fixer le bon startDestination dès le départ.
 * La barre de navigation basse (et l'icône Paramètres) n'apparaît que sur les
 * écrans post-onboarding.
 */
@Composable
fun LittleBigStepsNavGraph(
    userPreferencesRepository: UserPreferencesRepository,
    onboardingViewModelFactory: OnboardingViewModelFactory,
    challengeSelectionViewModelFactory: ChallengeSelectionViewModelFactory,
    portfolioViewModelFactory: PortfolioViewModelFactory,
    progressViewModelFactory: ProgressViewModelFactory,
    premiumViewModelFactory: PremiumViewModelFactory,
    settingsViewModelFactory: SettingsViewModelFactory,
    onOnboardingComplete: () -> Unit = {}
) {
    val isOnboardingDone by produceState<Boolean?>(initialValue = null) {
        value = userPreferencesRepository.observePreferences().first()?.onboardingCompletedAt != null
    }
    val onboardingDone = isOnboardingDone
    if (onboardingDone == null) {
        Box(Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
        return
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomDestinations.any { it.route == currentRoute }

    Scaffold(
        topBar = {
            // Seule l'icône Paramètres ici (pas de titre : chaque écran porte
            // déjà le sien dans son propre contenu) — accessible depuis les 3
            // onglets principaux, jamais depuis l'onboarding/Premium/Paramètres
            // lui-même.
            if (showBottomBar) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, end = 12.dp)) {
                    IconButton(
                        onClick = { navController.navigate(Routes.SETTINGS) },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (showBottomBar) {
                // Barre posée à plat sur le fond de page (pas de teinte propre) :
                // seule la pastille mint de l'onglet actif marque la sélection.
                NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                    bottomDestinations.forEach { destination ->
                        val label = stringResource(destination.labelRes)
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = label) },
                            label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.onBackground,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (onboardingDone) Routes.HOME else Routes.ONBOARDING,
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
                ChallengeSelectionScreen(
                    factory = challengeSelectionViewModelFactory,
                    onNavigateToPremium = { navController.navigate(Routes.PREMIUM) },
                    onNavigateToProgress = {
                        navController.navigate(Routes.PROGRESS) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Routes.PORTFOLIO) {
                PortfolioScreen(factory = portfolioViewModelFactory)
            }
            composable(Routes.PROGRESS) {
                ProgressScreen(
                    factory = progressViewModelFactory,
                    onNavigateToPremium = { navController.navigate(Routes.PREMIUM) }
                )
            }
            composable(Routes.PREMIUM) {
                PremiumScreen(
                    factory = premiumViewModelFactory,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    factory = settingsViewModelFactory,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
