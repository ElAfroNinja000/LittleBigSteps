package com.littlebigsteps.app.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.littlebigsteps.app.R

/**
 * Crée le canal de notification (requis API 26+) et affiche le rappel.
 * Rien n'est personnalisé à distance : pas de FCM nécessaire (CLAUDE.md §10).
 */
object NotificationHelper {

    private const val CHANNEL_ID = "daily_reminder"
    private const val NOTIFICATION_ID = 1001

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Rappel de défi",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Rappel périodique pour proposer le défi créatif du moment."
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    /** Ne fait rien si la permission POST_NOTIFICATIONS (API 33+) n'a pas été accordée
     *  — pas de crash, juste un rappel silencieux, cohérent avec l'absence de
     *  culpabilisation en cas d'oubli (CLAUDE.md §4). */
    @SuppressLint("MissingPermission") // vérifié manuellement juste en dessous
    fun showReminder(context: Context, contentText: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("LittleBigSteps")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
