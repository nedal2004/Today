package com.n.alian.today.data.repository


import com.n.alian.today.data.local.Bucket
import com.n.alian.today.data.local.Task
import com.n.alian.today.data.local.TaskDao
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {
    //observeActive
    fun activeTasks(bucket: Bucket): Flow<List<Task>> = dao.observeActive(bucket)

    fun doneToday(startOfDay: Long): Flow<List<Task>> = dao.observeDoneToday(startOfDay)

    suspend fun add(task: Task): Long = dao.insert(task)

    suspend fun update(task: Task) = dao.update(task)

    suspend fun delete(task: Task) = dao.delete(task)

    /** يضبط completedAt عند الإنجاز — ضروري لعمل archiveOldDone في الترحيل اليومي. */
    suspend fun complete(task: Task) =
        dao.update(task.copy(isDone = true, completedAt = System.currentTimeMillis()))

    suspend fun uncomplete(task: Task) =
        dao.update(task.copy(isDone = false, completedAt = null))

    suspend fun runDailyRollover(startOfDay: Long) {
        dao.archiveOldDone(startOfDay)
        dao.promoteTomorrowToToday()
    }
}