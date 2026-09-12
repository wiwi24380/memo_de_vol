package com.pensebete.app.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pensebete.app.MainActivity
import com.pensebete.app.R
import com.pensebete.app.data.Task

/**
 * High-importance channel so reminders show as a heads-up banner and get
 * relayed to a paired watch (WearPro or the system's own Wear bridge).
 */
class NotificationHelper(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        ensureChannel()
    }

    private fun ensureChannel() {
        val channel = NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH)
            .setName(context.getString(R.string.notification_channel_name))
            .setDescription(context.getString(R.string.notification_channel_description))
            .build()
        notificationManager.createNotificationChannel(channel)
    }

    fun showReminder(task: Task, reminderId: Long) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            task.id.toInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val completeAction = NotificationCompat.Action(
            R.drawable.ic_notification,
            context.getString(R.string.action_complete),
            actionPendingIntent(NotificationActionReceiver.ACTION_COMPLETE, task.id, reminderId, requestCode = 1),
        )
        val snoozeAction = NotificationCompat.Action(
            R.drawable.ic_notification,
            context.getString(R.string.action_snooze),
            actionPendingIntent(NotificationActionReceiver.ACTION_SNOOZE, task.id, reminderId, requestCode = 2),
        )

        val wearableExtender = NotificationCompat.WearableExtender()
            .addAction(completeAction)
            .addAction(snoozeAction)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(task.title)
            .setContentText(task.description?.takeIf { it.isNotBlank() } ?: context.getString(R.string.notification_default_body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .addAction(completeAction)
            .addAction(snoozeAction)
            .extend(wearableExtender)
            .build()

        notificationManager.notify(reminderId.toInt(), notification)
    }

    fun dismiss(reminderId: Long) {
        notificationManager.cancel(reminderId.toInt())
    }

    private fun actionPendingIntent(action: String, taskId: Long, reminderId: Long, requestCode: Int): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = action
            putExtra(NotificationActionReceiver.EXTRA_TASK_ID, taskId)
            putExtra(NotificationActionReceiver.EXTRA_REMINDER_ID, reminderId)
        }
        // reminderId is unique per reminder; combine with requestCode so complete/snooze don't collide.
        val uniqueRequestCode = reminderId.toInt() * 10 + requestCode
        return PendingIntent.getBroadcast(
            context,
            uniqueRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val CHANNEL_ID = "reminders"
    }
}
