package com.littlebigsteps.app.ui.challenge

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.littlebigsteps.app.data.local.entity.ChallengeEntity
import com.littlebigsteps.app.data.local.entity.ChallengePackEntity
import com.littlebigsteps.app.data.local.entity.InProgressChallengeEntity
import com.littlebigsteps.app.domain.model.ChallengeStatus
import com.littlebigsteps.app.domain.model.MediumType
import com.littlebigsteps.app.ui.common.LocalPhotoThumbnail
import com.littlebigsteps.app.ui.common.label
import com.littlebigsteps.app.ui.theme.PillShape
import com.littlebigsteps.app.ui.theme.mediumColors

/**
 * Écran cœur du core loop (CLAUDE.md §3.2-3.3) : propose 2-3 nouvelles
 * activités, laisse en choisir une (elle passe "En cours"), la faire
 * progresser sur une jauge auto-déclarée, puis la finaliser avec un souvenir
 * optionnel via la popup "Bien joué !". Permet aussi de parcourir les packs
 * thématiques/saisonniers premium (§7).
 */
@Composable
fun ChallengeSelectionScreen(
    factory: ChallengeSelectionViewModelFactory,
    onNavigateToPremium: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ChallengeSelectionViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Finalisation : simple notification "+XX XP" plutôt qu'un écran dédié —
    // la liste (déjà rafraîchie par dismissCompletion) reste visible dessous.
    // Clé stable (viewModel) : ne redémarre pas quand lastCompletion repasse à
    // null suite à notre propre appel à dismissCompletion, ce qui annulerait
    // sinon le showSnackbar avant qu'il s'affiche.
    LaunchedEffect(viewModel) {
        viewModel.uiState.collect { current ->
            val completion = current.lastCompletion ?: return@collect
            viewModel.dismissCompletion()
            snackbarHostState.showSnackbar("+${completion.xpEarned} XP")
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            if (state.mediumType == null && !state.isLoading) {
                Text(
                    "Aucun médium débloqué pour l'instant — termine l'onboarding pour commencer.",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                ChallengeOptionsList(
                    state = state,
                    onSelectMedium = viewModel::selectMedium,
                    onSelectNewChallenge = viewModel::selectNewChallenge,
                    onSelectInProgress = viewModel::selectInProgress,
                    onSelectPack = viewModel::selectPack,
                    onExitPack = viewModel::exitPack,
                    onNavigateToPremium = onNavigateToPremium
                )
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    when (val dialog = state.dialog) {
        is ChallengeDialog.NewChallenge -> NewChallengeDialog(
            challenge = dialog.challenge,
            onDismiss = viewModel::dismissDialog,
            onChoose = viewModel::chooseNewChallenge
        )
        is ChallengeDialog.InProgress -> InProgressDialog(
            entry = dialog.entry,
            onDismiss = viewModel::dismissDialog,
            onStatusChange = viewModel::setInProgressStatus,
            onFinalize = viewModel::openFinalize
        )
        is ChallengeDialog.Finalize -> FinalizeDialog(
            note = state.souvenirNote,
            photoPath = state.souvenirPhotoPath,
            isCompleting = state.isCompleting,
            onNoteChange = viewModel::updateSouvenirNote,
            onPrepareCameraCapture = viewModel::prepareCameraCapture,
            onCameraResult = viewModel::onCameraResult,
            onRemovePhoto = viewModel::removeSouvenirPhoto,
            onFinalize = viewModel::finalizeChallenge,
            onDismiss = viewModel::dismissDialog
        )
        null -> Unit
    }
}

@Composable
private fun ChallengeOptionsList(
    state: ChallengeSelectionUiState,
    onSelectMedium: (MediumType) -> Unit,
    onSelectNewChallenge: (ChallengeEntity) -> Unit,
    onSelectInProgress: (InProgressChallengeEntity) -> Unit,
    onSelectPack: (ChallengePackEntity) -> Unit,
    onExitPack: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Mes activités", style = MaterialTheme.typography.headlineSmall)

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
                TextButton(onClick = onExitPack) { Text("Mes activités") }
            }
            state.availablePacks.isNotEmpty() -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Packs thématiques", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.availablePacks.forEach { pack ->
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
            activePack != null -> {
                if (state.newOptions.isEmpty()) {
                    Text("Aucune activité dans ce pack.", style = MaterialTheme.typography.bodyLarge)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.newOptions.forEach { challenge ->
                            NewActivityCard(challenge = challenge, onClick = { onSelectNewChallenge(challenge) })
                        }
                    }
                }
            }
            state.newOptions.isEmpty() && state.inProgress.isEmpty() -> Text(
                "Aucune activité disponible pour ce médium pour l'instant.",
                style = MaterialTheme.typography.bodyLarge
            )
            else -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (state.newOptions.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Nouvelles activités", style = MaterialTheme.typography.titleSmall)
                        state.newOptions.forEach { challenge ->
                            NewActivityCard(challenge = challenge, onClick = { onSelectNewChallenge(challenge) })
                        }
                    }
                }
                if (state.inProgress.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("En cours", style = MaterialTheme.typography.titleSmall)
                        state.inProgress.forEach { entry ->
                            InProgressActivityCard(entry = entry, onClick = { onSelectInProgress(entry) })
                        }
                    }
                }
            }
        }
    }
}

