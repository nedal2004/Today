package com.n.alian.today.ui.tasklist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.n.alian.today.data.local.Bucket
import com.n.alian.today.data.local.Task
import com.n.alian.today.ui.component.EmptyState
import com.n.alian.today.ui.component.SegmentedTabs
import com.n.alian.today.ui.component.TaskDetailsDialog
import com.n.alian.today.ui.component.TaskEditorDialog
import com.n.alian.today.ui.component.TaskItem
import com.n.alian.today.ui.component.TodayHeader
import com.n.alian.today.ui.theme.Spacing
import kotlinx.coroutines.launch

@Composable
fun TaskListScreen(viewModel: TaskListViewModel) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var showActionDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة مهمة")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {

            TodayHeader(
                doneToday = uiState.doneToday,
                totalToday = uiState.totalToday,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = Spacing.medium)
                    .padding(top = Spacing.medium)
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            SegmentedTabs(
                selected = uiState.selectedBucket,
                onSelect = viewModel::onBucketSelected
            )

            AnimatedContent(
                targetState = Triple(uiState.isLoading, uiState.tasks.isEmpty(), uiState.selectedBucket),
                transitionSpec = {
                    (fadeIn(tween(250)) + scaleIn(initialScale = 0.95f, animationSpec = tween(250)))
                        .togetherWith(fadeOut(tween(150)))
                },
                label = "taskListContent"
            ) { (isLoading, isEmpty, bucket) ->
                when {
                    isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    isEmpty -> {
                        when (bucket) {
                            Bucket.TODAY -> EmptyState(
                                title = "أنجزت كل شيء 🎉",
                                subTitle = "لا مهام لليوم — استمتع بوقتك",
                                icon = Icons.Default.CheckCircle
                            )
                            Bucket.TOMORROW -> EmptyState(
                                title = "بكرة صفحة بيضاء",
                                subTitle = "أضف مهمة لتخطط ليومك القادم",
                                icon = Icons.Default.WbSunny
                            )
                            Bucket.LATER -> EmptyState(
                                title = "لا شيء مؤجل",
                                subTitle = "المهام غير المستعجلة تعيش هنا",
                                icon = Icons.Default.Schedule
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(Spacing.medium),
                            verticalArrangement = Arrangement.spacedBy(Spacing.small)
                        ) {
                            items(uiState.tasks, key = { it.id }) { task ->
                                TaskItem(
                                    task = task,
                                    onClick = {
                                        selectedTask = task
                                        showActionDialog = true
                                    },
                                    onToggle = { onTaskCompleted(task) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Dialog: إضافة مهمة ──
    if (showAddDialog) {
        TaskEditorDialog(
            dialogTitle = "مهمة جديدة",
            initialTask = null,
            onConfirm = { task ->
                viewModel.onAddTask(task)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // ── Dialog: تعديل مهمة ──
    editingTask?.let { task ->
        TaskEditorDialog(
            dialogTitle = "تعديل المهمة",
            initialTask = task,
            onConfirm = { updated ->
                viewModel.onUpdateTask(updated)
                editingTask = null
            },
            onDismiss = { editingTask = null },
            onDelete = {
                viewModel.onDeleteTask(task)
                editingTask = null
            }
        )
    }

    // ── Dialog: شو بدك تعمل؟ ──
    if (showActionDialog && selectedTask != null) {
        AlertDialog(
            onDismissRequest = {
                showActionDialog = false
                selectedTask = null
            },
            title = {
                Text(
                    text = selectedTask!!.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            text = { Text("شو بدك تعمل؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showActionDialog = false
                        showDetailsDialog = true
                    }
                ) {
                    Text("عرض التفاصيل")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showActionDialog = false
                        editingTask = selectedTask
                        selectedTask = null
                    }
                ) {
                    Text("تعديل")
                }
            }
        )
    }

    // ── Dialog: تفاصيل المهمة ──
    if (showDetailsDialog && selectedTask != null) {
        TaskDetailsDialog(
            task = selectedTask!!,
            onDismiss = {
                showDetailsDialog = false
                selectedTask = null
            }
        )
    }
}