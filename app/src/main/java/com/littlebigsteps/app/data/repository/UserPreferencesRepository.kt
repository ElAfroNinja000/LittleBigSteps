package com.littlebigsteps.app.data.repository

import com.littlebigsteps.app.data.local.dao.UserPreferencesDao
import com.littlebigsteps.app.data.local.entity.UserPreferencesEntity
import com.littlebigsteps.app.domain.model.Frequency
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime

/**
 * Préférences fixées à l'onboarding puis modifiables depuis les Paramètres
 * (aucun compte, tout reste local — CLAUDE.md §9).
 */
interface UserPreferencesRepository {

    fun observePreferences(): Flow<UserPreferencesEntity?>

    suspend fun completeOnboarding(
        selectedMediums: List<MediumType>,
        freeMedium: MediumType,
        reminderFrequency: Frequency,
        reminderTime: LocalTime
    )

    suspend fun setPremium(isPremium: Boolean)

    /** Vue "Paramètres" : fréquence/heure de rappel modifiables après
     *  l'onboarding, indépendamment l'une de l'autre. */
    suspend fun updateReminderFrequency(frequency: Frequency)
    suspend fun updateReminderTime(reminderTime: LocalTime)

    /** Coupe/rétablit les rappels sans changer fréquence/heure (voir
     *  NotificationScheduler, appelé séparément par SettingsViewModel). */
    suspend fun setNotificationsEnabled(enabled: Boolean)

    /** Opt-out analytics anonymes (voir AnalyticsTracker.setEnabled). */
    suspend fun setAnalyticsEnabled(enabled: Boolean)
}

class UserPreferencesRepositoryImpl(
    private val dao: UserPreferencesDao
) : UserPreferencesRepository {

    override fun observePreferences(): Flow<UserPreferencesEntity?> = dao.observe()

    override suspend fun completeOnboarding(
        selectedMediums: List<MediumType>,
        freeMedium: MediumType,
        reminderFrequency: Frequency,
        reminderTime: LocalTime
    ) {
        dao.upsert(
            UserPreferencesEntity(
                selectedMediums = selectedMediums,
                freeMedium = freeMedium,
                reminderFrequency = reminderFrequency,
                reminderTime = reminderTime,
                onboardingCompletedAt = Clock.System.now()
            )
        )
    }

    override suspend fun setPremium(isPremium: Boolean) {
        // No-op si l'onboarding n'a pas encore été fait : rien à mettre à jour.
        val current = dao.getOnce() ?: return
        dao.upsert(current.copy(isPremium = isPremium))
    }

    override suspend fun updateReminderFrequency(frequency: Frequency) {
        val current = dao.getOnce() ?: return
        dao.upsert(current.copy(reminderFrequency = frequency))
    }

    override suspend fun updateReminderTime(reminderTime: LocalTime) {
        val current = dao.getOnce() ?: return
        dao.upsert(current.copy(reminderTime = reminderTime))
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        val current = dao.getOnce() ?: return
        dao.upsert(current.copy(notificationsEnabled = enabled))
    }

    override suspend fun setAnalyticsEnabled(enabled: Boolean) {
        val current = dao.getOnce() ?: return
        dao.upsert(current.copy(analyticsEnabled = enabled))
    }
}
