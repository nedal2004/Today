package com.n.alian.today.ui.widget

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.n.alian.today.TodayApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// مفتاح نمرر فيه id المهمة مع الضغطة على الزر
val taskIdKey = ActionParameters.Key<Long>("task_id")

object CompleteTaskAction : ActionCallback {

    private const val TAG = "CompleteTaskAction"

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        try {
            val taskId = parameters[taskIdKey] ?: return

            val repository = (context.applicationContext as TodayApp).repository
            val task = repository.getTaskById(taskId) ?: return   // المهمة ممكن تكون اتحذفت، نتعامل بأمان

            repository.update(
                task.copy(isDone = true, completedAt = System.currentTimeMillis())
            )
            Log.d(TAG, "✅ تم شطب المهمة: ${task.title}")

            withContext(Dispatchers.Main.immediate) {
                TodayWidget().updateAll(context.applicationContext)
                Log.d(TAG, "🔄 Widget updated")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ فشل الشطب من الـ Widget", e)
        }
    }
}