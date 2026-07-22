package com.n.alian.today.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.n.alian.today.data.repository.TaskRepository
import com.n.alian.today.widget.TodayWidget
import java.util.Calendar

/**
 * ينفّذ الترحيل اليومي: يرقّي مهام TOMORROW غير المنجزة إلى TODAY،
 * ويؤرشف (يحذف) المهام المنجزة قبل بداية اليوم الحالي.
 * تتم جدولتها دورياً كل ٢٤ ساعة عبر [RolloverScheduler].
 */
class DailyRolloverWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val repository: TaskRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = try {
        repository.runDailyRollover(startOfToday())
        // الترحيل يغيّر ما يظهر في الودجت (بكت TODAY)، لذا يُحدَّث معه.
        TodayWidget().updateAll(applicationContext)
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }

    companion object {
        const val WORK_NAME = "daily_rollover"

        private fun startOfToday(): Long =
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
    }
}
