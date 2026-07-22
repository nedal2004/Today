package com.n.alian.today.ui.tasklist

import com.n.alian.today.data.local.Bucket
import com.n.alian.today.data.local.Task
import com.n.alian.today.data.local.TaskDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** بديل بسيط في الذاكرة لـ [TaskDao] لاختبار [TaskListViewModel] بمعزل عن Room/Android. */
class FakeTaskDao : TaskDao {

    private val tasks = MutableStateFlow<List<Task>>(emptyList())
    private var nextId = 1

    override fun observeActive(bucket: Bucket): Flow<List<Task>> =
        tasks.map { list -> list.filter { it.bucket == bucket && !it.isDone } }

    override fun observeDoneToday(startOfDay: Long): Flow<List<Task>> =
        tasks.map { list -> list.filter { it.isDone && (it.completedAt ?: 0L) >= startOfDay } }

    override suspend fun insert(task: Task): Long {
        val id = nextId++
        tasks.value = tasks.value + task.copy(id = id)
        return id.toLong()
    }

    override suspend fun update(task: Task) {
        tasks.value = tasks.value.map { if (it.id == task.id) task else it }
    }

    override suspend fun delete(task: Task) {
        tasks.value = tasks.value.filterNot { it.id == task.id }
    }

    override suspend fun promoteTomorrowToToday() {
        tasks.value = tasks.value.map {
            if (it.bucket == Bucket.TOMORROW && !it.isDone) it.copy(bucket = Bucket.TODAY) else it
        }
    }

    override suspend fun archiveOldDone(startOfDay: Long) {
        tasks.value = tasks.value.filterNot {
            it.isDone && (it.completedAt ?: Long.MAX_VALUE) < startOfDay
        }
    }
}
