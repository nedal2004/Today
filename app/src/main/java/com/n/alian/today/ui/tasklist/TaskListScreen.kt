package com.n.alian.today.ui.tasklist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.n.alian.today.data.local.Bucket
import com.n.alian.today.data.local.Task
import com.n.alian.today.ui.theme.Spacing

@Composable
fun TaskListScreen(viewModel: TaskListViewModel) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // ── State محلي للشاشة ──
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة مهمة")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {

            TabRow(selectedTabIndex = uiState.selectedBucket.ordinal) {
                Bucket.entries.forEach { bucket ->
                    Tab(
                        selected = bucket == uiState.selectedBucket,
                        onClick = { viewModel.onBucketSelected(bucket) },
                        text = { Text(bucket.name) }
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                uiState.tasks.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("لا مهام هنا")
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(Spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        items(uiState.tasks, key = { it.id }) { task ->
                            TaskRow(
                                task = task,
                                onClick = { editingTask = task }   // 🆕 ضغطة = فتح التعديل
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Dialog الإضافة ──
    if (showAddDialog) {
        TaskDialog(
            dialogTitle = "مهمة جديدة",
            initialTask = null,
            onConfirm = { title ->
                viewModel.onAddTask(title)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // ── 🆕 Dialog التعديل ──
    editingTask?.let { task ->
        TaskDialog(
            dialogTitle = "تعديل المهمة",
            initialTask = task,
            onConfirm = { title ->
                viewModel.onUpdateTask(task.copy(title = title))
                editingTask = null
            },
            onDismiss = { editingTask = null },
            onDelete = {
                viewModel.onDeleteTask(task)
                editingTask = null
            }
        )
    }
}

// ── 🆕 Dialog عام: بيخدم الإضافة والتعديل ──
@Composable
private fun TaskDialog(
    dialogTitle: String,
    initialTask: Task?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    // remember بمفتاح: كل ما تتغير المهمة المفتوحة، الحقل بينجهز من جديد
    var title by remember(initialTask) {
        mutableStateOf(initialTask?.title ?: "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogTitle) },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان المهمة") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title) },
                enabled = title.isNotBlank()
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text(
                            text = "حذف",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("إلغاء")
                }
            }
        }
    )
}

@Composable
private fun TaskRow(task: Task, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = task.title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(Spacing.medium)
        )
    }
}