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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.n.alian.today.data.local.Bucket
import com.n.alian.today.data.local.Task
import com.n.alian.today.ui.theme.Spacing
import kotlinx.coroutines.launch
import com.n.alian.today.ui.component.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(viewModel: TaskListViewModel) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val onTaskCompleted: (Task) -> Unit = { task ->
        viewModel.onComplete(task)

        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "تم إنجاز المهمة",
                actionLabel = "تراجع",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.onUndo()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة مهمة")
            }
        }
    ) {  innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {

            TabRow(selectedTabIndex = uiState.selectedBucket.ordinal) {
                Bucket.entries.forEach { bucket ->
                    Tab(
                        selected = bucket == uiState.selectedBucket,
                        onClick = { viewModel.onBucketSelected(bucket) },
                        text = { Text(bucket.displayName()) }
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

//
                uiState.tasks.isEmpty() -> {
                    when (uiState.selectedBucket) {
                        Bucket.TODAY -> EmptyState(
                            title = "أنجزت كل شيء 🎉",
                            subTitle = "لا مهام لليوم — استمتع بوقتك"
                        )
                        Bucket.TOMORROW -> EmptyState(
                            title = "بكرة صفحة بيضاء",
                            subTitle = "أضف مهمة لتخطط ليومك القادم"
                        )
                        Bucket.LATER -> EmptyState(
                            title = "لا شيء مؤجل",
                            subTitle = "المهام غير المستعجلة تعيش هنا"
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(Spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        items(uiState.tasks, key = { it.id }) { task ->
                            SwipeableTaskRow(
                                task = task,
                                onClick = { editingTask = task },
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
            dialogTitle = "مهمة جديدة",
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

@Composable
private fun TaskDialog(
    dialogTitle: String,
    initialTask: Task?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
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
private fun Bucket.displayName(): String = when (this) {
    Bucket.TODAY -> "اليوم"
    Bucket.TOMORROW -> "بكرة"
    Bucket.LATER -> "لاحقاً"
}