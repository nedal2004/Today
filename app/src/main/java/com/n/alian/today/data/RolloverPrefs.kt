package com.n.alian.today.data

import android.content.Context
import java.util.Calendar

private const val PREFS_NAME = "today_prefs"
private const val KEY_LAST_ROLLOVER_DAY = "last_rollover_day"

private fun currentDayKey(): Int {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
    return year * 1000 + dayOfYear
}

fun hasRolloverRunToday(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val lastRunDay = prefs.getInt(KEY_LAST_ROLLOVER_DAY, -1)
    return lastRunDay == currentDayKey()
}

fun markRolloverRanToday(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putInt(KEY_LAST_ROLLOVER_DAY, currentDayKey()).apply()
}