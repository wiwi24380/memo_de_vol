package com.pensebete.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.pensebete.app.ui.screens.TaskEditScreen
import com.pensebete.app.ui.screens.TaskListScreen
import com.pensebete.app.ui.theme.PenseBeteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PenseBeteTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PenseBeteApp()
                }
            }
        }
    }
}

/** Sentinel used for [editingTaskId] to mean "creating a new task" (real task ids start at 1). */
private const val NEW_TASK_ID = -1L

@Composable
private fun PenseBeteApp() {
    var editingTaskId by rememberSaveable { mutableStateOf<Long?>(null) }

    RequestNotificationPermission()

    if (editingTaskId == null) {
        TaskListScreen(
            onAddClick = { editingTaskId = NEW_TASK_ID },
            onTaskClick = { taskId -> editingTaskId = taskId },
        )
    } else {
        TaskEditScreen(
            taskId = editingTaskId.takeIf { it != NEW_TASK_ID },
            onDone = { editingTaskId = null },
        )
    }
}

@Composable
private fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    var alreadyGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        alreadyGranted = granted
    }
    LaunchedEffect(Unit) {
        if (!alreadyGranted) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
