package com.n.alian.today.ui.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.n.alian.today.data.local.Bucket
import com.n.alian.today.data.local.Task
import com.n.alian.today.data.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModel(
    private val repository: TaskRepository,
    // يحدّث Focus Widget بعد أي تعديل؛ suspend lambda بدل حقن Context مباشرة
    // في الـ ViewModel حتى يبقى قابلاً للاختبار بمعزل عن Android framework.
    private val onDataChanged: suspend () -> Unit = {}
) : ViewModel() {

    private val selectedBucket = MutableStateFlow(Bucket.TODAY)
    
    val uiState: StateFlow<TaskListUiState> = selectedBucket
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
        selectedBucket.value = bucket
    }

    fun onAddTask(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.add(Task(title = title.trim(), bucket = selectedBucket.value))
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
}

class TaskListViewModelFactory(
    private val repository: TaskRepository,
    private val onDataChanged: suspend () -> Unit = {}
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TaskListViewModel(repository, onDataChanged) as T
    }
}
