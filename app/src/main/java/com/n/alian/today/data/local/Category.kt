package com.n.alian.today.data.local

/** تصنيف المهمة — أساس الفلترة المستقبلية (شاشة/Query لاحقاً) */
enum class Category(val label: String) {
    NONE("بدون تصنيف"),
    PERSONAL("شخصي"),
    STUDY("دراسي"),
    WORK("عمل"),
    APPOINTMENTS("مواعيد")
}