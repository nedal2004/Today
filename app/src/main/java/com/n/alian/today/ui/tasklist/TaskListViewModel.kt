package com.n.alian.today.ui.tasklist

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.n.alian.today.data.local.Bucket
import com.n.alian.today.data.local.Task
import com.n.alian.today.data.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModel(
    private val repository: TaskRepository,
    private val savedStateHandle: SavedStateHandle,
    // يحدّث Focus Widget بعد أي تعديل؛ suspend lambda بدل حقن Context مباشرة
    // في الـ ViewModel حتى يبقى قابلاً للاختبار بمعزل عن Android framework.
    private val onDataChanged: suspend () -> Unit = {}
) : ViewModel() {

    // نخزن اسم الـ bucket (String) بدل الـ enum مباشرة لضمان التوافق مع
    // تخزين SavedStateHandle عبر Bundle، ليبقى التبويب المختار كما هو
    // بعد process death وليس فقط دوران الشاشة.
    private val selectedBucketName: StateFlow<String> =
        savedStateHandle.getStateFlow(KEY_SELECTED_BUCKET, Bucket.TODAY.name)

    private val selectedBucket: Bucket
        get() = Bucket.valueOf(selectedBucketName.value)

    val uiState: StateFlow<TaskListUiState> = selectedBucketName
        .map { Bucket.valueOf(it) }
        .flatMapLatest { bucket ->
            repository.activeTasks(bucket).map { tasks ->
                TaskListUiState(
                    tasks = tasks,
                    selectedBucket = bucket,
                    isLoading = false
                )
            }
        }
        .catch { e ->
            emit(TaskListUiState(isLoading = false, error = e.message))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TaskListUiState()
        )

    private var lastCompletedTask: Task? = null

    fun onBucketSelected(bucket: Bucket) {
        savedStateHandle[KEY_SELECTED_BUCKET] = bucket.name
    }

    fun onAddTask(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.add(Task(title = title.trim(), bucket = selectedBucket))
            onDataChanged()
        }
    }

    fun onUpdateTask(task: Task) {
        viewModelScope.launch {
            repository.update(task)
            onDataChanged()
        }
    }

    fun onDeleteTask(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
            onDataChanged()
        }
    }

    fun onComplete(task: Task) {
        lastCompletedTask = task
        viewModelScope.launch {
            repository.complete(task)
            onDataChanged()
        }
    }

    fun onUndo() {
        lastCompletedTask?.let { task ->
            viewModelScope.launch {
                repository.uncomplete(task)
                lastCompletedTask = null
                onDataChanged()
            }
        }
    }

    private companion object {
        const val KEY_SELECTED_BUCKET = "selected_bucket"
    }
}

class TaskListViewModelFactory(
    private val repository: TaskRepository,
    owner: SavedStateRegistryOwner,
    private val onDataChanged: suspend () -> Unit = {}
) : AbstractSavedStateViewModelFactory(owner, null) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return TaskListViewModel(repository, handle, onDataChanged) as T
    }
}
