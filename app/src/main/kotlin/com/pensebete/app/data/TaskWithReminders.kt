package com.pensebete.app.data

import androidx.room.Embedded
import androidx.room.Relation

data class TaskWithReminders(
    @Embedded val task: Task,
    @Relation(parentColumn = "id", entityColumn = "taskId")
    val reminders: List<Reminder>,
)
