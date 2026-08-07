package com.littlebigsteps.app.ui.challenge

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.spacedBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.littlebigsteps.app.data.local.entity.ChallengeEntity
import com.littlebigsteps.app.data.local.entity.ChallengePackEntity
import com.littlebigsteps.app.domain.model.MediumType
import com.littlebigsteps.app.ui.common.LocalPhotoThumbnail
import com.littlebigsteps.app.ui.common.label

/**
 * Écran cœur du core loop (CLAUDE.md §3.2-3.3) : propose 2-3 défis, laisse en
 * choisir un, puis le marquer terminé avec un souvenir optionnel. Permet aussi
 * de parcourir les packs thématiques/saisonniers premium (§7).
 */
@Composable
fun ChallengeSelectionScreen(
    factory: ChallengeSelectionViewModelFactory,
    onNavigateToPremium: () -> Unit,
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
                photoPath = state.souvenirPhotoPath,
                isCompleting = state.isCompleting,
                onNoteChange = viewModel::updateSouvenirNote,
                onPrepareCameraCapture = viewModel::prepareCameraCapture,
                onCameraResult = viewModel::onCameraResult,
                onGallerySelected = viewModel::onGalleryImageSelected,
                onRemovePhoto = viewModel::removeSouvenirPhoto,
                onComplete = viewModel::completeSelectedChallenge,
                onBack = viewModel::clearSelection
            )
            else -> ChallengeOptionsList(
                state = state,
                onSelectMedium = viewModel::selectMedium,
                onSelectChallenge = viewModel::selectChallenge,
                onRefresh = viewModel::refreshOptions,
                onSelectPack = viewModel::selectPack,
                onExitPack = viewModel::exitPack,
                onNavigateToPremium = onNavigateToPremium
            )
        }
    }
}

@Composable
private fun ChallengeOptionsList(
    state: ChallengeSelectionUiState,
    onSelectMedium: (MediumType) -> Unit,
    onSelectChallenge: (ChallengeEntity) -> Unit,
    onRefresh: () -> Unit,
    onSelectPack: (ChallengePackEntity) -> Unit,
    onExitPack: () -> Unit,
    onNavigateToPremium: () -> Unit
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

        val activePack = state.activePack
        when {
            activePack != null -> Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(activePack.title, style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onExitPack) { Text("Défis du jour") }
            }
            state.availablePacks.isNotEmpty() -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Packs thématiques", style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.availablePacks, key = { it.id }) { pack ->
                        val locked = pack.isPremiumOnly && !state.isPremium
                        AssistChip(
                            onClick = { if (locked) onNavigateToPremium() else onSelectPack(pack) },
                            label = { Text(pack.title) },
                            leadingIcon = if (locked) {
                                { Icon(Icons.Filled.Lock, contentDescription = "Verrouillé (premium)") }
                            } else {
                                null
                            }
                        )
                    }
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
                if (activePack == null) {
                    OutlinedButton(onClick = onRefresh) {
                        Text("Voir d'autres propositions")
                    }
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
    photoPath: String?,
    isCompleting: Boolean,
    onNoteChange: (String) -> Unit,
    onPrepareCameraCapture: () -> Uri,
    onCameraResult: (Boolean) -> Unit,
    onGallerySelected: (Uri?) -> Unit,
    onRemovePhoto: () -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    // TakePicture délègue à l'app caméra externe (pas de permission CAMERA requise
    // côté app) ; PickVisualMedia utilise le sélecteur photo système (aucune
    // permission de stockage requise non plus).
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> onCameraResult(success) }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> onGallerySelected(uri) }

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

        if (photoPath != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LocalPhotoThumbnail(path = photoPath)
                OutlinedButton(onClick = onRemovePhoto, enabled = !isCompleting) {
                    Text("Retirer la photo")
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { cameraLauncher.launch(onPrepareCameraCapture()) },
                    enabled = !isCompleting
                ) { Text("Prendre une photo") }
                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !isCompleting
                ) { Text("Choisir une photo") }
            }
        }

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
