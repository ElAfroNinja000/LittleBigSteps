package com.littlebigsteps.app.fakes

import com.littlebigsteps.app.domain.model.Frequency
import com.littlebigsteps.app.notification.NotificationScheduler
import kotlinx.datetime.LocalTime

/** Pas de vrai WorkManager programmé dans un test UI (voir CLAUDE.md §11). */
class FakeNotificationScheduler : NotificationScheduler {

    var lastScheduled: Pair<Frequency, LocalTime>? = null
        private set
    var cancelCallCount = 0
        private set

    override fun scheduleReminders(frequency: Frequency, reminderTime: LocalTime) {
        lastScheduled = frequency to reminderTime
    }

    override fun cancelReminders() {
        cancelCallCount++
        lastScheduled = null
    }
}
