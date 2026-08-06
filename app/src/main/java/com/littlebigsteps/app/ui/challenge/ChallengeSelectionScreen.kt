package com.littlebigsteps.app.ui.challenge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.spacedBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.littlebigsteps.app.data.local.entity.ChallengeEntity
import com.littlebigsteps.app.domain.model.MediumType
import com.littlebigsteps.app.ui.common.label

/**
 * Écran cœur du core loop (CLAUDE.md §3.2-3.3) : propose 2-3 défis, laisse en
 * choisir un, puis le marquer terminé avec un souvenir optionnel.
 */
@Composable
fun ChallengeSelectionScreen(
    factory: ChallengeSelectionViewModelFactory,
    modifier: Modifier = Modifier
) {
    val viewModel: ChallengeSelectionViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsState()
    val completion = state.lastCompletion
    val selected = state.selectedChallenge

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        when {
            state.mediumType == null && !state.isLoading -> Text(
                "Aucun médium débloqué pour l'instant — termine l'onboarding pour commencer.",
                style = MaterialTheme.typography.bodyLarge
            )
            completion != null -> CompletionRecap(
                xpEarned = completion.xpEarned,
                onContinue = viewModel::dismissCompletion
            )
            selected != null -> ChallengeDetail(
                challenge = selected,
                note = state.souvenirNote,
                isCompleting = state.isCompleting,
                onNoteChange = viewModel::updateSouvenirNote,
                onComplete = viewModel::completeSelectedChallenge,
                onBack = viewModel::clearSelection
            )
            else -> ChallengeOptionsList(
                state = state,
                onSelectMedium = viewModel::selectMedium,
                onSelectChallenge = viewModel::selectChallenge,
                onRefresh = viewModel::refreshOptions
            )
        }
    }
}

@Composable
private fun ChallengeOptionsList(
    state: ChallengeSelectionUiState,
    onSelectMedium: (MediumType) -> Unit,
    onSelectChallenge: (ChallengeEntity) -> Unit,
    onRefresh: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Choisis ton défi", style = MaterialTheme.typography.titleMedium)

        // Le sélecteur de médium n'a d'intérêt qu'une fois plusieurs médiums
        // débloqués (premium) — un seul au départ (modèle freemium, CLAUDE.md §7).
        if (state.availableMediums.size > 1) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.availableMediums.forEach { medium ->
                    AssistChip(onClick = { onSelectMedium(medium) }, label = { Text(medium.label()) })
                }
            }
        }

        when {
            state.isLoading -> CircularProgressIndicator()
            state.options.isEmpty() -> Text(
                "Aucun défi disponible pour ce médium pour l'instant.",
                style = MaterialTheme.typography.bodyLarge
            )
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.options, key = { it.id }) { challenge ->
                        ChallengeOptionCard(challenge = challenge, onClick = { onSelectChallenge(challenge) })
                    }
                }
                OutlinedButton(onClick = onRefresh) {
                    Text("Voir d'autres propositions")
                }
            }
        }
    }
}

@Composable
private fun ChallengeOptionCard(challenge: ChallengeEntity, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(challenge.title, style = MaterialTheme.typography.titleMedium)
            Text(challenge.description, style = MaterialTheme.typography.bodyLarge)
            Text("${challenge.estimatedMinutes} min", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ChallengeDetail(
    challenge: ChallengeEntity,
    note: String,
    isCompleting: Boolean,
    onNoteChange: (String) -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(challenge.title, style = MaterialTheme.typography.titleMedium)
        Text(challenge.description, style = MaterialTheme.typography.bodyLarge)
        Text("${challenge.estimatedMinutes} min", style = MaterialTheme.typography.bodyLarge)

        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            label = { Text("Souvenir (optionnel)") },
            modifier = Modifier.fillMaxWidth()
        )
        // Capture photo pas encore branchée (permissions caméra/galerie + écriture
        // en stockage interne) : souvenirPhotoPath reste null pour l'instant,
        // voir ChallengeSelectionViewModel.completeSelectedChallenge.

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack, enabled = !isCompleting) { Text("Changer de défi") }
            Button(onClick = onComplete, enabled = !isCompleting) { Text("Marquer terminé") }
        }
    }
}

@Composable
private fun CompletionRecap(xpEarned: Int, onContinue: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Bravo ! 🎉", style = MaterialTheme.typography.titleMedium)
        Text("+$xpEarned XP", style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onContinue) { Text("Continuer") }
    }
}
