package com.pensebete.app.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.pensebete.app.data.Reminder
import com.pensebete.app.data.Task
import java.time.ZoneId

/**
 * Schedules and cancels the exact alarms behind each [Reminder]. Uses
 * setExactAndAllowWhileIdle so reminders still fire while the phone is
 * dozing; USE_EXACT_ALARM (declared in the manifest) grants this without
 * a runtime prompt since the app isn't Play-distributed.
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager? = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun schedule(reminder: Reminder, task: Task) {
        if (task.isCompleted) return
        val triggerAtMillis = reminder.triggerAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val pendingIntent = pendingIntentFor(reminder.id)
        alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    fun cancel(reminderId: Long) {
        val pendingIntent = pendingIntentFor(reminderId)
        alarmManager?.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun cancelAll(reminders: List<Reminder>) {
        reminders.forEach { cancel(it.id) }
    }

    private fun pendingIntentFor(reminderId: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminderId)
        }
        return PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
