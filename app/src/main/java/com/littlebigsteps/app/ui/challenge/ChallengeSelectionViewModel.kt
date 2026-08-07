package com.littlebigsteps.app.ui.challenge

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlebigsteps.app.analytics.AnalyticsTracker
import com.littlebigsteps.app.data.local.entity.ChallengeEntity
import com.littlebigsteps.app.data.media.SouvenirPhotoStore
import com.littlebigsteps.app.data.repository.ChallengeRepository
import com.littlebigsteps.app.data.repository.ProgressRepository
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Étapes "Découverte du défi" et "Réalisation & complétion" du parcours
 * (CLAUDE.md §3.2-3.3) : propose toujours 2-3 défis, laisse en choisir un,
 * puis le marquer terminé avec un souvenir optionnel et non vérifié (texte
 * et/ou photo, stockée en local via SouvenirPhotoStore).
 */
class ChallengeSelectionViewModel(
    private val challengeRepository: ChallengeRepository,
    private val progressRepository: ProgressRepository,
    private val souvenirPhotoStore: SouvenirPhotoStore,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengeSelectionUiState())
    val uiState: StateFlow<ChallengeSelectionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val unlocked = progressRepository.observeAllMediumProgress().first()
                .filter { it.isUnlocked }
                .map { it.mediumType }
            val defaultMedium = unlocked.firstOrNull()
            _uiState.value = _uiState.value.copy(availableMediums = unlocked, mediumType = defaultMedium)
            if (defaultMedium != null) loadOptions(defaultMedium) else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun selectMedium(medium: MediumType) {
        discardPendingSouvenirPhoto()
        _uiState.value = _uiState.value.copy(
            mediumType = medium,
            selectedChallenge = null,
            souvenirNote = "",
            souvenirPhotoPath = null
        )
        loadOptions(medium)
    }

    /** Retire un nouveau lot de défis pour le même médium, sans le changer. */
    fun refreshOptions() {
        _uiState.value.mediumType?.let { loadOptions(it) }
    }

    fun selectChallenge(challenge: ChallengeEntity) {
        _uiState.value = _uiState.value.copy(
            selectedChallenge = challenge,
            souvenirNote = "",
            souvenirPhotoPath = null
        )
    }

    fun clearSelection() {
        discardPendingSouvenirPhoto()
        _uiState.value = _uiState.value.copy(
            selectedChallenge = null,
            souvenirNote = "",
            souvenirPhotoPath = null
        )
    }

    fun updateSouvenirNote(note: String) {
        _uiState.value = _uiState.value.copy(souvenirNote = note)
    }

    /** Crée le fichier cible pour l'app caméra et renvoie son URI à passer à l'Intent. */
    fun prepareCameraCapture(): Uri {
        discardPendingSouvenirPhoto() // une seule photo à la fois
        val target = souvenirPhotoStore.createCaptureTarget()
        _uiState.value = _uiState.value.copy(pendingCameraTarget = target)
        return target.uri
    }

    /** À appeler avec le résultat de l'Intent caméra (voir ChallengeSelectionScreen). */
    fun onCameraResult(success: Boolean) {
        val target = _uiState.value.pendingCameraTarget ?: return
        _uiState.value = if (success) {
            _uiState.value.copy(souvenirPhotoPath = target.file.absolutePath, pendingCameraTarget = null)
        } else {
            souvenirPhotoStore.deleteIfExists(target.file.absolutePath)
            _uiState.value.copy(pendingCameraTarget = null)
        }
    }

    /** À appeler avec le résultat du sélecteur de galerie. */
    fun onGalleryImageSelected(sourceUri: Uri?) {
        if (sourceUri == null) return
        viewModelScope.launch {
            val file = souvenirPhotoStore.importFromUri(sourceUri) ?: return@launch
            removeCurrentSouvenirPhotoFile() // remplace une éventuelle photo déjà choisie
            _uiState.value = _uiState.value.copy(souvenirPhotoPath = file.absolutePath)
        }
    }

    fun removeSouvenirPhoto() {
        removeCurrentSouvenirPhotoFile()
        _uiState.value = _uiState.value.copy(souvenirPhotoPath = null)
    }

    fun completeSelectedChallenge() {
        val state = _uiState.value
        val challenge = state.selectedChallenge ?: return
        val medium = state.mediumType ?: return

        _uiState.value = state.copy(isCompleting = true)
        viewModelScope.launch {
            val completion = challengeRepository.completeChallenge(
                challengeId = challenge.id,
                mediumType = medium,
                souvenirPhotoPath = state.souvenirPhotoPath,
                souvenirNote = state.souvenirNote.ifBlank { null }
            )
            val currentStreak = progressRepository.observeGlobalProgress().first()?.currentStreak ?: 0
            analyticsTracker.trackChallengeCompleted(
                mediumType = medium,
                hasSouvenir = state.souvenirPhotoPath != null || state.souvenirNote.isNotBlank(),
                currentStreak = currentStreak
            )
            _uiState.value = _uiState.value.copy(
                isCompleting = false,
                lastCompletion = completion,
                selectedChallenge = null,
                souvenirNote = "",
                souvenirPhotoPath = null,
                pendingCameraTarget = null
            )
        }
    }

    /** Ferme le récap de complétion et retire un nouveau lot de défis pour continuer. */
    fun dismissCompletion() {
        _uiState.value = _uiState.value.copy(lastCompletion = null)
        _uiState.value.mediumType?.let { loadOptions(it) }
    }

    private fun loadOptions(mediumType: MediumType) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val options = challengeRepository.pickDailyOptions(mediumType)
            _uiState.value = _uiState.value.copy(options = options, isLoading = false)
        }
    }

    private fun removeCurrentSouvenirPhotoFile() {
        _uiState.value.souvenirPhotoPath?.let { souvenirPhotoStore.deleteIfExists(it) }
    }

    /** Nettoie tout fichier orphelin (photo déjà choisie ou capture caméra en
     *  attente) quand on abandonne la sélection sans compléter le défi. */
    private fun discardPendingSouvenirPhoto() {
        _uiState.value.pendingCameraTarget?.let { souvenirPhotoStore.deleteIfExists(it.file.absolutePath) }
        removeCurrentSouvenirPhotoFile()
    }
}
