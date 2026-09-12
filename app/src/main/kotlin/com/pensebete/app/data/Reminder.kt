package com.pensebete.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * A single reminder attached to a [Task]. [offsetMinutes] is how long before
 * (or at, when 0) the task's due date this reminder should fire; [triggerAt]
 * is the resolved absolute time and is what actually gets scheduled with
 * AlarmManager. It is recomputed whenever the task's due date or this
 * reminder's offset changes.
 */
@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("taskId")],
)
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val offsetMinutes: Long,
    val triggerAt: LocalDateTime,
)

/** Common quick-pick offsets, in minutes before the due date. */
object ReminderPresets {
    const val AT_DUE_TIME = 0L
    const val MINUTES_15 = 15L
    const val HOUR_1 = 60L
    const val DAY_1 = 24L * 60L
    const val DAYS_2 = 2 * 24L * 60L
}
