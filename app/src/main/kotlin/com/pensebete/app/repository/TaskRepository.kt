package com.pensebete.app.repository

import com.pensebete.app.data.Reminder
import com.pensebete.app.data.ReminderDao
import com.pensebete.app.data.Task
import com.pensebete.app.data.TaskDao
import com.pensebete.app.data.TaskWithReminders
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val reminderDao: ReminderDao,
) {
    fun observeTasks(): Flow<List<TaskWithReminders>> = taskDao.observeTasksWithReminders()

    suspend fun getTask(taskId: Long): TaskWithReminders? = taskDao.getTaskWithReminders(taskId)

    /** Inserts (or replaces, if [task] already has an id) a task along with its reminders. */
    suspend fun saveTask(task: Task, reminders: List<Reminder>): Long {
        val taskId = if (task.id == 0L) taskDao.insert(task) else {
            taskDao.update(task)
            task.id
        }
        reminderDao.deleteForTask(taskId)
        if (reminders.isNotEmpty()) {
            reminderDao.insertAll(reminders.map { it.copy(taskId = taskId) })
        }
        return taskId
    }

    suspend fun setCompleted(task: Task, completed: Boolean) {
        taskDao.update(task.copy(isCompleted = completed))
    }

    suspend fun deleteTask(task: Task) {
        taskDao.delete(task)
    }

    suspend fun deleteCompleted() {
        taskDao.deleteCompleted()
    }
}
