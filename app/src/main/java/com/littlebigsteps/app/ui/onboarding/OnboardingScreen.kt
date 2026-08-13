package com.littlebigsteps.app.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.littlebigsteps.app.BuildConfig
import com.littlebigsteps.app.domain.model.MediumType
import com.littlebigsteps.app.ui.common.label
import com.littlebigsteps.app.ui.theme.InkOnDarkSecondary
import com.littlebigsteps.app.ui.theme.PillShape
import kotlinx.coroutines.flow.collect
import kotlinx.datetime.LocalTime
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

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
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            when (state.step) {
                OnboardingStep.WELCOME -> Unit // géré plus haut, jamais atteint ici
                OnboardingStep.MEDIUM_CHOICE -> MediumChoiceStep(state, viewModel::toggleMedium)
                OnboardingStep.FREE_MEDIUM_CHOICE -> FreeMediumChoiceStep(state, viewModel::selectFreeMedium)
                OnboardingStep.FREQUENCY_CHOICE -> FrequencyChoiceStep(state, viewModel::selectFrequency)
                OnboardingStep.REMINDER_HOUR_CHOICE -> ReminderHourChoiceStep(state, viewModel::selectReminderTime)
                OnboardingStep.REMINDER_MINUTE_CHOICE -> ReminderMinuteChoiceStep(state, viewModel::selectReminderTime)
                OnboardingStep.DONE -> Unit
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = viewModel::goToPreviousStep,
                enabled = state.step != OnboardingStep.MEDIUM_CHOICE
            ) {
                Text("Retour")
            }
            Button(
                onClick = viewModel::goToNextStep,
                enabled = state.canContinue && !state.isSaving
            ) {
                Text(if (state.step == OnboardingStep.REMINDER_MINUTE_CHOICE) "Terminer" else "Suivant")
            }
        }
    }
}

/** Seul écran sur fond navy plein (avec l'écran de fin d'activité) : c'est le
 *  moment "hero" de la DA, il tranche volontairement avec le fond clair du
 *  reste du parcours. */
