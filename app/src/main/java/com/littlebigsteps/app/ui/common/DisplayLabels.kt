package com.littlebigsteps.app.ui.common

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.littlebigsteps.app.R
import com.littlebigsteps.app.domain.model.ChallengeStatus
import com.littlebigsteps.app.domain.model.Frequency
import com.littlebigsteps.app.domain.model.MediumType

/** Libellés d'affichage, centralisés pour que chaque écran ne les redéfinisse
 *  pas — traduits (fr/en, voir res/values{,-en}/strings.xml), résolus selon la
 *  langue système. [MediumType.label] a deux variantes : une composable pour
 *  l'UI Compose, une avec [Context] explicite pour les contextes non-Compose
 *  (ReminderWorker, ExportRenderer — Canvas natif). */
@Composable
fun MediumType.label(): String = stringResource(labelRes())

fun MediumType.label(context: Context): String = context.getString(labelRes())

private fun MediumType.labelRes(): Int = when (this) {
    MediumType.PHOTO -> R.string.medium_photo
    MediumType.DRAWING -> R.string.medium_drawing
    MediumType.WRITING -> R.string.medium_writing
    MediumType.CRAFT -> R.string.medium_craft
}

/** Icône associée au médium — tuiles de l'onboarding et grille "Mes activités"
 *  (visible seulement en premium, un seul médium en free ne justifie pas
 *  l'icône, voir ChallengeSelectionScreen). */
fun MediumType.icon(): ImageVector = when (this) {
    MediumType.PHOTO -> Icons.Filled.PhotoCamera
    MediumType.DRAWING -> Icons.Filled.Brush
    MediumType.WRITING -> Icons.Filled.Edit
    MediumType.CRAFT -> Icons.Filled.ContentCut
}

@Composable
fun Frequency.label(): String = when (timesPerWeek) {
    7 -> stringResource(R.string.frequency_daily)
    1 -> stringResource(R.string.frequency_weekly_once)
    else -> stringResource(R.string.frequency_weekly_n, timesPerWeek)
}

@Composable
fun ChallengeStatus.label(): String = when (this) {
    ChallengeStatus.DRAFT -> stringResource(R.string.status_draft)
    ChallengeStatus.IN_PROGRESS -> stringResource(R.string.status_in_progress)
    ChallengeStatus.DONE -> stringResource(R.string.status_done)
}
