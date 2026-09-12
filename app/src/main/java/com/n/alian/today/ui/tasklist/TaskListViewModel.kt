package com.n.alian.today.ui.tasklist

import android.app.Application
import android.util.Log
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.n.alian.today.data.local.Bucket
import com.n.alian.today.data.local.Task
import com.n.alian.today.data.repository.TaskRepository
import com.n.alian.today.data.repository.startOfDayMillis
import com.n.alian.today.ui.widget.TodayWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModel(
    application: Application,
    private val repository: TaskRepository
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "TaskListViewModel"
    }

    private val selectedBucket = MutableStateFlow(Bucket.TODAY)

    // ✅ combine بدل map: ثلاثة Flows تتجمع بـ UiState واحد
    // (مهام التبويب المختار + عدّاد مهام اليوم + عدّاد المنجز اليوم)
    val uiState: StateFlow<TaskListUiState> = selectedBucket
        .flatMapLatest { bucket ->
            combine(
                repository.activeTasks(bucket),
                repository.activeTodayCount(),
                repository.doneTodayCount(startOfDayMillis())
            ) { tasks, activeTodayCount, doneTodayCount ->
                TaskListUiState(
                    tasks = tasks,
                    selectedBucket = bucket,
                    doneToday = doneTodayCount,
                    totalToday = activeTodayCount + doneTodayCount,
                    isLoading = false
                )
            }
        }
        .flowOn(Dispatchers.IO)
        .catch { e ->
            emit(TaskListUiState(isLoading = false, error = e.message))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TaskListUiState(isLoading = true)
        )

    fun onBucketSelected(bucket: Bucket) {
        selectedBucket.value = bucket
    }

    fun onAddTask(task: Task) {
        if (task.title.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            // الـ bucket دائماً من التبويب الحالي — الحوار ما بيحدده
            repository.add(task.copy(bucket = selectedBucket.value))
            refreshWidget()
        }
    }

    fun onUpdateTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(task)
            refreshWidget()
        }
    }

    fun onDeleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(task)
            refreshWidget()
        }
    }

    private var lastCompletedTask: Task? = null

    fun onComplete(task: Task) {
        lastCompletedTask = task
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(
                task.copy(isDone = true, completedAt = System.currentTimeMillis())
            )
            refreshWidget()
        }
    }

    fun onUndo() {
        val task = lastCompletedTask ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(task.copy(isDone = false, completedAt = null))
            lastCompletedTask = null
            refreshWidget()
        }
    }

    private fun refreshWidget() {
        viewModelScope.launch(Dispatchers.Main) {
            TodayWidget().updateAll(getApplication())
            Log.d(TAG, "🔄 Widget updated from ViewModel")
        }
    }
}

class TaskListViewModelFactory(
    private val application: Application,
    private val repository: TaskRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TaskListViewModel(application, repository) as T
    }
}