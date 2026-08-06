package com.n.alian.today.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.n.alian.today.TodayApp
import com.n.alian.today.data.local.Bucket
import com.n.alian.today.ui.theme.Spacing
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull

class TodayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as TodayApp).repository

        // نمهل جلب البيانات ٣ ثواني بس — لو تأخرت أكتر، نعرض قائمة فاضية
        // بدل ما الـ widget يفضل عالق بحالة تحميل للأبد
        val tasks = withTimeoutOrNull(3000) {
            repository.activeTasks(Bucket.TODAY).firstOrNull()
        } ?: emptyList()

        val focusTask = tasks.firstOrNull()
        val remainingCount = (tasks.size - 1).coerceAtLeast(0)

        provideContent {
            WidgetContent(
                focusTitle = focusTask?.title,
                remainingCount = remainingCount
            )
        }
    }
}

@Composable
private fun WidgetContent(focusTitle: String?, remainingCount: Int) {
    val textColor = ColorProvider(
        day = Color(0xFF1C1B1F),
        night = Color(0xFFE6E1E5)
    )
    val backgroundColor = ColorProvider(
        day = Color(0xFF1B6B4F),
        night = Color(0xFF7FD6B3)
    )
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (focusTitle == null) {
            Text(
                text = "لا مهام اليوم 🎉",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )
        } else {
            Text(
                text = focusTitle,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )
            if (remainingCount > 0) {
                Spacer(modifier = GlanceModifier.height(Spacing.small))
                Text(
                    text = "+$remainingCount إضافية",
                    style = TextStyle(color = textColor)
                )
            }
        }
    }
}