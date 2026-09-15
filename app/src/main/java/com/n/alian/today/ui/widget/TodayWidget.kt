package com.n.alian.today.ui.widget


import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider         // ← ✅ لازم يكون color، مش unit
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider as ColorProviderUnit
import com.n.alian.today.MainActivity
import com.n.alian.today.R
import com.n.alian.today.TodayApp
import com.n.alian.today.data.local.Category
import com.n.alian.today.data.local.Priority
import com.n.alian.today.data.local.Task
import com.n.alian.today.data.repository.startOfDayMillis
import java.util.Calendar

class TodayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as TodayApp).repository

        provideContent {
            // ⚠️ لا تستبدل collectAsState بـ first() — راجع خطأ رقم ٧ بالـ log
            val tasks by repository.todayTasksByPriority()
                .collectAsState(initial = emptyList())

            val doneToday by repository.doneTodayCount(startOfDayMillis())
                .collectAsState(initial = 0)

            WidgetContent(
                context = context,
                tasks = tasks,
                doneToday = doneToday
            )
        }
    }
}

// ─────────────────────────────────────────────────────────
//  التخطيط الرئيسي
// ─────────────────────────────────────────────────────────

@Composable
private fun WidgetContent(
    context: Context,
    tasks: List<Task>,
    doneToday: Int
) {
    val bgColor = ColorProvider(day = Color(0xFFF6F8F6), night = Color(0xFF181C19))
    val textMain = ColorProvider(day = Color(0xFF181B19), night = Color(0xFFE1E6E1))
    val textSub = ColorProvider(day = Color(0xFF5C615C), night = Color(0xFFA2A9A2))
    val dividerColor = ColorProvider(day = Color(0xFFE2E6E1), night = Color(0xFF39403A))
    val primary = ColorProvider(day = Color(0xFF2E9E67), night = Color(0xFF6ED7A5))

    val openAppAction = actionStartActivity(
        ComponentName(context.packageName, MainActivity::class.java.name)
    )

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .cornerRadius(24.dp)
            .padding(14.dp)
            .clickable(openAppAction),
        contentAlignment = Alignment.TopStart
    ) {
        if (tasks.isEmpty()) {
            EmptyWidget(textMain = textMain, textSub = textSub)
        } else {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) {
                FocusTaskRow(
                    task = tasks[0],
                    textMain = textMain,
                    textSub = textSub
                )

                if (tasks.size > 1) {
                    Spacer(modifier = GlanceModifier.height(6.dp))
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(dividerColor)
                    ) {}
                    Spacer(modifier = GlanceModifier.height(6.dp))

                    SecondaryTaskRow(
                        task = tasks[1],
                        textMain = textMain,
                        textSub = textSub
                    )
                }

                Spacer(modifier = GlanceModifier.height(8.dp))
                Footer(
                    remaining = (tasks.size - 1).coerceAtLeast(0),
                    doneToday = doneToday,
                    primary = primary,
                    textSub = textSub
                )
            }
        }
    }

}

// ─────────────────────────────────────────────────────────
//  المهمة الرئيسية
// ─────────────────────────────────────────────────────────

