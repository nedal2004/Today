package com.n.alian.today.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.n.alian.today.data.local.Category
import com.n.alian.today.data.local.Priority
import com.n.alian.today.data.local.Task
import com.n.alian.today.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TaskDetailsDialog(
    task: Task,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PriorityDot(priority = task.priority)
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                // ── الوصف ──
                if (!task.notes.isNullOrBlank()) {
                    Column {
                        Text(
                            text = "الوصف",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(Spacing.extraSmall))
                        Text(
                            text = task.notes,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                //الاولولية
                Column {
                    Text(
                        text = "الأولوية",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Spacing.extraSmall))
                    Text(
                        text = task.priority.label,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // ── التصنيف ──
                if (task.category != Category.NONE) {
                    Column {
                        Text(
                            text = "التصنيف",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(Spacing.extraSmall))
                        Text(
                            text = task.category.label,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // تاريخ الاستحقاق
                task.dueDate?.let { due ->
                    Column {
                        Text(
                            text = "تاريخ الاستحقاق",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(Spacing.extraSmall))
                        Text(
                            text = formatFullDate(due),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

// the status
                val (statusText, statusColor) = when {
                    task.isDone -> "منجزة ✓" to MaterialTheme.colorScheme.primary
                    task.dueDate == null -> "نشطة" to MaterialTheme.colorScheme.onSurface
                    isOverdue(task.dueDate!!) -> "⚠️ متأخرة" to MaterialTheme.colorScheme.error
                    isToday(task.dueDate!!) -> "📅 تستحق اليوم" to MaterialTheme.colorScheme.primary
                    else -> "نشطة" to MaterialTheme.colorScheme.onSurface
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "الحالة: ",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = statusColor
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}

// لون الاولوية
@Composable
private fun PriorityDot(priority: Priority) {
    val color = when (priority) {
        Priority.HIGH -> MaterialTheme.colorScheme.error
        Priority.MEDIUM -> MaterialTheme.colorScheme.primary
        Priority.LOW -> MaterialTheme.colorScheme.outlineVariant
    }
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
    )
}

// تنسيق التاريخ
private fun formatFullDate(millis: Long): String {
    val sdf = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar"))
    return sdf.format(Date(millis))
}

// هل التاريخ في الماضي؟
private fun isOverdue(millis: Long): Boolean {
    return millis < startOfDay()
}

// التاريخ اليوم؟
private fun isToday(millis: Long): Boolean {
    val today = startOfDay()
    val tomorrow = today + 24 * 60 * 60 * 1000L
    return millis >= today && millis < tomorrow
}

// بداية اليوم الحالي
private fun startOfDay(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}