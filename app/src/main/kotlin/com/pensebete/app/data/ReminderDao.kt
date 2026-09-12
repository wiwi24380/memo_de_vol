package com.pensebete.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ReminderDao {
    @Insert
    suspend fun insertAll(reminders: List<Reminder>): List<Long>

    @Query("DELETE FROM reminders WHERE taskId = :taskId")
    suspend fun deleteForTask(taskId: Long)

    @Query("SELECT * FROM reminders WHERE id = :reminderId")
    suspend fun getById(reminderId: Long): Reminder?

    /** All reminders belonging to a task that isn't completed yet, used to reschedule alarms after boot. */
    @Query(
        """
        SELECT reminders.* FROM reminders
        INNER JOIN tasks ON tasks.id = reminders.taskId
        WHERE tasks.isCompleted = 0
        """,
    )
    suspend fun getAllActive(): List<Reminder>
}
