package com.n.alian.today.data.repository

import com.n.alian.today.data.local.Bucket
import com.n.alian.today.data.local.Task
import com.n.alian.today.data.local.TaskDao
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

fun startOfDayMillis(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

class TaskRepository(private val dao: TaskDao) {

    fun activeTasks(bucket: Bucket): Flow<List<Task>> = dao.observeActive(bucket)

    fun doneToday(startOfDay: Long): Flow<List<Task>> = dao.observeDoneToday(startOfDay)

    // ── عدّادات الهيدر ──
    fun activeTodayCount(): Flow<Int> = dao.observeActiveTodayCount()

    fun doneTodayCount(startOfDay: Long): Flow<Int> = dao.observeDoneTodayCount(startOfDay)

    suspend fun getTaskById(taskId: Long): Task? = dao.getTaskById(taskId)

    suspend fun add(task: Task): Long = dao.insert(task)

    suspend fun update(task: Task) = dao.update(task)

    suspend fun delete(task: Task) = dao.delete(task)

    suspend fun runDailyRollover(startOfDay: Long) {
        dao.archiveOldDone(startOfDay)
        dao.promoteTomorrowToToday()
    }
}