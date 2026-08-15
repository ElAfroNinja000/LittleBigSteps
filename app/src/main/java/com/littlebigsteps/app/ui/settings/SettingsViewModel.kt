package com.littlebigsteps.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlebigsteps.app.data.repository.ChallengeRepository
import com.littlebigsteps.app.data.repository.ProgressRepository
import com.littlebigsteps.app.data.repository.UserPreferencesRepository
import com.littlebigsteps.app.domain.model.Frequency
import com.littlebigsteps.app.notification.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime

/**
 * Réglages modifiables après l'onboarding : fréquence/heure de rappel (mêmes
 * valeurs qu'à l'onboarding, juste éditables ensuite), interrupteur rappels,
 * lien de gestion de l'abonnement premium, réinitialisation de la
 * progression. La langue n'est pas gérée ici : elle
 * passe par AppCompatDelegate (OS), pas par UserPreferencesRepository — voir
 * SettingsScreen.
 */
class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val progressRepository: ProgressRepository,
    private val challengeRepository: ChallengeRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.observePreferences().collect { prefs ->
                if (prefs == null) return@collect
                _uiState.value = _uiState.value.copy(
                    frequency = prefs.reminderFrequency,
                    reminderTime = prefs.reminderTime,
                    notificationsEnabled = prefs.notificationsEnabled,
                    isPremium = prefs.isPremium,
                    isLoading = false
                )
            }
        }
    }

    fun openDialog(dialog: SettingsDialog) {
        _uiState.value = _uiState.value.copy(dialog = dialog)
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(dialog = null)
    }

    fun updateFrequency(timesPerWeek: Int) {
        val frequency = Frequency(timesPerWeek)
        _uiState.value = _uiState.value.copy(frequency = frequency)
        viewModelScope.launch {
            userPreferencesRepository.updateReminderFrequency(frequency)
            rescheduleIfEnabled(frequency, _uiState.value.reminderTime)
        }
    }

    fun updateReminderTime(time: LocalTime) {
        _uiState.value = _uiState.value.copy(reminderTime = time)
        viewModelScope.launch {
            userPreferencesRepository.updateReminderTime(time)
            rescheduleIfEnabled(_uiState.value.frequency, time)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
        viewModelScope.launch {
            userPreferencesRepository.setNotificationsEnabled(enabled)
            if (enabled) {
                notificationScheduler.scheduleReminders(_uiState.value.frequency, _uiState.value.reminderTime)
            } else {
                notificationScheduler.cancelReminders()
            }
        }
    }

    /** Fréquence/heure changées : ne reprogramme que si les rappels sont
     *  actifs — sinon on écraserait un "coupé" explicite par une reprogrammation. */
    private fun rescheduleIfEnabled(frequency: Frequency, time: LocalTime) {
        if (_uiState.value.notificationsEnabled) {
            notificationScheduler.scheduleReminders(frequency, time)
        }
    }

    /** Confirmé depuis la popup de confirmation (action irréversible, jamais
     *  déclenchée directement au tap sur la ligne). */
    fun confirmResetProgress() {
        viewModelScope.launch {
            progressRepository.resetProgress()
            challengeRepository.clearHistory()
            _uiState.value = _uiState.value.copy(dialog = null)
        }
    }
}
