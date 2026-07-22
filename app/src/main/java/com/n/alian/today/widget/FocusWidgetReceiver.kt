package com.n.alian.today.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/** نقطة الدخول التي يستدعيها نظام الأندرويد لتحديث [FocusWidget]. */
class FocusWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FocusWidget()
}
