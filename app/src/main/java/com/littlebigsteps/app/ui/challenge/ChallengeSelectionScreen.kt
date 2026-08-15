package com.littlebigsteps.app.ui.challenge

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.littlebigsteps.app.BuildConfig
import com.littlebigsteps.app.R
import com.littlebigsteps.app.data.local.entity.ChallengeEntity
import com.littlebigsteps.app.data.local.entity.ChallengePackEntity
import com.littlebigsteps.app.data.local.entity.InProgressChallengeEntity
import com.littlebigsteps.app.domain.GamificationRules
import com.littlebigsteps.app.domain.model.ChallengeStatus
import com.littlebigsteps.app.domain.model.MediumType
import com.littlebigsteps.app.ui.common.LocalPhotoThumbnail
import com.littlebigsteps.app.ui.common.MediumTintedPopup
import com.littlebigsteps.app.ui.common.icon
import com.littlebigsteps.app.ui.common.label
import com.littlebigsteps.app.ui.theme.MediumColorPair
import com.littlebigsteps.app.ui.theme.PillShape
import com.littlebigsteps.app.ui.theme.mediumColors
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

/**
 * Écran cœur du core loop (CLAUDE.md §3.2-3.3) : propose 2-3 nouvelles
 * activités, laisse en choisir plusieurs (elles passent "En cours"), les
 * faire progresser sur une jauge auto-déclarée, puis les finaliser avec un
 * souvenir optionnel via la popup "Bien joué !". Permet aussi de parcourir
 * les packs thématiques/saisonniers premium (§7).
 */
