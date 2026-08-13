package com.littlebigsteps.app.ui.challenge

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlebigsteps.app.analytics.AnalyticsTracker
import com.littlebigsteps.app.data.local.entity.ChallengeEntity
import com.littlebigsteps.app.data.local.entity.ChallengePackEntity
import com.littlebigsteps.app.data.local.entity.InProgressChallengeEntity
import com.littlebigsteps.app.data.media.SouvenirPhotoStore
import com.littlebigsteps.app.data.repository.ChallengeRepository
import com.littlebigsteps.app.data.repository.ProgressRepository
import com.littlebigsteps.app.data.repository.UserPreferencesRepository
import com.littlebigsteps.app.domain.model.ChallengeStatus
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Étapes "Découverte du défi" et "Réalisation & complétion" du parcours
 * (CLAUDE.md §3.2-3.3) : propose toujours 2-3 nouvelles activités, laisse en
 * choisir une (elle passe alors "En cours"), la faire progresser sur une
 * jauge auto-déclarée (Brouillon/En cours/Terminé), puis la finaliser avec un
 * souvenir optionnel et non vérifié (texte et/ou photo, stockée en local via
 * SouvenirPhotoStore). Gère aussi le parcours des packs thématiques/saisonniers
 * premium (CLAUDE.md §7).
 */
