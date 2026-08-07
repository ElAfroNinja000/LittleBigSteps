package com.littlebigsteps.app

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.littlebigsteps.app.data.local.entity.ChallengeEntity
import com.littlebigsteps.app.domain.model.ChallengeLevel
import com.littlebigsteps.app.domain.model.Frequency
import com.littlebigsteps.app.domain.model.MediumType
import com.littlebigsteps.app.ui.navigation.LittleBigStepsNavGraph
import com.littlebigsteps.app.ui.theme.LittleBigStepsTheme
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalTime
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Parcours bout-en-bout du core loop (CLAUDE.md §3) : onboarding -> sélection
 * de défi -> complétion avec souvenir -> portfolio -> progression. Voir
 * CLAUDE.md §11 pour la stratégie de test (ce qui est réel vs simulé).
 */
@RunWith(AndroidJUnit4::class)
class CoreLoopE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var graph: TestAppGraph

    private val seededChallenge = ChallengeEntity(
        id = "test_drawing_001",
        mediumType = MediumType.DRAWING,
        title = "Dessine l'objet le plus proche de toi",
        description = "Un défi de test.",
        estimatedMinutes = 10,
        level = ChallengeLevel.BEGINNER,
        isPremiumOnly = false,
        tags = null
    )

    @Before
    fun setUp() {
        graph = TestAppGraph(composeTestRule.activity)
        runBlocking {
            graph.database.challengeDao().upsertAll(listOf(seededChallenge))
        }
    }

    private fun setNavGraphContent() {
        composeTestRule.setContent {
            LittleBigStepsTheme {
                LittleBigStepsNavGraph(
                    userPreferencesRepository = graph.userPreferencesRepository,
                    onboardingViewModelFactory = graph.onboardingViewModelFactory,
                    challengeSelectionViewModelFactory = graph.challengeSelectionViewModelFactory,
                    portfolioViewModelFactory = graph.portfolioViewModelFactory,
                    progressViewModelFactory = graph.progressViewModelFactory,
                    premiumViewModelFactory = graph.premiumViewModelFactory
                )
            }
        }
    }

    /** L'état issu d'un onboarding/complétion est écrit en base par une coroutine
     *  qui saute sur l'exécuteur Room avant de revenir recomposer l'UI : on
     *  attend explicitement plutôt que de compter sur la seule synchronisation
     *  de Compose (voir CLAUDE.md §11). */
    private fun waitUntilExists(text: String, timeoutMillis: Long = 5_000) {
        composeTestRule.waitUntil(timeoutMillis) {
            composeTestRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun completingOnboardingAndAChallengeUpdatesPortfolioAndProgress() {
        setNavGraphContent()

        // --- Onboarding : mono-médium Dessin, rappel quotidien le matin ---
        composeTestRule.onNodeWithText("Un seul médium").performClick()
        composeTestRule.onNodeWithText("Suivant").performClick()

        composeTestRule.onNodeWithText("Dessin").performClick()
        composeTestRule.onNodeWithText("Suivant").performClick()

        composeTestRule.onNodeWithText("Tous les jours").performClick()
        composeTestRule.onNodeWithText("Suivant").performClick()

        composeTestRule.onNodeWithText("Matin (9h)").performClick()
        composeTestRule.onNodeWithText("Terminer").performClick()

        waitUntilExists(seededChallenge.title)

        // --- Sélection puis complétion du défi avec un souvenir texte ---
        composeTestRule.onNodeWithText(seededChallenge.title).performClick()
        composeTestRule.onNodeWithText("Souvenir (optionnel)").performTextInput("Premier essai !")
        composeTestRule.onNodeWithText("Marquer terminé").performClick()

        waitUntilExists("+20 XP")
        composeTestRule.onNodeWithText("Continuer").performClick()

        // --- Portfolio : le défi complété apparaît avec son souvenir ---
        composeTestRule.onNodeWithText("Portfolio").performClick()
        waitUntilExists(seededChallenge.title)
        composeTestRule.onNodeWithText("« Premier essai ! »").assertExists()

        // --- Progression : le streak a été mis à jour ---
        composeTestRule.onNodeWithText("Progression").performClick()
        waitUntilExists("1 jour(s) de suite")

        // --- Les événements attendus ont bien été suivis (CLAUDE.md §8) ---
        assert("onboarding_completed" in graph.analyticsTracker.events)
        assert("challenge_completed" in graph.analyticsTracker.events)
    }

    @Test
    fun onboardingIsSkippedWhenAlreadyCompleted() {
        runBlocking {
            graph.userPreferencesRepository.completeOnboarding(
                selectedMediums = listOf(MediumType.DRAWING),
                freeMedium = MediumType.DRAWING,
                reminderFrequency = Frequency.DAILY,
                reminderTime = LocalTime(9, 0)
            )
            graph.progressRepository.ensureMediumRowsExist(unlockedMediums = setOf(MediumType.DRAWING))
        }

        setNavGraphContent()

        waitUntilExists("Choisis ton défi")
        composeTestRule.onNodeWithText("Comment veux-tu pratiquer ?").assertDoesNotExist()
    }
}
