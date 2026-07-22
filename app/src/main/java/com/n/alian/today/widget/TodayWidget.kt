package com.n.alian.today.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.material3.GlanceTheme
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.n.alian.today.MainActivity
import com.n.alian.today.TodayApp
import com.n.alian.today.data.local.Bucket
import com.nedal.today.R
import kotlinx.coroutines.flow.first

/**
 * ودجت "التركيز" (Focus mode): يعرض أهم مهمة الآن بخط كبير + عدّاد "+N more"
 * للباقي، بدل قائمة مزدحمة (أول مهمة بترتيب TODAY = الـ Focus). الألوان من
 * GlanceTheme.colors (glance-material3) فتدعم Material You على أندرويد 12+
 * تلقائياً وتتساقط (fallback) لثيم ثابت على ما دونها — بدون أي قيمة hardcoded.
 */
class TodayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as TodayApp).repository
        val tasks = repository.activeTasks(Bucket.TODAY).first()
        val focusTask = tasks.firstOrNull()
        val moreCount = (tasks.size - 1).coerceAtLeast(0)

        val noTasksMessage = context.getString(R.string.widget_no_tasks)
        val moreTasksLabel = if (moreCount > 0) {
            context.getString(R.string.widget_more_tasks, moreCount)
        } else {
            null
        }
        val doneLabel = context.getString(R.string.widget_mark_done)
        val addTaskDescription = context.getString(R.string.cd_widget_add_task)

        provideContent {
            GlanceTheme {
                TodayWidgetContent(
                    focusTaskTitle = focusTask?.title,
                    focusTaskId = focusTask?.id,
                    moreTasksLabel = moreTasksLabel,
                    noTasksMessage = noTasksMessage,
                    doneLabel = doneLabel,
                    addTaskDescription = addTaskDescription
                )
            }
        }
    }
}

@Composable
private fun TodayWidgetContent(
    focusTaskTitle: String?,
    focusTaskId: Int?,
    moreTasksLabel: String?,
    noTasksMessage: String,
    doneLabel: String,
    addTaskDescription: String
) {
    val context = LocalContext.current

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        if (focusTaskTitle != null && focusTaskId != null) {
            Text(
                text = focusTaskTitle,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                )
            )
            if (moreTasksLabel != null) {
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = moreTasksLabel,
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
                )
            }
            Spacer(modifier = GlanceModifier.height(8.dp))
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                Row(
                    modifier = GlanceModifier
                        .background(GlanceTheme.colors.primaryContainer)
                        .padding(8.dp)
                        .clickable(
                            actionRunCallback<CompleteFocusTaskAction>(
                                actionParametersOf(FocusTaskIdKey to focusTaskId)
                            )
                        )
                ) {
                    Text(
                        text = doneLabel,
                        style = TextStyle(color = GlanceTheme.colors.onPrimaryContainer)
                    )
                }
                Spacer(modifier = GlanceModifier.width(8.dp))
                AddTaskRow(context, addTaskDescription)
            }
        } else {
            Text(
                text = noTasksMessage,
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            AddTaskRow(context, addTaskDescription)
        }
    }
}

@Composable
private fun AddTaskRow(context: Context, description: String) {
    Row(
        modifier = GlanceModifier
            .background(GlanceTheme.colors.secondaryContainer)
            .padding(8.dp)
            .clickable(actionStartActivity(openAddTaskIntent(context)))
    ) {
        Text(
            text = "+ $description",
            style = TextStyle(color = GlanceTheme.colors.onSecondaryContainer)
        )
    }
}

private fun openAddTaskIntent(context: Context): Intent =
    Intent(context, MainActivity::class.java).apply {
        putExtra(MainActivity.EXTRA_OPEN_ADD_DIALOG, true)
    }
