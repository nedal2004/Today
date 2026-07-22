package com.n.alian.today.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.material3.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.action.ActionParameters
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.n.alian.today.MainActivity
import com.n.alian.today.TodayApp
import com.n.alian.today.data.local.Bucket
import com.nedal.today.R
import kotlinx.coroutines.flow.first

/**
 * ودجت "التركيز": يعرض المهمة التالية غير المنجزة اليوم وعدد المهام
 * المتبقية، مع زر إنجاز سريع. عمداً لا يعرض قائمة كاملة (كـ TaskListScreen)
 * حتى لا يتكرر منطق الشاشة داخل Glance — الفكرة هي التركيز على مهمة واحدة.
 */
class FocusWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as TodayApp).repository
        val tasks = repository.activeTasks(Bucket.TODAY).first()
        val nextTask = tasks.firstOrNull()

        val title = context.getString(R.string.widget_title)
        val doneLabel = context.getString(R.string.widget_mark_done)
        val bodyText = if (nextTask != null) {
            context.getString(R.string.widget_tasks_remaining, tasks.size)
        } else {
            context.getString(R.string.widget_no_tasks)
        }

        provideContent {
            GlanceTheme {
                FocusWidgetContent(
                    title = title,
                    nextTaskTitle = nextTask?.title,
                    bodyText = bodyText,
                    doneLabel = doneLabel,
                    nextTaskId = nextTask?.id
                )
            }
        }
    }
}

@Composable
private fun FocusWidgetContent(
    title: String,
    nextTaskTitle: String?,
    bodyText: String,
    doneLabel: String,
    nextTaskId: Int?
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        Text(
            text = title,
            style = TextStyle(fontWeight = FontWeight.Bold, color = GlanceTheme.colors.primary)
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        if (nextTaskTitle != null) {
            Text(
                text = nextTaskTitle,
                style = TextStyle(color = GlanceTheme.colors.onSurface)
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
        }
        Text(
            text = bodyText,
            style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
        )
        if (nextTaskId != null) {
            Spacer(modifier = GlanceModifier.height(8.dp))
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(GlanceTheme.colors.primaryContainer)
                    .padding(8.dp)
                    .clickable(
                        actionRunCallback<CompleteNextTaskAction>(
                            actionParametersOf(TaskIdKey to nextTaskId)
                        )
                    )
            ) {
                Text(
                    text = doneLabel,
                    style = TextStyle(color = GlanceTheme.colors.onPrimaryContainer)
                )
            }
        }
    }
}

private val TaskIdKey = ActionParameters.Key<Int>("task_id")

/** ينجز المهمة التالية مباشرة من الودجت ثم يحدّث عرضها. */
class CompleteNextTaskAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val taskId = parameters[TaskIdKey] ?: return
        val repository = (context.applicationContext as TodayApp).repository
        val task = repository.activeTasks(Bucket.TODAY).first().find { it.id == taskId } ?: return
        repository.complete(task)
        FocusWidget().updateAll(context)
    }
}
