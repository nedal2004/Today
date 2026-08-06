package com.n.alian.today.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.n.alian.today.TodayApp
import com.n.alian.today.data.markRolloverRanToday
import com.n.alian.today.data.repository.startOfDayMillis
import kotlinx.coroutines.delay

class DailyRolloverWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        delay(3000)

        Log.d("DailyRolloverWorker", "بدء عملية تدوير المهام اليومية...")

        return try {

            val app = applicationContext as TodayApp
            val repository = app.repository

            repository.runDailyRollover(startOfDayMillis())

            // تسجيل أن الترحيل تم اليوم
            markRolloverRanToday(applicationContext)

            Log.d("DailyRolloverWorker", "تمت العملية بنجاح")

            Result.success()

        } catch (e: Exception) {

            Log.e("DailyRolloverWorker", "فشلت العملية: ${e.message}")

            Result.retry()
        }
    }
}