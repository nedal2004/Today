package com.n.alian.today.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.n.alian.today.ui.theme.Spacing
import java.util.Calendar

// ── التاريخ بالعربي (offline — بدون مكتبات ICU) ──
private val arabicMonths = listOf(
    "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
    "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
)
private val arabicDays = listOf(
    "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت"
)

/** "الثلاثاء، ٨ سبتمبر" */
private fun todayDateLabel(): String {
    val now = Calendar.getInstance()
    val dayName = arabicDays[now.get(Calendar.DAY_OF_WEEK) - 1]   // 1 = الأحد
    val monthName = arabicMonths[now.get(Calendar.MONTH)]          // 0 = يناير
    val dayOfMonth = now.get(Calendar.DAY_OF_MONTH)//.arabicDigits()
    return "$dayName، $dayOfMonth $monthName"
}

/** 8 → ٨ — أرقام عربية للإحساس العربي الأول */
//private fun Int.arabicDigits(): String =
 //   toString().joinToString("") { "٠١٢٣٤٥٦٧٨٩"[it.digitToInt()].toString() }

@Composable
fun TodayHeader(
    doneToday: Int,
    totalToday: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "اليوم",
                style = MaterialTheme.typography.headlineSmall,   // Cairo Bold 26
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Spacing.extraSmall))
            Text(
                text = todayDateLabel(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        ProgressRing(done = doneToday, total = totalToday)
    }
}

/** حلقة التقدم: قاعدة رمادية + قوس أخضر متحرك + العدّاد بالنص */
@Composable
private fun ProgressRing(done: Int, total: Int, modifier: Modifier = Modifier) {
    val progress = if (total == 0) 0f else done.toFloat() / total

    // ✅ أنيميشن ناعم: كل ما يتغير done، القوس بيمشي لحاله
    val animatedSweep by animateFloatAsState(
        targetValue = progress * 360f,
        label = "progressRing"
    )

    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // القاعدة (المسار الرمادي الكامل)
            drawArc(
                color = androidx.compose.ui.graphics.Color(0xFFE7E7E0),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(3.dp.toPx(), 3.dp.toPx()),
                size = Size(size.width - 6.dp.toPx(), size.height - 6.dp.toPx()),
                style = Stroke(width = 5.dp.toPx())
            )
            // قوس الإنجاز (بيبدأ من ١٢ ساعة = -90 درجة)
            if (progress > 0f) {
                drawArc(
                    color = androidx.compose.ui.graphics.Color(0xFF1B6B4F),
                    startAngle = -90f,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(3.dp.toPx(), 3.dp.toPx()),
                    size = Size(size.width - 6.dp.toPx(), size.height - 6.dp.toPx()),
                    style = Stroke(width = 5.dp.toPx())
                )
            }
        }
        Text(
            text = "$done/$total",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}