package com.n.alian.today

import android.app.Application
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.n.alian.today.data.hasRolloverRunToday
import com.n.alian.today.data.markRolloverRanToday
import com.n.alian.today.data.local.AppDatabase
import com.n.alian.today.data.repository.TaskRepository
import com.n.alian.today.data.repository.startOfDayMillis
import kotlinx.coroutines.*
import com.n.alian.today.work.DailyRolloverWorker
import java.util.concurrent.TimeUnit


class TodayApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    //  (Repository)
    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val repository: TaskRepository by lazy { TaskRepository(database.taskDao()) }

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            try {
                if (!hasRolloverRunToday(this@TodayApp)) {
                    repository.runDailyRollover(startOfDayMillis())
                    markRolloverRanToday(this@TodayApp)
                }
                schedulePeriodicRollover()
            } catch (e: Exception) {
                Log.e("TodayApp", "فشل الترحيل اليومي عند فتح التطبيق", e)
            }
        }
    }

    private fun schedulePeriodicRollover() {
        val request = PeriodicWorkRequestBuilder<DailyRolloverWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_rollover",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
