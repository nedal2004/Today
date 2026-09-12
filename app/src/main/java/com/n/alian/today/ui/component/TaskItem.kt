package com.n.alian.today.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.n.alian.today.data.local.Task
import com.n.alian.today.ui.theme.Spacing
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextOverflow
import com.n.alian.today.data.local.Category
import com.n.alian.today.data.local.Priority

/**
 * صف مهمة بتصميم "تركيز جريء":
 * - بدون كرت — الصف نفسه قابل للضغط
 * - تشيك دائري متحرك (بيمتلئ أخضر + علامة ✓ بتكبر)
 * - المنجزة: شطر + لون باهت
 * - السحب يمين = إنجاز (كما كان)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItem(
    task: Task,
    onClick: () -> Unit,
    onToggle: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) {
                onToggle()   // ✅ نفس سلوك الـ checkbox: إنجاز + Snackbar تراجع
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = Spacing.medium),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        enableDismissFromEndToStart = false
    ) {
        TaskRow(task = task, onClick = onClick, onToggle = onToggle)
    }
}

@Composable
private fun TaskRow(
    task: Task,
    onClick: () -> Unit,
    onToggle: () -> Unit
) {
    val textColor by animateColorAsState(
        targetValue = if (task.isDone)
            MaterialTheme.colorScheme.onSurfaceVariant
        else
            MaterialTheme.colorScheme.onSurface,
        label = "taskTextColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleCheck(checked = task.isDone, onToggle = onToggle)

        Spacer(modifier = Modifier.width(Spacing.small))

        // ✅ عمود بدل Text منفرد: العنوان فوق + الميتا تحته
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                textDecoration = if (task.isDone) TextDecoration.LineThrough else null
            )

// ✅ جديد — الوصف تحت العنوان (لو موجود)
            task.notes?.let { notes ->
                Spacer(modifier = Modifier.height(Spacing.extraSmall))
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else null
                )
            }

            // 🎨 الميتا: نقطة أولوية + تصنيف + تاريخ استحقاق
            val hasMeta = task.dueDate != null ||
                    task.category != Category.NONE ||
                    task.priority != Priority.MEDIUM

            if (hasMeta) {
                Spacer(modifier = Modifier.height(Spacing.extraSmall))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val dotColor = when (task.priority) {
                        Priority.HIGH -> MaterialTheme.colorScheme.error
                        Priority.MEDIUM -> MaterialTheme.colorScheme.primary
                        Priority.LOW -> MaterialTheme.colorScheme.outlineVariant
                    }
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Spacer(modifier = Modifier.width(Spacing.extraSmall))
                    Text(
                        text = buildString {
                            if (task.category != Category.NONE) append(task.category.label)
                            task.dueDate?.let {
                                if (isNotEmpty()) append(" · ")
                                append(formatDueDate(it))
                            }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** تشيك بوكس دائري متحرك: حدود رمادية → تمتلئ أخضر مع علامة ✓ بتنبثق */
@Composable
private fun CircleCheck(checked: Boolean, onToggle: () -> Unit) {
    val fillColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surface,
        label = "checkFill"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant,
        label = "checkBorder"
    )
    // علامة ✓ بتكبر ناعمة لما تظهر
    val checkScale by animateFloatAsState(
        targetValue = if (checked) 1f else 0.4f,
        label = "checkScale"
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(fillColor)
            .border(width = 1.5.dp, color = borderColor, shape = CircleShape)
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "إنجاز",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer {
                        scaleX = checkScale
                        scaleY = checkScale
                    }
            )
        }
    }
}