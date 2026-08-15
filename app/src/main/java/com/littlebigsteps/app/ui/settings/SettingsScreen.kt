package com.littlebigsteps.app.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.littlebigsteps.app.BuildConfig
import com.littlebigsteps.app.LittleBigStepsApplication
import com.littlebigsteps.app.R
import com.littlebigsteps.app.ui.common.MediumTintedPopup
import com.littlebigsteps.app.ui.common.StepperButton
import com.littlebigsteps.app.ui.common.TimeStepperPicker
import com.littlebigsteps.app.ui.common.label
import com.littlebigsteps.app.ui.theme.PillShape
import kotlinx.datetime.LocalTime

/**
 * Réglages accessibles après l'onboarding (option C validée en maquette :
 * liste compacte, icône mint par ligne, pas d'entête de section). La langue
 * est gérée directement ici via AppCompatDelegate (OS), pas par le ViewModel
 * — voir SettingsViewModel pour le reste (fréquence/heure/rappels/premium/
 * analytics/réinitialisation).
 */
@Composable
fun SettingsScreen(factory: SettingsViewModelFactory, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: SettingsViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (state.isLoading) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(14.dp))

        SettingsRow(
            icon = Icons.Filled.Repeat,
            label = stringResource(R.string.settings_frequency_label),
            value = state.frequency.label(),
            onClick = { viewModel.openDialog(SettingsDialog.Frequency) }
        )
        SettingsRow(
            icon = Icons.Filled.Schedule,
            label = stringResource(R.string.settings_reminder_time_label),
            value = state.reminderTime.toDisplayString(),
            onClick = { viewModel.openDialog(SettingsDialog.ReminderTime) }
        )
        SettingsSwitchRow(
            icon = Icons.Filled.NotificationsActive,
            label = stringResource(R.string.settings_notifications_label),
            checked = state.notificationsEnabled,
            onCheckedChange = viewModel::setNotificationsEnabled
        )
        SettingsRow(
            icon = Icons.Filled.Language,
            label = stringResource(R.string.settings_language_label),
            value = currentLanguageLabel(),
            onClick = { viewModel.openDialog(SettingsDialog.Language) }
        )
        if (state.isPremium) {
            SettingsRow(
                icon = Icons.Filled.WorkspacePremium,
                label = stringResource(R.string.settings_manage_subscription),
                value = "↗",
                onClick = { context.startActivity(managePlaySubscriptionIntent()) }
            )
        }
        SettingsRow(
            icon = Icons.Filled.DeleteOutline,
            label = stringResource(R.string.settings_reset_progress),
            value = null,
            destructive = true,
            onClick = { viewModel.openDialog(SettingsDialog.ConfirmReset) }
        )
        SettingsRow(
            icon = Icons.Filled.Info,
            label = stringResource(R.string.settings_version_label),
            value = BuildConfig.VERSION_NAME,
            onClick = null
        )

        Spacer(modifier = Modifier.height(14.dp))
        OutlinedButton(onClick = onBack, shape = PillShape) {
            Text(stringResource(R.string.action_back))
        }
    }

    when (state.dialog) {
        SettingsDialog.Frequency -> FrequencyDialog(
            timesPerWeek = state.frequency.timesPerWeek,
            onSelect = viewModel::updateFrequency,
            onDismiss = viewModel::dismissDialog
        )
        SettingsDialog.ReminderTime -> ReminderTimeDialog(
            time = state.reminderTime,
            onSelect = viewModel::updateReminderTime,
            onDismiss = viewModel::dismissDialog
        )
        SettingsDialog.Language -> LanguageDialog(onDismiss = viewModel::dismissDialog)
        SettingsDialog.ConfirmReset -> ResetProgressDialog(
            onConfirm = viewModel::confirmResetProgress,
            onDismiss = viewModel::dismissDialog
        )
        null -> Unit
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    value: String?,
    destructive: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsIcon(icon, destructive)
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            trailing()
        } else if (value != null) {
            Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsIcon(icon, destructive = false)
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun SettingsIcon(icon: ImageVector, destructive: Boolean) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (destructive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
private fun FrequencyDialog(timesPerWeek: Int, onSelect: (Int) -> Unit, onDismiss: () -> Unit) {
    MediumTintedPopup(accentColor = MaterialTheme.colorScheme.onBackground, onDismiss = onDismiss) {
        Text(stringResource(R.string.settings_frequency_label), style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepperButton(icon = Icons.Filled.Remove, enabled = timesPerWeek > 1, onClick = { onSelect(timesPerWeek - 1) })
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
            StepperButton(icon = Icons.Filled.Add, enabled = timesPerWeek < 7, onClick = { onSelect(timesPerWeek + 1) })
        }
    }
}

@Composable
private fun ReminderTimeDialog(time: LocalTime, onSelect: (LocalTime) -> Unit, onDismiss: () -> Unit) {
    MediumTintedPopup(accentColor = MaterialTheme.colorScheme.onBackground, onDismiss = onDismiss) {
        Text(stringResource(R.string.settings_reminder_time_label), style = MaterialTheme.typography.titleMedium)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            TimeStepperPicker(time = time, onSelect = onSelect)
        }
    }
}

@Composable
private fun LanguageDialog(onDismiss: () -> Unit) {
    val current = AppCompatDelegate.getApplicationLocales()
    val currentTag = if (current.isEmpty) null else current[0]?.language
    val application = LocalContext.current.applicationContext as LittleBigStepsApplication

    // Changer la langue ne suffit pas à traduire les activités : leurs textes
    // viennent du catalogue téléchargé, servi par sous-dossier de langue. On
    // relance donc une synchro, sur la portée process — changer la langue recrée
    // l'Activity et annulerait une synchro portée par cet écran.
    fun applyLanguage(locales: LocaleListCompat) {
        AppCompatDelegate.setApplicationLocales(locales)
        application.syncContent()
        onDismiss()
    }

    MediumTintedPopup(accentColor = MaterialTheme.colorScheme.onBackground, onDismiss = onDismiss) {
        Text(stringResource(R.string.settings_language_label), style = MaterialTheme.typography.titleMedium)
        LanguageOption(
            label = stringResource(R.string.settings_language_system),
            selected = currentTag == null,
            onClick = { applyLanguage(LocaleListCompat.getEmptyLocaleList()) }
        )
        LanguageOption(
            label = stringResource(R.string.settings_language_fr),
            selected = currentTag == "fr",
            onClick = { applyLanguage(LocaleListCompat.forLanguageTags("fr")) }
        )
        LanguageOption(
            label = stringResource(R.string.settings_language_en),
            selected = currentTag == "en",
            onClick = { applyLanguage(LocaleListCompat.forLanguageTags("en")) }
        )
    }
}

@Composable
private fun LanguageOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

/** Confirmation classique (pas MediumTintedPopup) : action destructive et
 *  irréversible, mieux servie par le patron AlertDialog standard que par le
 *  chrome "détail d'activité" du reste de l'app. */
@Composable
private fun ResetProgressDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_reset_confirm_title)) },
        text = { Text(stringResource(R.string.settings_reset_confirm_body)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.settings_reset_confirm_button), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_cancel)) }
        }
    )
}

@Composable
private fun currentLanguageLabel(): String {
    val current = AppCompatDelegate.getApplicationLocales()
    return when (if (current.isEmpty) null else current[0]?.language) {
        "en" -> stringResource(R.string.settings_language_en)
        "fr" -> stringResource(R.string.settings_language_fr)
        else -> stringResource(R.string.settings_language_system)
    }
}

// "premium_subscription" duplique PREMIUM_SUBSCRIPTION_ID (privé) de
// PlayBillingRepository.kt — à garder synchronisé si l'ID change côté Play
// Console (voir CLAUDE.md §13).
private fun managePlaySubscriptionIntent(): Intent {
    val uri = Uri.parse(
        "https://play.google.com/store/account/subscriptions?sku=premium_subscription&package=${BuildConfig.APPLICATION_ID}"
    )
    return Intent(Intent.ACTION_VIEW, uri)
}

/** Format d'affichage cohérent avec les roues heure/minute de l'app (12h + AM/PM). */
private fun LocalTime.toDisplayString(): String {
    val hour12 = when (val h = hour % 12) {
        0 -> 12
        else -> h
    }
    val period = if (hour >= 12) "PM" else "AM"
    return "%02d:%02d %s".format(hour12, minute, period)
}
