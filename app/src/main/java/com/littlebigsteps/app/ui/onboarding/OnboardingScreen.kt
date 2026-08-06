package com.littlebigsteps.app.ui.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.spacedBy
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.littlebigsteps.app.domain.model.Frequency
import com.littlebigsteps.app.domain.model.MediumType
import com.littlebigsteps.app.ui.common.label
import kotlinx.datetime.LocalTime

/**
 * Parcours d'onboarding en 4-5 étapes (CLAUDE.md §3.1). `onOnboardingComplete`
 * est déclenché une fois les préférences enregistrées en local.
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            when (state.step) {
                OnboardingStep.MODE_CHOICE -> ModeChoiceStep(state, viewModel::selectMode)
                OnboardingStep.MEDIUM_CHOICE -> MediumChoiceStep(state, viewModel::toggleMedium)
                OnboardingStep.FREE_MEDIUM_CHOICE -> FreeMediumChoiceStep(state, viewModel::selectFreeMedium)
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
                enabled = state.step != OnboardingStep.MODE_CHOICE
            ) {
                Text("Retour")
            }
            Button(
                onClick = viewModel::goToNextStep,
                enabled = state.canContinue && !state.isSaving
            ) {
                Text(if (state.step == OnboardingStep.REMINDER_TIME_CHOICE) "Terminer" else "Suivant")
            }
        }
    }
}

@Composable
private fun ModeChoiceStep(state: OnboardingUiState, onSelect: (OnboardingMode) -> Unit) {
    Text("Comment veux-tu pratiquer ?", style = MaterialTheme.typography.titleMedium)
    OnboardingOption(
        label = "Un seul médium",
        selected = state.mode == OnboardingMode.MONO,
        onClick = { onSelect(OnboardingMode.MONO) }
    )
    OnboardingOption(
        label = "Découvrir plusieurs médiums",
        selected = state.mode == OnboardingMode.MULTI,
        onClick = { onSelect(OnboardingMode.MULTI) }
    )
}

@Composable
private fun MediumChoiceStep(state: OnboardingUiState, onToggle: (MediumType) -> Unit) {
    val title = if (state.mode == OnboardingMode.MONO) "Quel médium ?" else "Lesquels veux-tu explorer ?"
    Text(title, style = MaterialTheme.typography.titleMedium)
    MediumType.entries.forEach { medium ->
        OnboardingOption(
            label = medium.label(),
            selected = medium in state.selectedMediums,
            onClick = { onToggle(medium) },
            multiSelect = state.mode == OnboardingMode.MULTI
        )
    }
}

@Composable
private fun FreeMediumChoiceStep(state: OnboardingUiState, onSelect: (MediumType) -> Unit) {
    Text("Lequel démarrer gratuitement ?", style = MaterialTheme.typography.titleMedium)
    Text(
        "Les autres restent visibles, à débloquer plus tard.",
        style = MaterialTheme.typography.bodyLarge
    )
    state.selectedMediums.forEach { medium ->
        OnboardingOption(
            label = medium.label(),
            selected = state.freeMedium == medium,
            onClick = { onSelect(medium) }
        )
    }
}

@Composable
private fun FrequencyChoiceStep(state: OnboardingUiState, onSelect: (Frequency) -> Unit) {
    Text("À quelle fréquence veux-tu un rappel ?", style = MaterialTheme.typography.titleMedium)
    Frequency.entries.forEach { frequency ->
        OnboardingOption(
            label = frequency.label(),
            selected = state.frequency == frequency,
            onClick = { onSelect(frequency) }
        )
    }
}

@Composable
private fun ReminderTimeChoiceStep(state: OnboardingUiState, onSelect: (LocalTime) -> Unit) {
    Text("À quel moment ?", style = MaterialTheme.typography.titleMedium)
    ReminderTimePreset.entries.forEach { preset ->
        OnboardingOption(
            label = preset.label,
            selected = state.reminderTime == preset.time,
            onClick = { onSelect(preset.time) }
        )
    }
}

@Composable
private fun OnboardingOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    multiSelect: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (multiSelect) {
            Checkbox(checked = selected, onCheckedChange = { onClick() })
        } else {
            RadioButton(selected = selected, onClick = onClick)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
