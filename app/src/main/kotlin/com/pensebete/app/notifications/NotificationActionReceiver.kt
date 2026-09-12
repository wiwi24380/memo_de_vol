package com.pensebete.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pensebete.app.alarms.AlarmScheduler
import com.pensebete.app.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/** Handles the "Terminé" and "Rappeler plus tard" actions, from the phone or relayed from the watch. */
class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        if (taskId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val scheduler = AlarmScheduler(context)
                when (intent.action) {
                    ACTION_COMPLETE -> {
                        db.taskDao().getTaskWithReminders(taskId)?.task?.let { task ->
                            db.taskDao().update(task.copy(isCompleted = true))
                        }
                        scheduler.cancelAll(db.reminderDao().getForTask(taskId))
                    }
                    ACTION_SNOOZE -> {
                        val reminder = if (reminderId != -1L) db.reminderDao().getById(reminderId) else null
                        val task = db.taskDao().getTaskWithReminders(taskId)?.task
                        if (reminder != null && task != null) {
                            val snoozed = reminder.copy(triggerAt = LocalDateTime.now().plusMinutes(SNOOZE_MINUTES))
                            db.reminderDao().update(snoozed)
                            scheduler.schedule(snoozed, task)
                        }
                    }
                }
                if (reminderId != -1L) NotificationHelper(context).dismiss(reminderId)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_COMPLETE = "com.pensebete.app.action.COMPLETE"
        const val ACTION_SNOOZE = "com.pensebete.app.action.SNOOZE"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val SNOOZE_MINUTES = 15L
    }
}
