package com.pensebete.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pensebete.app.alarms.AlarmScheduler
import com.pensebete.app.data.AppDatabase
import com.pensebete.app.data.Reminder
import com.pensebete.app.data.Task
import com.pensebete.app.data.TaskWithReminders
import com.pensebete.app.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = TaskRepository(db.taskDao(), db.reminderDao(), AlarmScheduler(application))
    }

    val uiState: StateFlow<TaskListUiState> = repository.observeTasks()
        .map { groupTasks(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskListUiState())

    fun toggleCompleted(taskWithReminders: TaskWithReminders) {
        viewModelScope.launch {
            repository.setCompleted(taskWithReminders.task, !taskWithReminders.task.isCompleted)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repository.deleteTask(task) }
    }

    fun deleteCompletedTasks() {
        viewModelScope.launch { repository.deleteCompleted() }
    }

    fun saveTask(task: Task, reminders: List<Reminder>) {
        viewModelScope.launch { repository.saveTask(task, reminders) }
    }

    suspend fun loadTask(taskId: Long): TaskWithReminders? = repository.getTask(taskId)
}