@Composable
fun ChallengeSelectionScreen(
    factory: ChallengeSelectionViewModelFactory,
    onNavigateToPremium: () -> Unit,
    onNavigateToProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ChallengeSelectionViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Finalisation : simple notification plutôt qu'un écran dédié — la liste
    // (déjà rafraîchie par dismissCompletion) reste visible dessous. Clé
    // stable (viewModel) : ne redémarre pas quand lastCompletion repasse à
    // null suite à notre propre appel à dismissCompletion, ce qui annulerait
    // sinon le showSnackbar avant qu'il s'affiche. Contexte capturé hors de
    // l'effet (pas composable à l'intérieur) pour résoudre la chaîne traduite.
    LaunchedEffect(viewModel) {
        viewModel.uiState.collect { current ->
            val completion = current.lastCompletion ?: return@collect
            viewModel.dismissCompletion()
            snackbarHostState.showSnackbar(
                context.getString(R.string.activity_completed_xp, completion.xpEarned)
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Défilement obligatoire : sans lui, dès que le contenu dépasse la
                // hauteur (plusieurs activités en cours + nouvelles + packs), Column
                // rogne la hauteur des lignes restantes alors que aspectRatio(1f) des
                // tuiles dessine quand même son carré — les lignes se chevauchaient.
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            if (state.mediumType == null && !state.isLoading) {
                Text(
                    stringResource(R.string.no_medium_unlocked),
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
            onFinalize = viewModel::openFinalize,
            onOpenTips = viewModel::openTips
        )
        is ChallengeDialog.Tips -> TipsDialog(
            entry = dialog.entry,
            onDismiss = viewModel::closeTips
        )
        is ChallengeDialog.Finalize -> FinalizeDialog(
            entry = dialog.entry,
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

    // Montée de niveau : popup dédiée (confettis) à la place du snackbar XP
    // habituel pour cette complétion (voir finalizeChallenge côté ViewModel).
    state.lastLevelUp?.let { event ->
        LevelUpDialog(
            event = event,
            // Le médium n'a d'intérêt à préciser que si plusieurs sont
            // débloqués (premium) — un seul médium en free rendrait la
            // mention redondante.
            includeMediumName = state.availableMediums.size > 1,
            onDismiss = {
                viewModel.dismissLevelUp()
                onNavigateToProgress()
            }
        )
    }
}

/** Popup de montée de niveau : confettis + félicitations, affichée à la
 *  finalisation d'une activité qui fait passer un médium au niveau
 *  supérieur. Dialog dédiée (pas MediumTintedPopup) pour pouvoir superposer
 *  les confettis derrière la carte dans une même fenêtre, sans double-scrim. */
@Composable
private fun LevelUpDialog(event: LevelUpEvent, includeMediumName: Boolean, onDismiss: () -> Unit) {
    val colors = mediumColors(event.mediumType)
    val party = remember {
        Party(
            speed = 10f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            // 0xFF... : le premier octet est le canal alpha en ARGB, sans lui
            // les particules seraient transparentes (couleurs = palette pastel
            // des médiums, Color.kt).
            colors = listOf(0xFFC0FCD2L, 0xFFDDE7F5L, 0xFFE5DEF8L, 0xFFF8DCDCL, 0xFFF8ECD5L).map { it.toInt() },
            emitter = Emitter(duration = 2, TimeUnit.SECONDS).max(120),
            position = Position.Relative(0.5, 0.3)
        )
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            KonfettiView(modifier = Modifier.fillMaxSize(), parties = listOf(party))
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.background,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        stringResource(R.string.level_up_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onContainer
                    )
                    val message = if (includeMediumName) {
                        stringResource(R.string.level_up_message_with_medium, event.newLevel, event.mediumType.label())
                    } else {
                        stringResource(R.string.level_up_message, event.newLevel)
                    }
                    Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                    MediumFilledButton(
                        text = stringResource(R.string.see_progress_button),
                        colors = colors,
                        onClick = onDismiss
                    )
                }
            }
        }
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
        Text(stringResource(R.string.my_activities_title), style = MaterialTheme.typography.headlineSmall)

        // Le sélecteur de médium n'a d'intérêt qu'une fois plusieurs médiums
        // débloqués (premium) — un seul au départ (modèle freemium, CLAUDE.md §7).
        if (state.availableMediums.size > 1) {
            // Défilement horizontal : à 4 médiums débloqués (premium) la ligne
            // dépasse la largeur de l'écran, et sans lui le dernier chip est
            // compressé jusqu'à casser son libellé caractère par caractère.
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                TextButton(onClick = onExitPack) { Text(stringResource(R.string.my_activities_title)) }
            }
            state.availablePacks.isNotEmpty() -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.packs_thematic_title), style = MaterialTheme.typography.titleMedium)
                // Même défilement que la ligne des médiums : les titres de packs
                // sont longs, deux suffisent à dépasser la largeur.
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.availablePacks.forEach { pack ->
                        val locked = pack.isPremiumOnly && !state.isPremium
                        AssistChip(
                            onClick = { if (locked) onNavigateToPremium() else onSelectPack(pack) },
                            label = { Text(pack.title) },
                            leadingIcon = if (locked) {
                                { Icon(Icons.Filled.Lock, contentDescription = stringResource(R.string.locked_premium_description)) }
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
                    Text(stringResource(R.string.pack_empty), style = MaterialTheme.typography.bodyLarge)
                } else {
                    // Jamais de défi "surprise" dans un pack, déjà curé (voir
                    // ChallengeSelectionViewModel.selectPack).
                    ActivityGrid(state.newOptions, surpriseChallengeId = null) { challenge -> onSelectNewChallenge(challenge) }
                }
            }
            state.newOptions.isEmpty() && state.inProgress.isEmpty() -> Text(
                stringResource(R.string.no_activity_available),
                style = MaterialTheme.typography.bodyLarge
            )
            // "En cours" avant "Nouvelles activités" : ce qu'on a déjà commencé
            // prime sur ce qui reste à découvrir.
            else -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (state.inProgress.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.status_in_progress), style = MaterialTheme.typography.titleSmall)
                        ActivityGrid(
                            challenges = state.inProgress.map { it.challenge },
                            // Le défi surprise choisi garde sa mise en valeur
                            // tant qu'il n'est pas finalisé (persisté en base).
                            surpriseChallengeId = state.inProgress.firstOrNull { it.isSurprise }?.challenge?.id
                        ) { challenge ->
                            state.inProgress.first { it.challenge.id == challenge.id }
                                .let(onSelectInProgress)
                        }
                    }
                }
                if (state.newOptions.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.section_new_activities), style = MaterialTheme.typography.titleSmall)
                        ActivityGrid(
                            challenges = state.newOptions,
                            surpriseChallengeId = state.surpriseChallengeId
                        ) { challenge -> onSelectNewChallenge(challenge) }
                    }
                }
            }
        }
    }
}

/** Grille 2 colonnes de tuiles carrées — même traitement pour une nouvelle
 *  activité ou une activité en cours, aucune pastille ne les distingue (le
 *  regroupement par section suffit). Le défi "surprise" du tirage (s'il y en
 *  a un, voir ChallengeSelectionViewModel.loadOptions) est mis à part sur sa
 *  propre ligne pleine largeur plutôt que dans la trame 2 colonnes. */
