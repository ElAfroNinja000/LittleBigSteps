package com.littlebigsteps.app.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.littlebigsteps.app.R
import com.littlebigsteps.app.domain.model.MediumType
import com.littlebigsteps.app.ui.common.StepperButton
import com.littlebigsteps.app.ui.common.TimeStepperPicker
import com.littlebigsteps.app.ui.common.icon
import com.littlebigsteps.app.ui.common.label
import com.littlebigsteps.app.ui.theme.PillShape
import com.littlebigsteps.app.ui.theme.mediumColors
import kotlinx.datetime.LocalTime

/**
 * Parcours d'onboarding en 4-5 étapes (CLAUDE.md §3.1). `onOnboardingComplete`
 * est déclenché une fois les préférences enregistrées en local. Fréquence et
 * heure de rappel restent modifiables ensuite depuis les Paramètres (voir
 * ui/settings/SettingsScreen.kt, qui réutilise les mêmes roues/stepper).
 */
@Composable
fun OnboardingScreen(
    factory: OnboardingViewModelFactory,
    onOnboardingComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: OnboardingViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) onOnboardingComplete()
    }
    if (state.isComplete) return

    // Écran d'accueil : mise en page dédiée (pas de barre Retour/Suivant), un
    // seul bouton pleine largeur pour démarrer le parcours à proprement parler.
    if (state.step == OnboardingStep.WELCOME) {
        WelcomeStep(modifier = modifier, onContinue = viewModel::goToNextStep)
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            when (state.step) {
                OnboardingStep.WELCOME -> Unit // géré plus haut, jamais atteint ici
                OnboardingStep.MEDIUM_CHOICE -> MediumChoiceStep(state, viewModel::toggleMedium)
                OnboardingStep.FREQUENCY_CHOICE -> FrequencyChoiceStep(state, viewModel::selectFrequency)
                OnboardingStep.REMINDER_TIME_CHOICE -> ReminderTimeChoiceStep(state, viewModel::selectReminderTime)
                OnboardingStep.DONE -> Unit
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = viewModel::goToPreviousStep,
                enabled = state.step != OnboardingStep.MEDIUM_CHOICE,
                shape = PillShape
            ) {
                Text(stringResource(R.string.action_back))
            }
            Button(
                onClick = viewModel::goToNextStep,
                enabled = state.canContinue && !state.isSaving,
                shape = PillShape
            ) {
                Text(
                    stringResource(
                        if (state.step == OnboardingStep.REMINDER_TIME_CHOICE) {
                            R.string.onboarding_finish
                        } else {
                            R.string.onboarding_next
                        }
                    )
                )
            }
        }
    }
}

/** Pas de bloc distinct : le fond est le même blanc chaud que le reste de
 *  l'app, seuls le cercle mint et le bouton créent l'emphase (DA). */
@Composable
private fun WelcomeStep(modifier: Modifier = Modifier, onContinue: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Le logo de l'app, repris directement du foreground de l'icône
            // adaptative : une seule source de vérité, l'écran d'accueil ne peut
            // pas diverger de l'icône du launcher. Décoratif (le titre juste en
            // dessous porte déjà le nom), d'où contentDescription = null. La
            // taille tient compte de la marge de sécurité de l'adaptive icon :
            // la rosace n'occupe qu'environ 60 % du drawable.
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(140.dp)
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                stringResource(R.string.onboarding_welcome_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(R.string.onboarding_welcome_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        Button(
            onClick = onContinue,
            shape = PillShape,
            contentPadding = PaddingValues(vertical = 14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.onboarding_welcome_cta), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun MediumChoiceStep(state: OnboardingUiState, onToggle: (MediumType) -> Unit) {
    Text(stringResource(R.string.onboarding_medium_choice_title), style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(4.dp))
    MediumType.entries.chunked(2).forEach { row ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            row.forEach { medium ->
                MediumTile(
                    medium = medium,
                    selected = medium in state.selectedMediums,
                    onClick = { onToggle(medium) },
                    modifier = Modifier.weight(1f)
                )
            }
            if (row.size < 2) Spacer(modifier = Modifier.weight(1f))
        }
    }
}

/** Tuile carrée : contour coloré du médium au repos, aplat plein une fois
 *  sélectionnée — icône et texte restent dans l'encre du médium dans les
 *  deux états. */
@Composable
private fun MediumTile(
    medium: MediumType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = mediumColors(medium)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.medium)
            .background(if (selected) colors.container else MaterialTheme.colorScheme.surface)
            .border(
                width = if (selected) 0.dp else 1.5.dp,
                color = colors.container,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(medium.icon(), contentDescription = null, tint = colors.onContainer)
            Text(medium.label(), style = MaterialTheme.typography.labelMedium, color = colors.onContainer)
        }
    }
}

@Composable
private fun ColumnScope.FrequencyChoiceStep(state: OnboardingUiState, onSelect: (Int) -> Unit) {
    val timesPerWeek = state.frequency.timesPerWeek
    Text(stringResource(R.string.onboarding_frequency_title), style = MaterialTheme.typography.titleMedium)
    // Le sélecteur occupe tout l'espace restant sous le titre et s'y centre :
    // le compteur reste au milieu de l'écran plutôt que collé au titre. Le
    // titre porte déjà "fois par semaine", pas de libellé redondant dessous.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepperButton(
            icon = Icons.Filled.Remove,
            enabled = timesPerWeek > 1,
            onClick = { onSelect(timesPerWeek - 1) }
        )
        Spacer(modifier = Modifier.width(20.dp))
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$timesPerWeek",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(modifier = Modifier.width(20.dp))
        StepperButton(
            icon = Icons.Filled.Add,
            enabled = timesPerWeek < 7,
            onClick = { onSelect(timesPerWeek + 1) }
        )
    }
}

@Composable
private fun ColumnScope.ReminderTimeChoiceStep(state: OnboardingUiState, onSelect: (LocalTime) -> Unit) {
    Text(stringResource(R.string.onboarding_time_title), style = MaterialTheme.typography.titleMedium)

    // Même centrage que l'étape fréquence : le sélecteur occupe l'espace
    // restant sous le titre et s'y centre.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimeStepperPicker(time = state.reminderTime, onSelect = onSelect)
    }
}

