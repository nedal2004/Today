package com.n.alian.today

import com.n.alian.today.data.local.AppDatabase
import com.n.alian.today.data.repository.TaskRepository
import android.app.Application

class TodayApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val repository: TaskRepository by lazy { TaskRepository(database.taskDao()) }
}