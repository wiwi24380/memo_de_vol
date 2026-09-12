package com.pensebete.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pensebete.app.R
import com.pensebete.app.data.TaskWithReminders
import com.pensebete.app.ui.components.BatteryOptimizationBanner
import com.pensebete.app.ui.components.SwipeableTaskItem
import com.pensebete.app.ui.components.isIgnoringBatteryOptimizations
import com.pensebete.app.ui.components.requestIgnoreBatteryOptimizations
import com.pensebete.app.ui.viewmodel.TaskViewModel

@Composable
fun TaskListScreen(
    onAddClick: () -> Unit,
    onTaskClick: (Long) -> Unit,
    viewModel: TaskViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showBatteryBanner by remember { mutableStateOf(!isIgnoringBatteryOptimizations(context)) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_task))
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            if (showBatteryBanner) {
                BatteryOptimizationBanner(
                    onRequestDisable = {
                        requestIgnoreBatteryOptimizations(context)
                        showBatteryBanner = false
                    },
                    onDismiss = { showBatteryBanner = false },
                )
            }

            if (uiState.isEmpty) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.empty_state), style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    taskSection(stringResource(R.string.section_overdue), uiState.overdue, viewModel, onTaskClick)
                    taskSection(stringResource(R.string.section_today), uiState.today, viewModel, onTaskClick)
                    taskSection(stringResource(R.string.section_upcoming), uiState.upcoming, viewModel, onTaskClick)

                    if (uiState.completed.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(stringResource(R.string.section_completed), style = MaterialTheme.typography.titleSmall)
                                TextButton(onClick = { viewModel.deleteCompletedTasks() }) {
                                    Text(stringResource(R.string.delete_completed))
                                }
                            }
                        }
                        items(uiState.completed, key = { it.task.id }) { item ->
                            SwipeableTaskItem(
                                taskWithReminders = item,
                                onClick = { onTaskClick(item.task.id) },
                                onComplete = { viewModel.toggleCompleted(item) },
                                onDelete = { viewModel.deleteTask(item.task) },
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun LazyListScope.taskSection(
    title: String,
    tasks: List<TaskWithReminders>,
    viewModel: TaskViewModel,
    onTaskClick: (Long) -> Unit,
) {
    if (tasks.isEmpty()) return
    item {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
    items(tasks, key = { it.task.id }) { item ->
        SwipeableTaskItem(
            taskWithReminders = item,
            onClick = { onTaskClick(item.task.id) },
            onComplete = { viewModel.toggleCompleted(item) },
            onDelete = { viewModel.deleteTask(item.task) },
        )
    }
}