@Composable
private fun WelcomeStep(modifier: Modifier = Modifier, onContinue: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondary)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                "Bienvenue sur LittleBigSteps !",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Chaque jour, on te propose une ou plusieurs petites activités " +
                    "créatives : dessin, photo, écriture et bricolage, à faire à " +
                    "ton rythme, sans pression ni compte à créer. Complète-les au " +
                    "fur et à mesure pour voir ton portfolio grandir au fil de tes " +
                    "créations !",
                style = MaterialTheme.typography.bodyLarge,
                color = InkOnDarkSecondary,
                textAlign = TextAlign.Center
            )
        }
        Button(
            onClick = onContinue,
            shape = PillShape,
            contentPadding = PaddingValues(vertical = 14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("C'est parti !", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun MediumChoiceStep(state: OnboardingUiState, onToggle: (MediumType) -> Unit) {
    Text("Qu'est-ce qui t'intéresse ?", style = MaterialTheme.typography.titleMedium)
    MediumType.entries.forEach { medium ->
        OnboardingOption(
            label = medium.label(),
            selected = medium in state.selectedMediums,
            onClick = { onToggle(medium) },
            multiSelect = BuildConfig.FORCE_PREMIUM
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
private fun FrequencyChoiceStep(state: OnboardingUiState, onSelect: (Int) -> Unit) {
    Text("Combien de fois par semaines ?", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(16.dp))
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalWheelPicker(
            values = (1..7).toList(),
            selected = state.frequency.timesPerWeek,
            onSelect = onSelect,
            modifier = Modifier.fillMaxWidth()
        )
        Icon(
            Icons.Filled.KeyboardArrowUp,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
            Text("${state.frequency.timesPerWeek}", style = MaterialTheme.typography.headlineMedium)
            Text("fois", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private val WHEEL_ITEM_WIDTH = 56.dp
private const val WHEEL_VISIBLE_COLUMNS = 5

/** Roue de sélection horizontale : défilement libre avec un aimant qui aligne
 *  automatiquement la valeur la plus proche du centre dès que l'utilisateur
 *  relâche (snap-to-nearest), en plus du tap direct sur une valeur visible. */
@Composable
private fun HorizontalWheelPicker(
    values: List<Int>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(listState)

    // Recentre la roue quand la sélection change depuis l'extérieur (tap direct).
    LaunchedEffect(selected) {
        val targetIndex = values.indexOf(selected).coerceAtLeast(0)
        listState.animateScrollToItem(targetIndex)
    }

    // Snap-to-nearest au relâchement : dès que le défilement s'arrête, la
    // valeur la plus proche du centre du viewport devient la sélection.
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.collect { scrolling ->
            if (scrolling) return@collect
            val viewportCenter = listState.layoutInfo.viewportSize.width / 2
            val centerItem = listState.layoutInfo.visibleItemsInfo.minByOrNull { info ->
                abs((info.offset + info.size / 2) - viewportCenter)
            }
            val value = centerItem?.let { values.getOrNull(it.index) }
            if (value != null && value != selected) onSelect(value)
        }
    }

    LazyRow(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = modifier
            .height(64.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentPadding = PaddingValues(horizontal = WHEEL_ITEM_WIDTH * (WHEEL_VISIBLE_COLUMNS / 2)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(values) { value ->
            val isSelected = value == selected
            Box(
                modifier = Modifier
                    .width(WHEEL_ITEM_WIDTH)
                    .fillMaxHeight()
                    .clickable { onSelect(value) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    value.toString(),
                    style = if (isSelected) {
                        MaterialTheme.typography.headlineSmall
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                    }
                )
            }
        }
    }
}

@Composable
private fun ReminderHourChoiceStep(state: OnboardingUiState, onSelect: (LocalTime) -> Unit) {
    val time = state.reminderTime
    val hour12 = when (val h = time.hour % 12) {
        0 -> 12
        else -> h
    }
    val isPm = time.hour >= 12

    Text("À quelle heure de la journée ?", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(16.dp))

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append("%02d".format(hour12))
                    }
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                        append(":%02d".format(time.minute))
                    }
                },
                style = MaterialTheme.typography.headlineMedium
            )
            AmPmToggle(
                isPm = isPm,
                onChange = { newIsPm -> onSelect(LocalTime(to24Hour(hour12, newIsPm), time.minute)) }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        ClockDialPicker(
            values = (1..12).toList(),
            selected = hour12,
            onSelect = { newHour12 -> onSelect(LocalTime(to24Hour(newHour12, isPm), time.minute)) }
        )
    }
}

@Composable
private fun ReminderMinuteChoiceStep(state: OnboardingUiState, onSelect: (LocalTime) -> Unit) {
    val time = state.reminderTime
    val hour12 = when (val h = time.hour % 12) {
        0 -> 12
        else -> h
    }
    val roundedMinute = (time.minute / 5) * 5

    Text("À quelle heure de la journée ?", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(16.dp))

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                    append("%02d:".format(hour12))
                }
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("%02d".format(roundedMinute))
                }
            },
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        ClockDialPicker(
            values = (0..55 step 5).toList(),
            selected = roundedMinute,
            format = { "%02d".format(it) },
            onSelect = { minute -> onSelect(LocalTime(time.hour, minute)) }
        )
    }
}

/** Convertit une heure 12h + AM/PM en heure 24h (`hour12` toujours 1..12). */
private fun to24Hour(hour12: Int, isPm: Boolean): Int = when {
    hour12 == 12 && !isPm -> 0
    hour12 == 12 -> 12
    isPm -> hour12 + 12
    else -> hour12
}

@Composable
private fun AmPmToggle(isPm: Boolean, onChange: (Boolean) -> Unit) {
    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
    ) {
        AmPmOption(label = "AM", selected = !isPm, onClick = { onChange(false) })
        AmPmOption(label = "PM", selected = isPm, onClick = { onChange(true) })
    }
}

@Composable
private fun AmPmOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private val CLOCK_DIAL_SIZE = 220.dp
private val CLOCK_LABEL_RADIUS = 82.dp
private val CLOCK_LABEL_SIZE = 32.dp

/** Cadran circulaire (façon horloge Android) : `values` réparties à intervalle
 *  angulaire égal, aiguille + pastille pleine sur la valeur sélectionnée,
 *  chaque valeur tappable directement. */
@Composable
private fun ClockDialPicker(
    values: List<Int>,
    selected: Int,
    onSelect: (Int) -> Unit,
    format: (Int) -> String = Int::toString,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val selectedIndex = values.indexOf(selected).coerceAtLeast(0)
    val angleStep = 360f / values.size
    val labelRadiusPx = with(density) { CLOCK_LABEL_RADIUS.toPx() }
    val handColor = MaterialTheme.colorScheme.primary
    val dialColor = MaterialTheme.colorScheme.surfaceVariant

    Box(modifier = modifier.size(CLOCK_DIAL_SIZE), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = dialColor)
            val angleRad = Math.toRadians((selectedIndex * angleStep - 90).toDouble())
            val end = Offset(
                center.x + (labelRadiusPx * cos(angleRad)).toFloat(),
                center.y + (labelRadiusPx * sin(angleRad)).toFloat()
            )
            drawLine(color = handColor, start = center, end = end, strokeWidth = 4f)
            drawCircle(color = handColor, radius = 6f, center = center)
        }

        values.forEachIndexed { index, value ->
            val angleRad = Math.toRadians((index * angleStep - 90).toDouble())
            val x = (labelRadiusPx * cos(angleRad)).roundToInt()
            val y = (labelRadiusPx * sin(angleRad)).roundToInt()
            val isSelected = value == selected
            Box(
                modifier = Modifier
                    .offset { IntOffset(x, y) }
                    .size(CLOCK_LABEL_SIZE)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelect(value) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    format(value),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
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