@Composable
private fun ActivityGrid(
    challenges: List<ChallengeEntity>,
    surpriseChallengeId: String?,
    onClick: (ChallengeEntity) -> Unit
) {
    val surprise = challenges.firstOrNull { it.id == surpriseChallengeId }
    val rest = challenges.filterNot { it.id == surpriseChallengeId }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        surprise?.let { challenge ->
            SurpriseActivityTile(challenge = challenge, onClick = { onClick(challenge) })
        }
        rest.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { challenge ->
                    ActivityTile(challenge = challenge, onClick = { onClick(challenge) }, modifier = Modifier.weight(1f))
                }
                if (row.size < 2) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

/** Tuile 2x plus large que les tuiles normales, ruban mint "Surprise" en
 *  coin, bonus XP affiché — le défi surprise occasionnel doit se remarquer
 *  au premier coup d'œil plutôt que se fondre dans la grille. */
@Composable
private fun SurpriseActivityTile(challenge: ChallengeEntity, onClick: () -> Unit) {
    val colors = mediumColors(challenge.mediumType)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2.3f)
            .clip(MaterialTheme.shapes.medium)
            .background(colors.container)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = colors.onContainer)
            Column {
                Text(
                    challenge.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onContainer
                )
                Text(
                    stringResource(
                        R.string.surprise_xp_bonus,
                        GamificationRules.XP_PER_COMPLETION + GamificationRules.SURPRISE_XP_BONUS,
                        GamificationRules.XP_PER_COMPLETION
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onContainer
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.small
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                stringResource(R.string.surprise_badge),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

/** Aplat plein du pastel médium, sans bordure ni ombre. Icône affichée
 *  seulement en premium : en free, tout un même médium, l'icône ne
 *  distinguerait rien. */
@Composable
private fun ActivityTile(challenge: ChallengeEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = mediumColors(challenge.mediumType)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.medium)
            .background(colors.container)
            .clickable(onClick = onClick)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        if (BuildConfig.FORCE_PREMIUM) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(challenge.mediumType.icon(), contentDescription = null, tint = colors.onContainer)
                Text(
                    challenge.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onContainer,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            Text(
                challenge.title,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onContainer,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/** Bouton monochrome (encre du médium en fond, pastel en texte) commun aux
 *  popups d'activité — "Choisir" et "Finaliser". */
@Composable
private fun MediumFilledButton(text: String, colors: MediumColorPair, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = PillShape,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 9.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.onContainer,
            contentColor = colors.container
        )
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun NewChallengeDialog(challenge: ChallengeEntity, onDismiss: () -> Unit, onChoose: () -> Unit) {
    val colors = mediumColors(challenge.mediumType)
    MediumTintedPopup(accentColor = colors.onContainer, onDismiss = onDismiss) {
        Text(challenge.title, style = MaterialTheme.typography.titleMedium, color = colors.onContainer)
        Text(challenge.description, style = MaterialTheme.typography.bodyLarge)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            MediumFilledButton(text = stringResource(R.string.choose_button), colors = colors, onClick = onChoose)
        }
    }
}

@Composable
private fun InProgressDialog(
    entry: InProgressChallengeEntity,
    onDismiss: () -> Unit,
    onStatusChange: (ChallengeStatus) -> Unit,
    onFinalize: () -> Unit,
    onOpenTips: () -> Unit
) {
    val colors = mediumColors(entry.challenge.mediumType)
    val hasTips = !entry.challenge.tips.isNullOrEmpty()
    MediumTintedPopup(accentColor = colors.onContainer, onDismiss = onDismiss) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                entry.challenge.title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.onContainer,
                modifier = Modifier.weight(1f)
            )
            if (hasTips) {
                IconButton(onClick = onOpenTips) {
                    Icon(
                        imageVector = Icons.Filled.Lightbulb,
                        contentDescription = stringResource(R.string.tips_button_description),
                        tint = colors.onContainer
                    )
                }
            }
        }
        Text(entry.challenge.description, style = MaterialTheme.typography.bodyLarge)
        SegmentedProgressControl(status = entry.status, colors = colors, onSelect = onStatusChange)
        if (entry.status == ChallengeStatus.DONE) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                MediumFilledButton(text = stringResource(R.string.finalize_button), colors = colors, onClick = onFinalize)
            }
        }
    }
}

