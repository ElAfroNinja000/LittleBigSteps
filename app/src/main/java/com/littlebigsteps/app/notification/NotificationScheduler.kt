package com.littlebigsteps.app.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.littlebigsteps.app.domain.model.Frequency
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.util.concurrent.TimeUnit

/** Programme/annule le rappel périodique (CLAUDE.md §10 : WorkManager, pas de FCM). */
interface NotificationScheduler {
    fun scheduleReminders(frequency: Frequency, reminderTime: LocalTime)
    fun cancelReminders()
}

class WorkManagerNotificationScheduler(
    private val context: Context
) : NotificationScheduler {

    override fun scheduleReminders(frequency: Frequency, reminderTime: LocalTime) {
        NotificationHelper.ensureChannel(context)

        val request = PeriodicWorkRequestBuilder<ReminderWorker>(
            frequency.toRepeatIntervalDays(), TimeUnit.DAYS
        )
            .setInitialDelay(calculateInitialDelayMillis(reminderTime), TimeUnit.MILLISECONDS)
            .build()

        // UPDATE plutôt que KEEP : si l'utilisateur change ses préférences plus
        // tard, le nouveau planning remplace l'ancien au lieu de l'ignorer.
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    override fun cancelReminders() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /** ~3-4 rappels par semaine pour FEW_TIMES_WEEK. WorkManager rejoue toutes les
     *  `repeatInterval` depuis l'enqueue sans recaler l'heure exacte à chaque
     *  répétition : un décalage progressif est possible, acceptable pour un
     *  rappel motivant plutôt qu'une alarme critique. */
    private fun Frequency.toRepeatIntervalDays(): Long = when (this) {
        Frequency.DAILY -> 1L
        Frequency.FEW_TIMES_WEEK -> 2L
        Frequency.WEEKLY -> 7L
    }

    private fun calculateInitialDelayMillis(reminderTime: LocalTime): Long {
        val timeZone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val today = now.toLocalDateTime(timeZone).date
        var target = LocalDateTime(today, reminderTime).toInstant(timeZone)
        if (target <= now) {
            target = LocalDateTime(today.plus(1, DateTimeUnit.DAY), reminderTime).toInstant(timeZone)
        }
        return (target - now).inWholeMilliseconds
    }

    private companion object {
        const val WORK_NAME = "reminder_work"
    }
}
