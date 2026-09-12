package com.pensebete.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pensebete.app.R
import com.pensebete.app.data.ReminderPresets

private enum class TimeUnitOption(val multiplierMinutes: Long, val labelRes: Int) {
    MINUTES(1L, R.string.custom_offset_unit_minutes),
    HOURS(60L, R.string.custom_offset_unit_hours),
    DAYS(24L * 60L, R.string.custom_offset_unit_days),
}

/** Lets the user add several reminders to a task: quick presets, or a free minutes/hours/days offset. */
@Composable
fun ReminderPicker(
    offsets: List<Long>,
    onAdd: (Long) -> Unit,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(stringResource(R.string.reminders_label), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        val presets = listOf(
            ReminderPresets.AT_DUE_TIME to R.string.reminder_at_due_time,
            ReminderPresets.MINUTES_15 to R.string.reminder_15_min,
            ReminderPresets.HOUR_1 to R.string.reminder_1_hour,
            ReminderPresets.DAY_1 to R.string.reminder_1_day,
            ReminderPresets.DAYS_2 to R.string.reminder_2_days,
        )
        LazyRow {
            items(presets.size) { index ->
                val (minutes, labelRes) = presets[index]
                AssistChip(
                    onClick = { onAdd(minutes) },
                    label = { Text(stringResource(labelRes)) },
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        CustomOffsetInput(onAdd = onAdd)

        if (offsets.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            offsets.forEachIndexed { index, minutes ->
                ReminderEntryRow(minutes = minutes, onRemove = { onRemove(index) })
            }
        }
    }
}

@Composable
private fun CustomOffsetInput(onAdd: (Long) -> Unit) {
    var value by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(TimeUnitOption.MINUTES) }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = { new -> if (new.length <= 4 && new.all(Char::isDigit)) value = new },
            label = { Text(stringResource(R.string.custom_offset_value)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(110.dp),
        )
        Spacer(Modifier.width(8.dp))
        TimeUnitOption.entries.forEach { option ->
            FilterChip(
                selected = unit == option,
                onClick = { unit = option },
                label = { Text(stringResource(option.labelRes)) },
                modifier = Modifier.padding(end = 4.dp),
            )
        }
        IconButton(onClick = {
            val amount = value.toLongOrNull()
            if (amount != null && amount > 0) {
                onAdd(amount * unit.multiplierMinutes)
                value = ""
            }
        }) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_reminder))
        }
    }
}

@Composable
private fun ReminderEntryRow(minutes: Long, onRemove: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Icon(Icons.Filled.Notifications, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(formatOffsetLabel(minutes), modifier = Modifier.weight(1f))
        IconButton(onClick = onRemove) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.delete))
        }
    }
}

@Composable
private fun formatOffsetLabel(minutes: Long): String = when {
    minutes == 0L -> stringResource(R.string.reminder_at_due_time)
    minutes % (24 * 60) == 0L -> "${minutes / (24 * 60)} j avant"
    minutes % 60 == 0L -> "${minutes / 60} h avant"
    else -> "$minutes min avant"
}
