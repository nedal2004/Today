package com.n.alian.today.ui.component

import java.util.Calendar

val monthsAr = listOf(
    "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
    "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
)

// "٨ سبتمبر"
fun formatDueDate(millis: Long): String {
    val c = Calendar.getInstance().apply { timeInMillis = millis }
    return "${c.get(Calendar.DAY_OF_MONTH)} ${monthsAr[c.get(Calendar.MONTH)]}"
}