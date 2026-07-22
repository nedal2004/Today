package com.n.alian.today.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.n.alian.today.data.repository.TaskRepository

/**
 * يحقن [TaskRepository] يدوياً في الـ Workers بدون أي DI framework،
 * تفادياً لأي over-engineering في مشروع بحجم "Today".
 */
class TodayWorkerFactory(
    private val repository: TaskRepository
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? = when (workerClassName) {
        DailyRolloverWorker::class.java.name ->
            DailyRolloverWorker(appContext, workerParameters, repository)
        else -> null
    }
}
