package com.littlebigsteps.app.ui.progress

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlebigsteps.app.analytics.AnalyticsTracker
import com.littlebigsteps.app.data.local.entity.MediumProgressEntity
import com.littlebigsteps.app.data.repository.ChallengeRepository
import com.littlebigsteps.app.data.repository.ProgressRepository
import com.littlebigsteps.app.data.repository.UserPreferencesRepository
import com.littlebigsteps.app.export.ExportData
import com.littlebigsteps.app.export.ExportFormat
import com.littlebigsteps.app.export.ProgressExportGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Streak global + XP/niveau par médium (CLAUDE.md §4), simple lecture réactive
 * de ProgressRepository. Gère aussi l'export du résumé — image/PDF pour tous,
 * plus de souvenirs/photos pour le premium (§4, §6, §7).
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
                challengeRepository.observePortfolio(),
                userPreferencesRepository.observePreferences()
            ) { global, mediums, portfolio, prefs ->
                // Le médium accessible en free (s'il existe) prime dans l'ordre
                // d'affichage — c'est le seul que l'utilisateur gratuit pratique.
                val freeMedium = prefs?.freeMedium
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val activitiesThisMonth = portfolio.count { entry ->
                    val completedAt = entry.completion.completedAt.toLocalDateTime(TimeZone.currentSystemDefault())
                    completedAt.year == now.year && completedAt.monthNumber == now.monthNumber
                }
                ProgressUiState(
                    currentStreak = global?.currentStreak ?: 0,
                    longestStreak = global?.longestStreak ?: 0,
                    totalChallengesCompleted = global?.totalChallengesCompleted ?: 0,
                    activitiesThisMonth = activitiesThisMonth,
                    totalXp = mediums.sumOf { it.xp },
                    mediumProgress = mediums.sortedWith(
                        compareByDescending<MediumProgressEntity> { it.mediumType == freeMedium }
                            .thenBy { it.mediumType.ordinal }
                    ),
                    isPremium = prefs?.isPremium ?: false,
                    isLoading = false
                )
            }.collect { _uiState.value = it }
        }
    }

    /** Régénère le résumé (streak, niveaux, souvenirs) et renvoie un URI
     *  content:// prêt à être partagé (voir ProgressScreen). L'enrichissement
     *  (plus de souvenirs, photos) dépend d'isPremium, déterminé ici et non
     *  par l'appelant. Décodage bitmap potentiellement coûteux (photos
     *  premium) : hors du thread principal. */
    suspend fun exportSummary(format: ExportFormat): Uri = withContext(Dispatchers.IO) {
        val global = progressRepository.observeGlobalProgress().first()
        val mediums = progressRepository.observeAllMediumProgress().first()
        val isPremium = userPreferencesRepository.observePreferences().first()?.isPremium ?: false
        val souvenirs = challengeRepository.observePortfolio().first()
            .filter { it.completion.souvenirNote != null || it.completion.souvenirPhotoPath != null }
            .take(20) // ExportRenderer applique ensuite la limite gratuite/premium

        val data = ExportData(
            globalProgress = global,
            mediumProgress = mediums,
            recentSouvenirs = souvenirs,
            isPremium = isPremium
        )

        val uri = when (format) {
            ExportFormat.IMAGE -> exportGenerator.exportAsImage(data)
            ExportFormat.PDF -> exportGenerator.exportAsPdf(data)
        }
        analyticsTracker.trackExport(format.name)
        uri
    }
}
