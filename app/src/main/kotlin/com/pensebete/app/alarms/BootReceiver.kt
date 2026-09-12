package com.pensebete.app.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pensebete.app.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/** Alarms don't survive a reboot: re-read every pending reminder from Room and reschedule it. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val scheduler = AlarmScheduler(context)
                val now = LocalDateTime.now()
                db.reminderDao().getAllActive()
                    .filter { it.triggerAt.isAfter(now) }
                    .forEach { reminder ->
                        db.taskDao().getTaskWithReminders(reminder.taskId)?.task?.let { task ->
                            scheduler.schedule(reminder, task)
                        }
                    }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
