package com.n.alian.today.ui.tasklist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.n.alian.today.data.local.Bucket
import com.n.alian.today.data.local.Task
import com.n.alian.today.ui.theme.Spacing
import com.nedal.today.R
import kotlinx.coroutines.launch

@Composable
fun TaskListScreen(viewModel: TaskListViewModel) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    // نخزن id المهمة قيد التعديل فقط (وليس الكائن كاملاً) لأن Task غير Parcelable،
    // وهذا يضمن بقاء الحوار مفتوحاً على نفس المهمة بعد دوران الشاشة أو process death.
    var editingTaskId by rememberSaveable { mutableStateOf<Int?>(null) }
    val editingTask = uiState.tasks.find { it.id == editingTaskId }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val taskCompletedMessage = stringResource(R.string.task_completed_message)
    val undoLabel = stringResource(R.string.undo)

    val onTaskCompleted: (Task) -> Unit = { task ->
        viewModel.onComplete(task)

        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = taskCompletedMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.onUndo()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add_task))
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {

            TabRow(selectedTabIndex = uiState.selectedBucket.ordinal) {
                Bucket.entries.forEach { bucket ->
                    Tab(
                        selected = bucket == uiState.selectedBucket,
                        onClick = { viewModel.onBucketSelected(bucket) },
                        text = { Text(bucket.label()) }
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
                    EmptyState()
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(Spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        items(uiState.tasks, key = { it.id }) { task ->
                            SwipeableTaskRow(
                                task = task,
                                onClick = { editingTaskId = task.id },
                                onCheckedChange = { onTaskCompleted(task) },
                                onSwiped = { onTaskCompleted(task) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        TaskDialog(
            dialogTitle = stringResource(R.string.new_task_dialog_title),
            initialTask = null,
            onConfirm = { title ->
                viewModel.onAddTask(title)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    editingTask?.let { task ->
        TaskDialog(
            dialogTitle = stringResource(R.string.edit_task_dialog_title),
            initialTask = task,
            onConfirm = { title ->
                viewModel.onUpdateTask(task.copy(title = title))
                editingTaskId = null
            },
            onDismiss = { editingTaskId = null },
            onDelete = {
                viewModel.onDeleteTask(task)
                editingTaskId = null
            }
        )
    }
}

/** Centered placeholder shown when the selected bucket has no active tasks. */
@Composable
private fun EmptyState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.empty_state_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TaskDialog(
    dialogTitle: String,
    initialTask: Task?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    // rememberSaveable حتى لا يُفقد النص المكتوب وغير المحفوظ بعد دوران الشاشة.
    var title by rememberSaveable(initialTask) {
        mutableStateOf(initialTask?.title ?: "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogTitle) },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.task_title_label)) },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title) },
                enabled = title.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text(
                            text = stringResource(R.string.delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    )
}

@Composable
private fun SwipeableTaskRow(
    task: Task,
    onClick: () -> Unit,
    onCheckedChange: () -> Unit,
    onSwiped: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) {
                onSwiped()
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
        TaskRow(
            task = task,
            onClick = onClick,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun TaskRow(
    task: Task,
    onClick: () -> Unit,
    onCheckedChange: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = { onCheckedChange() }
            )
            Spacer(modifier = Modifier.width(Spacing.small))
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f) // ضمان أن النص لا ينزل تحت التشك بوكس
            )
        }
    }
}
