package com.n.alian.today.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.n.alian.today.TodayApp
import com.n.alian.today.data.local.Bucket
import com.n.alian.today.data.local.Task
import com.n.alian.today.ui.theme.Spacing

class TodayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as TodayApp).repository

        provideContent {
            // ✅ نراقب الـ Flow مباشرة جوا الـ composition
            // كل ما تتغير قاعدة البيانات، هاي الشاشة بتعيد الرسم تلقائياً
            val tasks by repository.activeTasks(Bucket.TODAY).collectAsState(initial = emptyList())

            val focusTask = tasks.firstOrNull()
            val remainingCount = (tasks.size - 1).coerceAtLeast(0)

            WidgetContent(
                focusTask = focusTask,
                remainingCount = remainingCount
            )
        }
    }
}

@Composable
private fun WidgetContent(
    focusTask: Task?,
    remainingCount: Int
) {
    val textColor = ColorProvider(day = Color(0xFF1C1B1F), night = Color(0xFFE6E1E5))
    val backgroundColor = ColorProvider(day = Color(0xFF1B6B4F), night = Color(0xFF7FD6B3))

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (focusTask == null) {
            Text(
                text = "لا مهام اليوم 🎉",
                style = TextStyle(fontWeight = FontWeight.Bold, color = textColor)
            )
        } else {
            Text(
                text = focusTask.title,
                style = TextStyle(fontWeight = FontWeight.Bold, color = textColor)
            )
            if (remainingCount > 0) {
                Spacer(modifier = GlanceModifier.height(Spacing.small))
                Text(
                    text = "+$remainingCount إضافية",
                    style = TextStyle(color = textColor)
                )
            }
            Spacer(modifier = GlanceModifier.height(Spacing.medium))
            Text(
                text = "✓ تم",
                style = TextStyle(fontWeight = FontWeight.Medium, color = textColor),
                modifier = GlanceModifier.clickable(
                    onClick = actionRunCallback<CompleteTaskAction>(
                        actionParametersOf(taskIdKey to focusTask.id)
                    )
                )
            )
        }
    }
}