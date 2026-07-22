package com.n.alian.today.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/** يجدول [DailyRolloverWorker] ليعمل كل ٢٤ ساعة بدءاً من منتصف الليل القادم. */
object RolloverScheduler {

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<DailyRolloverWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(millisUntilNextMidnight(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DailyRolloverWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun millisUntilNextMidnight(): Long {
        val now = Calendar.getInstance()
        val nextMidnight = (now.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return nextMidnight.timeInMillis - now.timeInMillis
    }
}
