package com.n.alian.today.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.n.alian.today.data.local.Category
import com.n.alian.today.data.local.Priority
import com.n.alian.today.data.local.Task
import com.n.alian.today.ui.theme.Spacing
import java.util.Calendar
import java.util.TimeZone


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskEditorDialog(
    dialogTitle: String,
    initialTask: Task?,                 // null = إضافة جديدة
    onConfirm: (Task) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var notes by remember { mutableStateOf(initialTask?.notes ?: "") }
    var dueDate by remember { mutableStateOf(initialTask?.dueDate) }
    var priority by remember { mutableStateOf(initialTask?.priority ?: Priority.MEDIUM) }
    var category by remember { mutableStateOf(initialTask?.category ?: Category.NONE) }
    var showDatePicker by remember { mutableStateOf(false) }

    // ── منتقي التاريخ ──
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        dueDate = utcToLocalStartOfDay(it)
                    }
                    showDatePicker = false
                }) { Text("تم") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("إلغاء") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان المهمة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("وصف (اختياري)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(dueDate?.let { "📅 يستحق: ${formatDueDate(it)}" }
                        ?: "📅 تاريخ الاستحقاق (اختياري)")
                }
                Text("الأولوية", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    Priority.entries.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.label) }
                        )
                    }
                }
                Text("التصنيف", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    Category.entries.forEach { c ->
                        FilterChip(
                            selected = category == c,
                            onClick = { category = c },
                            label = { Text(c.label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val base = initialTask ?: Task(title = "")
                    onConfirm(
                        base.copy(
                            title = title.trim(),
                            notes = notes.trim().takeIf { it.isNotEmpty() },
                            dueDate = dueDate,
                            priority = priority,
                            category = category
                        )
                    )
                },
                enabled = title.isNotBlank()
            ) { Text("حفظ") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text("حذف", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("إلغاء") }
            }
        }
    )
}

/** DatePicker بيرجع millis بمنتصف ليل UTC — نحوّلها لمنتصف ليل محلي */
private fun utcToLocalStartOfDay(utcMillis: Long): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
    return Calendar.getInstance().apply {
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

