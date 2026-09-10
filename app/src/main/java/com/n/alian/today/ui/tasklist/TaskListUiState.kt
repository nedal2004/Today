package com.n.alian.today.ui.tasklist

import com.n.alian.today.data.local.Bucket
import com.n.alian.today.data.local.Task

data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val selectedBucket: Bucket = Bucket.TODAY,
    val doneToday: Int = 0,        // ← جديد: منجز اليوم
    val totalToday: Int = 0,       // ← جديد: كل مهام اليوم (نشطة + منجزة)
    val isLoading: Boolean = true,
    val error: String? = null
)