class ChallengeSelectionViewModel(
    private val challengeRepository: ChallengeRepository,
    private val progressRepository: ProgressRepository,
    private val souvenirPhotoStore: SouvenirPhotoStore,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengeSelectionUiState())
    val uiState: StateFlow<ChallengeSelectionUiState> = _uiState.asStateFlow()

    /** Collecteur de [ChallengeRepository.observeInProgress] pour le médium
     *  actif — annulé/relancé à chaque changement de médium (voir
     *  [observeInProgressFor]), jamais depuis [loadOptions] qui peut être
     *  appelé bien plus souvent sans que le médium change. */
    private var inProgressJob: Job? = null

    init {
        viewModelScope.launch {
            val unlocked = progressRepository.observeAllMediumProgress().first()
                .filter { it.isUnlocked }
                .map { it.mediumType }
            val isPremium = userPreferencesRepository.observePreferences().first()?.isPremium ?: false
            val defaultMedium = unlocked.firstOrNull()
            _uiState.value = _uiState.value.copy(
                availableMediums = unlocked,
                mediumType = defaultMedium,
                isPremium = isPremium
            )
            if (defaultMedium != null) {
                observeInProgressFor(defaultMedium)
                loadOptions(defaultMedium)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun selectMedium(medium: MediumType) {
        discardPendingSouvenirPhoto()
        _uiState.value = _uiState.value.copy(
            mediumType = medium,
            dialog = null,
            souvenirNote = "",
            souvenirPhotoPath = null
        )
        observeInProgressFor(medium)
        loadOptions(medium)
    }

    private fun observeInProgressFor(mediumType: MediumType) {
        inProgressJob?.cancel()
        inProgressJob = viewModelScope.launch {
            challengeRepository.observeInProgress(mediumType).collect { inProgress ->
                _uiState.value = _uiState.value.copy(inProgress = inProgress)
            }
        }
    }

    /** Passe des suggestions du jour au contenu complet d'un pack (CLAUDE.md §7).
     *  L'appelant (écran) doit vérifier isPremiumOnly/isPremium avant d'appeler
     *  ceci — un pack verrouillé redirige plutôt vers l'écran Premium. */
    fun selectPack(pack: ChallengePackEntity) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val challenges = challengeRepository.challengesInPack(pack.id)
            _uiState.value = _uiState.value.copy(activePack = pack, newOptions = challenges, isLoading = false)
        }
    }

    /** Revient aux suggestions aléatoires du jour pour le médium actif. */
    fun exitPack() {
        _uiState.value.mediumType?.let { loadOptions(it) }
    }

    fun selectNewChallenge(challenge: ChallengeEntity) {
        _uiState.value = _uiState.value.copy(dialog = ChallengeDialog.NewChallenge(challenge))
    }

    fun selectInProgress(entry: InProgressChallengeEntity) {
        _uiState.value = _uiState.value.copy(dialog = ChallengeDialog.InProgress(entry))
    }

    fun dismissDialog() {
        discardPendingSouvenirPhoto()
        _uiState.value = _uiState.value.copy(dialog = null, souvenirNote = "", souvenirPhotoPath = null)
    }

    /** "Choisir" sur une nouvelle activité : elle passe directement "En cours"
     *  (statut Brouillon) et la popup se ferme. */
    fun chooseNewChallenge() {
        val challenge = (_uiState.value.dialog as? ChallengeDialog.NewChallenge)?.challenge ?: return
        val medium = _uiState.value.mediumType ?: return
        _uiState.value = _uiState.value.copy(dialog = null)
        viewModelScope.launch {
            challengeRepository.startChallenge(challenge.id, medium)
            loadOptions(medium)
        }
    }

    /** Tap sur la jauge d'une activité en cours — reflète le nouveau statut
     *  immédiatement dans la popup ouverte, sans attendre le prochain tour de
     *  la Flow (voir [ChallengeSelectionUiState.dialog]). */
    fun setInProgressStatus(status: ChallengeStatus) {
        val current = (_uiState.value.dialog as? ChallengeDialog.InProgress)?.entry ?: return
        _uiState.value = _uiState.value.copy(
            dialog = ChallengeDialog.InProgress(current.copy(status = status))
        )
        viewModelScope.launch {
            challengeRepository.updateChallengeStatus(current.challenge.id, status)
        }
    }

    /** "Finaliser" sur la jauge (visible une fois à Terminé) : ouvre la popup
     *  "Bien joué !" (photo/légende) avant l'enregistrement définitif. */
    fun openFinalize() {
        val entry = (_uiState.value.dialog as? ChallengeDialog.InProgress)?.entry ?: return
        _uiState.value = _uiState.value.copy(dialog = ChallengeDialog.Finalize(entry))
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

    fun removeSouvenirPhoto() {
        removeCurrentSouvenirPhotoFile()
        _uiState.value = _uiState.value.copy(souvenirPhotoPath = null)
    }

    /** Bouton "Finaliser" de la popup "Bien joué !" : enregistrement définitif. */
    fun finalizeChallenge() {
        val state = _uiState.value
        val entry = (state.dialog as? ChallengeDialog.Finalize)?.entry ?: return
        val medium = state.mediumType ?: return

        _uiState.value = state.copy(isCompleting = true)
        viewModelScope.launch {
            val completion = challengeRepository.completeChallenge(
                challengeId = entry.challenge.id,
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
                dialog = null,
                souvenirNote = "",
                souvenirPhotoPath = null,
                pendingCameraTarget = null
            )
        }
    }

    /** Ferme le récap de complétion et retire un nouveau lot de propositions
     *  pour continuer (revient aux suggestions du jour, même si on finalisait
     *  une activité de pack). */
    fun dismissCompletion() {
        _uiState.value = _uiState.value.copy(lastCompletion = null, activePack = null)
        _uiState.value.mediumType?.let { loadOptions(it) }
    }

    private fun loadOptions(mediumType: MediumType) {
        _uiState.value = _uiState.value.copy(isLoading = true, activePack = null)
        viewModelScope.launch {
            val options = challengeRepository.pickDailyOptions(mediumType)
            val packs = challengeRepository.packsForMedium(mediumType)
            _uiState.value = _uiState.value.copy(newOptions = options, availablePacks = packs, isLoading = false)
        }
    }

    private fun removeCurrentSouvenirPhotoFile() {
        _uiState.value.souvenirPhotoPath?.let { souvenirPhotoStore.deleteIfExists(it) }
    }

    /** Nettoie tout fichier orphelin (photo déjà choisie ou capture caméra en
     *  attente) quand on ferme une popup sans finaliser l'activité. */
    private fun discardPendingSouvenirPhoto() {
        _uiState.value.pendingCameraTarget?.let { souvenirPhotoStore.deleteIfExists(it.file.absolutePath) }
        removeCurrentSouvenirPhotoFile()
    }
}