/** Popup "Conseils" (option C retenue) : liste à puces façon checklist, une
 *  icône check par conseil. Accessible uniquement depuis la popup "En cours"
 *  via l'icône ampoule (voir InProgressDialog), seulement si des conseils ont
 *  été rédigés pour ce défi (CLAUDE.md §5 — contenu éditorial, écrit par
 *  l'utilisateur lui-même, jamais généré). */
@Composable
private fun TipsDialog(entry: InProgressChallengeEntity, onDismiss: () -> Unit) {
    val colors = mediumColors(entry.challenge.mediumType)
    MediumTintedPopup(accentColor = colors.onContainer, onDismiss = onDismiss) {
        Text(stringResource(R.string.tips_title), style = MaterialTheme.typography.titleMedium, color = colors.onContainer)
        entry.challenge.tips.orEmpty().forEach { tip ->
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = colors.onContainer,
                    modifier = Modifier.padding(top = 2.dp, end = 8.dp)
                )
                Text(tip, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

/** Contrôle segmenté à 3 cases tappables (auto-déclaré, CLAUDE.md §9 —
 *  aucune vérification) : plus explicitement "boutons" que la jauge à
 *  pastilles qu'il remplace. Petit bounce d'échelle + vibration légère sur la
 *  case "Terminé" au moment précis où elle est atteinte par un tap (jamais
 *  rejoué en rouvrant une activité déjà terminée, voir previousStatus). */
@Composable
private fun SegmentedProgressControl(status: ChallengeStatus, colors: MediumColorPair, onSelect: (ChallengeStatus) -> Unit) {
    val haptics = LocalHapticFeedback.current
    var previousStatus by remember { mutableStateOf(status) }
    val doneScale = remember { Animatable(1f) }

    LaunchedEffect(status) {
        if (status == ChallengeStatus.DONE && previousStatus != ChallengeStatus.DONE) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            doneScale.animateTo(1.18f, animationSpec = tween(140))
            doneScale.animateTo(1f, animationSpec = tween(200))
        }
        previousStatus = status
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.onContainer.copy(alpha = 0.1f))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        ChallengeStatus.entries.forEach { stage ->
            val reached = stage.ordinal <= status.ordinal
            Box(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (stage == ChallengeStatus.DONE) {
                            Modifier.scale(doneScale.value)
                        } else {
                            Modifier
                        }
                    )
                    .clip(MaterialTheme.shapes.small)
                    .then(if (reached) Modifier.background(colors.onContainer) else Modifier)
                    .clickable { onSelect(stage) }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stage.label(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (stage == status) FontWeight.Bold else FontWeight.Normal,
                    color = if (reached) colors.container else colors.onContainer,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FinalizeDialog(
    entry: InProgressChallengeEntity,
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
    val colors = mediumColors(entry.challenge.mediumType)
    // TakePicture délègue à l'app caméra externe (pas de permission CAMERA requise
    // côté app).
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> onCameraResult(success) }

    MediumTintedPopup(accentColor = colors.onContainer, onDismiss = onDismiss) {
        Text(stringResource(R.string.well_done_title), style = MaterialTheme.typography.titleMedium, color = colors.onContainer)
        Text(
            stringResource(R.string.well_done_body),
            style = MaterialTheme.typography.bodyLarge
        )

        if (photoPath != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LocalPhotoThumbnail(path = photoPath)
                OutlinedButton(onClick = onRemovePhoto, enabled = !isCompleting, shape = PillShape) {
                    Text(stringResource(R.string.remove_photo_button))
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .border(1.5.dp, colors.onContainer, MaterialTheme.shapes.medium)
                    .clickable(enabled = !isCompleting) { cameraLauncher.launch(onPrepareCameraCapture()) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.AddAPhoto,
                    contentDescription = stringResource(R.string.take_photo_description),
                    tint = colors.onContainer
                )
            }
        }

        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            label = { Text(stringResource(R.string.comment_label)) },
            // Le troncage réel a lieu dans le ViewModel (updateSouvenirNote) —
            // ce compteur n'est qu'un retour visuel.
            supportingText = { Text(stringResource(R.string.souvenir_char_count, note.length, SOUVENIR_NOTE_MAX_LENGTH)) },
            modifier = Modifier.fillMaxWidth()
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            // Photo obligatoire pour finaliser (le commentaire texte reste
            // optionnel) — consigne explicite, déroge à l'optionalité totale
            // du souvenir décrite en CLAUDE.md §3.3.
            MediumFilledButton(
                text = stringResource(R.string.finalize_button),
                colors = colors,
                enabled = !isCompleting && photoPath != null,
                onClick = onFinalize
            )
        }
    }
}
