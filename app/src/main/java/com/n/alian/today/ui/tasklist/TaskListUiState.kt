package com.n.alian.today.ui.tasklist

import com.n.alian.today.data.local.Task
import com.n.alian.today.data.local.Bucket

data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val selectedBucket: Bucket = Bucket.TODAY,
    val isLoading: Boolean = true,
    val error: String? = null
)