/** Nouvelle proposition : aplat pastel de son médium, sans bordure ni ombre —
 *  on repère le médium à la couleur avant même de lire le titre. */
@Composable
private fun NewActivityCard(challenge: ChallengeEntity, onClick: () -> Unit) {
    val colors = mediumColors(challenge.mediumType)
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = colors.container,
        contentColor = colors.onContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            challenge.title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/** Activité en cours : surface neutre (elle n'est plus une "proposition") avec
 *  une pastille d'état, pour la distinguer au premier coup d'œil des pastels. */
@Composable
private fun InProgressActivityCard(entry: InProgressChallengeEntity, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(entry.challenge.title, style = MaterialTheme.typography.bodyLarge)
            Surface(
                shape = PillShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text(
                    entry.status.label(),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/** Popup flottante (fond assombri géré par [Dialog]) commune aux trois états
 *  de détail d'activité — nouvelle, en cours, finalisation. */
@Composable
private fun ChallengePopup(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), tonalElevation = 4.dp) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun NewChallengeDialog(challenge: ChallengeEntity, onDismiss: () -> Unit, onChoose: () -> Unit) {
    ChallengePopup(onDismiss = onDismiss) {
        Text(challenge.title, style = MaterialTheme.typography.titleMedium)
        Text(challenge.description, style = MaterialTheme.typography.bodyLarge)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(onClick = onDismiss, shape = PillShape) { Text("Retour") }
            Button(onClick = onChoose, shape = PillShape) { Text("Choisir") }
        }
    }
}

@Composable
private fun InProgressDialog(
    entry: InProgressChallengeEntity,
    onDismiss: () -> Unit,
    onStatusChange: (ChallengeStatus) -> Unit,
    onFinalize: () -> Unit
) {
    ChallengePopup(onDismiss = onDismiss) {
        Text(entry.challenge.title, style = MaterialTheme.typography.titleMedium)
        Text(entry.challenge.description, style = MaterialTheme.typography.bodyLarge)
        StatusGauge(status = entry.status, onSelect = onStatusChange)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(onClick = onDismiss, shape = PillShape) { Text("Retour") }
            if (entry.status == ChallengeStatus.DONE) {
                Button(onClick = onFinalize, shape = PillShape) { Text("Finaliser") }
            }
        }
    }
}

/** Jauge à 3 étapes tappables (auto-déclaré, CLAUDE.md §9 — aucune vérification).
 *  Colonnes de largeur fixe pour que les pastilles restent parfaitement
 *  alignées avec les segments de liaison, quel que soit l'état actif. */
@Composable
private fun StatusGauge(status: ChallengeStatus, onSelect: (ChallengeStatus) -> Unit) {
    val stages = ChallengeStatus.entries
    val stageWidth = 72.dp
    val dotSize = 14.dp

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        stages.forEachIndexed { index, stage ->
            val reached = stage.ordinal <= status.ordinal
            Column(
                modifier = Modifier
                    .width(stageWidth)
                    .clickable { onSelect(stage) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(if (reached) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .then(
                            if (reached) {
                                Modifier
                            } else {
                                Modifier.border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            }
                        )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    stage.label(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (stage == status) FontWeight.Bold else FontWeight.Normal,
                    color = if (reached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (index < stages.lastIndex) {
                val segmentReached = stage.ordinal < status.ordinal
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(dotSize),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                if (segmentReached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun FinalizeDialog(
    note: String,
    photoPath: String?,
    isCompleting: Boolean,
    onNoteChange: (String) -> Unit,
    onPrepareCameraCapture: () -> Uri,
    onCameraResult: (Boolean) -> Unit,
    onRemovePhoto: () -> Unit,
    onFinalize: () -> Unit,
    onDismiss: () -> Unit
) {
    // TakePicture délègue à l'app caméra externe (pas de permission CAMERA requise
    // côté app).
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> onCameraResult(success) }

    ChallengePopup(onDismiss = onDismiss) {
        Text("Bien joué !", style = MaterialTheme.typography.titleMedium)
        Text(
            "Un souvenir de cette activité, si tu veux le garder.",
            style = MaterialTheme.typography.bodyLarge
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
            OutlinedButton(
                onClick = { cameraLauncher.launch(onPrepareCameraCapture()) },
                enabled = !isCompleting
            ) { Text("Prendre une photo") }
        }

        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            label = { Text("Légende (optionnelle)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onFinalize,
            enabled = !isCompleting,
            shape = PillShape,
            contentPadding = PaddingValues(vertical = 14.dp),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Finaliser", style = MaterialTheme.typography.labelLarge) }
    }
}
