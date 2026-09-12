package com.pensebete.app.repository

import com.pensebete.app.alarms.AlarmScheduler
import com.pensebete.app.data.Reminder
import com.pensebete.app.data.ReminderDao
import com.pensebete.app.data.Task
import com.pensebete.app.data.TaskDao
import com.pensebete.app.data.TaskWithReminders
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val reminderDao: ReminderDao,
    private val alarmScheduler: AlarmScheduler,
) {
    fun observeTasks(): Flow<List<TaskWithReminders>> = taskDao.observeTasksWithReminders()

    suspend fun getTask(taskId: Long): TaskWithReminders? = taskDao.getTaskWithReminders(taskId)

    /** Inserts (or replaces, if [task] already has an id) a task along with its reminders, rescheduling their alarms. */
    suspend fun saveTask(task: Task, reminders: List<Reminder>): Long {
        val previousReminders = if (task.id != 0L) reminderDao.getForTask(task.id) else emptyList()
        alarmScheduler.cancelAll(previousReminders)

        val taskId = if (task.id == 0L) taskDao.insert(task) else {
            taskDao.update(task)
            task.id
        }
        val savedTask = task.copy(id = taskId)

        reminderDao.deleteForTask(taskId)
        if (reminders.isNotEmpty()) {
            val newIds = reminderDao.insertAll(reminders.map { it.copy(taskId = taskId) })
            reminders.zip(newIds).forEach { (reminder, newId) ->
                alarmScheduler.schedule(reminder.copy(id = newId, taskId = taskId), savedTask)
            }
        }
        return taskId
    }

    suspend fun setCompleted(task: Task, completed: Boolean) {
        taskDao.update(task.copy(isCompleted = completed))
        val reminders = reminderDao.getForTask(task.id)
        if (completed) {
            alarmScheduler.cancelAll(reminders)
        } else {
            reminders.forEach { alarmScheduler.schedule(it, task) }
        }
    }

    suspend fun deleteTask(task: Task) {
        alarmScheduler.cancelAll(reminderDao.getForTask(task.id))
        taskDao.delete(task)
    }

    /** Safe to call without cancelling alarms: setCompleted() already cancels a task's alarms when it's marked done. */
    suspend fun deleteCompleted() {
        taskDao.deleteCompleted()
    }
}
