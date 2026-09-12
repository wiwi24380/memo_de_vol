package com.pensebete.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ReminderDao {
    @Insert
    suspend fun insertAll(reminders: List<Reminder>): List<Long>

    @Update
    suspend fun update(reminder: Reminder)

    @Query("DELETE FROM reminders WHERE taskId = :taskId")
    suspend fun deleteForTask(taskId: Long)

    @Query("SELECT * FROM reminders WHERE id = :reminderId")
    suspend fun getById(reminderId: Long): Reminder?

    @Query("SELECT * FROM reminders WHERE taskId = :taskId")
    suspend fun getForTask(taskId: Long): List<Reminder>

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
