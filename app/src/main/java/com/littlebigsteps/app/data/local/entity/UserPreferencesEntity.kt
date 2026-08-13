package com.littlebigsteps.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.littlebigsteps.app.domain.model.Frequency
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime

/**
 * Préférences définies à l'onboarding (singleton, id fixé). Aucun compte :
 * ces données restent 100% locales à l'appareil (voir CLAUDE.md §9, risque
 * de perte de progression au changement d'appareil, à mitiger par l'export).
 */
@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val selectedMediums: List<MediumType> = emptyList(),
    val freeMedium: MediumType,
    val reminderFrequency: Frequency = Frequency(7),
    val reminderTime: LocalTime,
    val isPremium: Boolean = false,
    val onboardingCompletedAt: Instant? = null
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}
