package com.littlebigsteps.app.ui.progress

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlebigsteps.app.analytics.AnalyticsTracker
import com.littlebigsteps.app.data.repository.ChallengeRepository
import com.littlebigsteps.app.data.repository.ProgressRepository
import com.littlebigsteps.app.data.repository.UserPreferencesRepository
import com.littlebigsteps.app.export.ExportFormat
import com.littlebigsteps.app.export.ProgressExportGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Streak global + XP/niveau par médium (CLAUDE.md §4), simple lecture réactive
 * de ProgressRepository. Gère aussi l'export image/PDF de ce même résumé (§4, §6)
 * et l'affichage des badges premium (§7).
 */
class ProgressViewModel(
    private val progressRepository: ProgressRepository,
    private val challengeRepository: ChallengeRepository,
    private val exportGenerator: ProgressExportGenerator,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                progressRepository.observeGlobalProgress(),
                progressRepository.observeAllMediumProgress(),
                progressRepository.observeUnlockedBadges(),
                userPreferencesRepository.observePreferences()
            ) { global, mediums, badges, prefs ->
                ProgressUiState(
                    currentStreak = global?.currentStreak ?: 0,
                    longestStreak = global?.longestStreak ?: 0,
                    totalChallengesCompleted = global?.totalChallengesCompleted ?: 0,
                    mediumProgress = mediums.sortedBy { it.mediumType.ordinal },
                    unlockedBadges = badges.map { it.badge }.toSet(),
                    isPremium = prefs?.isPremium ?: false,
                    isLoading = false
                )
            }.collect { _uiState.value = it }
        }
    }

    /** Régénère le résumé (streak, niveaux, derniers souvenirs) et renvoie un
     *  URI content:// prêt à être partagé (voir ProgressScreen). */
    suspend fun exportSummary(format: ExportFormat): Uri {
        val global = progressRepository.observeGlobalProgress().first()
        val mediums = progressRepository.observeAllMediumProgress().first()
        val souvenirs = challengeRepository.observePortfolio().first()
            .filter { it.completion.souvenirNote != null }
            .take(5)

        val uri = when (format) {
            ExportFormat.IMAGE -> exportGenerator.exportAsImage(global, mediums, souvenirs)
            ExportFormat.PDF -> exportGenerator.exportAsPdf(global, mediums, souvenirs)
        }
        analyticsTracker.trackExport(format.name)
        return uri
    }
}
