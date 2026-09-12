package com.pensebete.app.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pensebete.app.data.AppDatabase
import com.pensebete.app.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Fires when an exact alarm goes off; looks the reminder back up so edits made after scheduling are respected. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        if (reminderId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val reminder = db.reminderDao().getById(reminderId)
                val task = reminder?.let { db.taskDao().getTaskWithReminders(it.taskId)?.task }
                if (task != null && !task.isCompleted) {
                    NotificationHelper(context).showReminder(task, reminderId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
    }
}
