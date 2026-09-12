package com.pensebete.app.ui.viewmodel

import com.pensebete.app.data.TaskWithReminders
import java.time.LocalDateTime

data class TaskListUiState(
    val overdue: List<TaskWithReminders> = emptyList(),
    val today: List<TaskWithReminders> = emptyList(),
    val upcoming: List<TaskWithReminders> = emptyList(),
    val completed: List<TaskWithReminders> = emptyList(),
) {
    val isEmpty: Boolean
        get() = overdue.isEmpty() && today.isEmpty() && upcoming.isEmpty() && completed.isEmpty()
}

fun groupTasks(tasks: List<TaskWithReminders>, now: LocalDateTime = LocalDateTime.now()): TaskListUiState {
    val endOfToday = now.toLocalDate().plusDays(1).atStartOfDay()
    val (completed, active) = tasks.partition { it.task.isCompleted }
    val overdue = active.filter { it.task.dueAt.isBefore(now) }
    val today = active.filter { !it.task.dueAt.isBefore(now) && it.task.dueAt.isBefore(endOfToday) }
    val upcoming = active.filter { !it.task.dueAt.isBefore(endOfToday) }
    return TaskListUiState(overdue = overdue, today = today, upcoming = upcoming, completed = completed)
}