@Composable
private fun FocusTaskRow(
    task: Task,
    textMain: ColorProviderUnit,
    textSub: ColorProviderUnit
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        GlanceCheckbox(task = task, size = 22)

        Spacer(modifier = GlanceModifier.width(10.dp))

        Column(modifier = GlanceModifier.defaultWeight()) {
            if (task.priority != Priority.MEDIUM || task.category != Category.NONE) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = GlanceModifier.padding(bottom = 2.dp)
                ) {
                    PriorityDot(priority = task.priority)
                    if (task.category != Category.NONE) {
                        Spacer(modifier = GlanceModifier.width(4.dp))
                        Text(
                            text = task.category.label,
                            style = TextStyle(
                                color = textSub,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            Text(
                text = task.title,
                style = TextStyle(
                    color = textMain,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 2
            )

            task.dueDate?.let { due ->
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = formatDueDateSmart(due),
                    style = TextStyle(
                        color = textSub,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
//  المهمة الثانوية
// ─────────────────────────────────────────────────────────

@Composable
private fun SecondaryTaskRow(
    task: Task,
    textMain: ColorProviderUnit,
    textSub: ColorProviderUnit
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlanceCheckbox(task = task, size = 18)

        Spacer(modifier = GlanceModifier.width(8.dp))

        Text(
            text = task.title,
            style = TextStyle(
                color = textMain,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            ),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight()
        )

        if (task.priority == Priority.HIGH) {
            PriorityDot(priority = task.priority)
        }
    }
}

// ─────────────────────────────────────────────────────────
//  Checkbox (نسخة Glance)
// ─────────────────────────────────────────────────────────

@Composable
private fun GlanceCheckbox(task: Task, size: Int) {
    val isDone = task.isDone
    val primary = ColorProvider(day = Color(0xFF2E9E67), night = Color(0xFF6ED7A5))
    val transparent = ColorProvider(day = Color(0x00000000), night = Color(0x00000000))

    Box(
        modifier = GlanceModifier
            .size(size.dp)
            .background(if (isDone) primary else transparent)
            .cornerRadius((size / 2).dp)
            .clickable(
                onClick = actionRunCallback<CompleteTaskAction>(
                    actionParametersOf(taskIdKey to task.id)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!isDone) {
            Image(
                provider = ImageProvider(R.drawable.ic_circle_outline),
                contentDescription = null,
                modifier = GlanceModifier.size(size.dp)
            )
        } else {
            Image(
                provider = ImageProvider(R.drawable.ic_check_widget),
                contentDescription = "منجزة",
                modifier = GlanceModifier.size((size * 0.6f).dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────
//  نقطة الأولوية
// ─────────────────────────────────────────────────────────

@Composable
private fun PriorityDot(priority: Priority) {
    val dotColor = when (priority) {
        Priority.HIGH -> ColorProvider(day = Color(0xFFD93F3F), night = Color(0xFFFF7A7A))
        Priority.MEDIUM -> ColorProvider(day = Color(0xFF2E9E67), night = Color(0xFF6ED7A5))
        Priority.LOW -> ColorProvider(day = Color(0xFFBDBDBD), night = Color(0xFF6E6E6E))
    }
    Box(
        modifier = GlanceModifier
            .size(8.dp)
            .background(dotColor)
            .cornerRadius(4.dp)
    ) {}
}

// ─────────────────────────────────────────────────────────
//  الفوتر
// ─────────────────────────────────────────────────────────

@Composable
private fun Footer(
    remaining: Int,
    doneToday: Int,
    primary: ColorProviderUnit,
    textSub: ColorProviderUnit
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (doneToday > 0) {
            Text(
                text = "✓ $doneToday منجزة اليوم",
                style = TextStyle(
                    color = primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
        Spacer(modifier = GlanceModifier.defaultWeight())
        if (remaining > 0) {
            Text(
                text = "+$remaining إضافية",
                style = TextStyle(
                    color = textSub,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────
//  الحالة الفارغة
// ─────────────────────────────────────────────────────────

@Composable
private fun EmptyWidget(
    textMain: ColorProviderUnit,
    textSub: ColorProviderUnit
) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🎉",
            style = TextStyle(fontSize = 32.sp)
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = "لا مهام اليوم",
            style = TextStyle(
                color = textMain,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = GlanceModifier.height(2.dp))
        Text(
            text = "استمتع بوقتك",
            style = TextStyle(
                color = textSub,
                fontSize = 11.sp
            )
        )
    }
}

// ─────────────────────────────────────────────────────────
//  دالة ذكية لتنسيق التاريخ
// ─────────────────────────────────────────────────────────

private fun formatDueDateSmart(millis: Long): String {
    val now = Calendar.getInstance()
    val due = Calendar.getInstance().apply { timeInMillis = millis }

    val isToday = now.get(Calendar.YEAR) == due.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == due.get(Calendar.DAY_OF_YEAR)

    val tomorrow = Calendar.getInstance().apply {
        timeInMillis = now.timeInMillis
        add(Calendar.DAY_OF_YEAR, 1)
    }
    val isTomorrow = tomorrow.get(Calendar.YEAR) == due.get(Calendar.YEAR) &&
            tomorrow.get(Calendar.DAY_OF_YEAR) == due.get(Calendar.DAY_OF_YEAR)

    val yesterday = Calendar.getInstance().apply {
        timeInMillis = now.timeInMillis
        add(Calendar.DAY_OF_YEAR, -1)
    }
    val isYesterday = yesterday.get(Calendar.YEAR) == due.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == due.get(Calendar.DAY_OF_YEAR)

    return when {
        isYesterday -> "⚠️ متأخرة من أمس"
        millis < now.timeInMillis && !isToday -> "⚠️ متأخرة"
        isToday -> "📅 يستحق اليوم"
        isTomorrow -> "📅 بكرة"
        else -> {
            val monthsAr = listOf(
                "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
                "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
            )
            "📅 ${due.get(Calendar.DAY_OF_MONTH)} ${monthsAr[due.get(Calendar.MONTH)]}"
        }
    }
}