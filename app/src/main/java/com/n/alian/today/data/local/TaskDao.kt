package com.n.alian.today.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: Long): Task?

    // ── قراءة ──
    @Query("SELECT * FROM tasks WHERE bucket = :bucket AND isDone = 0 ORDER BY sortOrder ASC")
    fun observeActive(bucket: Bucket): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isDone = 1 AND completedAt >= :startOfDay ORDER BY completedAt DESC")
    fun observeDoneToday(startOfDay: Long): Flow<List<Task>>

    // ── عدّادات الهيدر (خفيفة — COUNT بدل SELECT *) ──
    @Query("SELECT COUNT(*) FROM tasks WHERE bucket = 'TODAY' AND isDone = 0")
    fun observeActiveTodayCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE isDone = 1 AND completedAt >= :startOfDay")
    fun observeDoneTodayCount(startOfDay: Long): Flow<Int>

    // ── كتابة ──

    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    // ── الترحيل اليومي ──

    @Query("UPDATE tasks SET bucket = 'TODAY' WHERE bucket = 'TOMORROW' AND isDone = 0")
    suspend fun promoteTomorrowToToday()

    @Query("DELETE FROM tasks WHERE isDone = 1 AND completedAt < :startOfDay")
    suspend fun archiveOldDone(startOfDay: Long)
}