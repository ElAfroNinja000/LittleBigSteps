package com.littlebigsteps.app.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.littlebigsteps.app.LittleBigStepsApplication
import com.littlebigsteps.app.R
import com.littlebigsteps.app.ui.common.label
import kotlinx.coroutines.flow.first

/**
 * Déclenché périodiquement par WorkManager (voir NotificationScheduler).
 * Personnalise le message avec le médium gratuit de l'utilisateur quand
 * disponible, sinon reste générique.
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        NotificationHelper.ensureChannel(applicationContext)

        val app = applicationContext as LittleBigStepsApplication
        val preferences = app.userPreferencesRepository.observePreferences().first()
        val message = preferences?.freeMedium?.let { medium ->
            applicationContext.getString(R.string.reminder_message_with_medium, medium.label(applicationContext))
        } ?: applicationContext.getString(R.string.reminder_message_generic)

        NotificationHelper.showReminder(applicationContext, message)
        return Result.success()
    }
}
