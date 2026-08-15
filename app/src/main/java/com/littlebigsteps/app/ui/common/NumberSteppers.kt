package com.littlebigsteps.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.littlebigsteps.app.R
import com.littlebigsteps.app.ui.theme.PillShape
import kotlinx.datetime.LocalTime

/**
 * Steppers numériques +/-, utilisés à la fois par l'onboarding (fréquence,
 * heure de rappel) et les Paramètres (mêmes réglages, modifiables après
 * coup) — extraits ici pour ne pas dupliquer.
 *
 * Remplace l'ancien sélecteur à roue (ui/common/WheelPickers.kt, défilement +
 * snap) qui a causé deux bugs de scroll incontrôlé de suite (auto-scroll,
 * puis bouton AM/PM invisible) : plus aucune mécanique de scroll/fling/état
 * de défilement ici, uniquement du tap — même famille de contrôle que
 * `StepperButton` déjà utilisé sans souci pour la fréquence.
 */

/** Convertit une heure 12h + AM/PM en heure 24h (`hour12` toujours 1..12). */
fun to24Hour(hour12: Int, isPm: Boolean): Int = when {
    hour12 == 12 && !isPm -> 0
    hour12 == 12 -> 12
    isPm -> hour12 + 12
    else -> hour12
}

/** Bouton rond +/-, désactivé (grisé, non cliquable) aux bornes. */
@Composable
fun StepperButton(icon: ImageVector, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (enabled) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            }
        )
    }
}

/** Stepper cyclique (revient au minimum après le maximum et vice-versa) sur
 *  une liste de valeurs discrètes — adapté aux heures/minutes qui n'ont pas
 *  de borne naturelle (23h + 1 = 0h, pas un bouton désactivé). */
@Composable
private fun CyclicStepper(value: Int, values: List<Int>, format: (Int) -> String, onChange: (Int) -> Unit) {
    val index = values.indexOf(value).coerceAtLeast(0)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        StepperButton(icon = Icons.Filled.Remove, enabled = true) {
            onChange(values[(index - 1 + values.size) % values.size])
        }
        Text(
            format(value),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.Center
        )
        StepperButton(icon = Icons.Filled.Add, enabled = true) {
            onChange(values[(index + 1) % values.size])
        }
    }
}

/** Toggle AM/PM à deux segments horizontaux — autonome, ne dépend d'aucun
 *  élément voisin pour sa largeur (contrairement à l'ancien `AmPmToggle`,
 *  dont la mise en page dépendait d'un hack IntrinsicSize sur le wheel
 *  d'à côté). */
@Composable
private fun AmPmSegmentedToggle(isPm: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .clip(PillShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(3.dp)
    ) {
        AmPmSegment(stringResource(R.string.am_label), selected = !isPm, onClick = { onChange(false) })
        AmPmSegment(stringResource(R.string.pm_label), selected = isPm, onClick = { onChange(true) })
    }
}

@Composable
private fun AmPmSegment(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(PillShape)
            .then(if (selected) Modifier.background(MaterialTheme.colorScheme.primary) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Sélecteur d'heure de rappel complet (heure/minute/AM-PM), partagé entre
 *  l'onboarding et les Paramètres. Minute par pas de 5 ; le stepper heure
 *  raisonne toujours sur la minute affichée (arrondie), pour ne jamais
 *  enregistrer une minute différente de celle montrée à l'écran. */
@Composable
fun TimeStepperPicker(time: LocalTime, onSelect: (LocalTime) -> Unit) {
    val hour12 = when (val h = time.hour % 12) {
        0 -> 12
        else -> h
    }
    val isPm = time.hour >= 12
    val roundedMinute = (time.minute / 5) * 5

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CyclicStepper(
                value = hour12,
                values = (1..12).toList(),
                format = { "%02d".format(it) },
                onChange = { newHour12 -> onSelect(LocalTime(to24Hour(newHour12, isPm), roundedMinute)) }
            )
            Text(":", style = MaterialTheme.typography.headlineSmall)
            CyclicStepper(
                value = roundedMinute,
                values = (0..55 step 5).toList(),
                format = { "%02d".format(it) },
                onChange = { minute -> onSelect(LocalTime(time.hour, minute)) }
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        AmPmSegmentedToggle(
            isPm = isPm,
            onChange = { newIsPm -> onSelect(LocalTime(to24Hour(hour12, newIsPm), roundedMinute)) }
        )
    }
}
