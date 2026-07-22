package com.n.alian.today

import android.app.Application
import androidx.work.Configuration
import com.n.alian.today.data.local.AppDatabase
import com.n.alian.today.data.repository.TaskRepository
import com.n.alian.today.worker.RolloverScheduler
import com.n.alian.today.worker.TodayWorkerFactory

/**
 * [Configuration.Provider] لأن جدولة الترحيل اليومي تحتاج [TaskRepository]
 * محقوناً يدوياً في الـ Worker (بدون Hilt) — هذا يتطلب تعطيل التهيئة
 * التلقائية الافتراضية لـ WorkManager في AndroidManifest.
 */
class TodayApp : Application(), Configuration.Provider {
    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val repository: TaskRepository by lazy { TaskRepository(database.taskDao()) }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(TodayWorkerFactory(repository))
            .build()

    override fun onCreate() {
        super.onCreate()
        RolloverScheduler.schedule(this)
    }
}