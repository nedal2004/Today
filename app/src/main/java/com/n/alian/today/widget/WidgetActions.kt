package com.n.alian.today.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.n.alian.today.TodayApp
import com.n.alian.today.data.local.Bucket
import kotlinx.coroutines.flow.first

/** مفتاح تمرير id المهمة المعروضة على الودجت إلى [CompleteFocusTaskAction]. */
val FocusTaskIdKey = ActionParameters.Key<Int>("focus_task_id")

/** ينجز مهمة الـ Focus المعروضة على الودجت مباشرة، ثم يحدّث الودجت. */
class CompleteFocusTaskAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val taskId = parameters[FocusTaskIdKey] ?: return
        val repository = (context.applicationContext as TodayApp).repository
        val task = repository.activeTasks(Bucket.TODAY).first().find { it.id == taskId } ?: return
        repository.complete(task)
        TodayWidget().updateAll(context)
    }
}
