package com.n.alian.today.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.n.alian.today.R

// خط التطبيق — Cairo Variable Font عبر font-family XML (offline 100%)
val Cairo = FontFamily(
    Font(R.font.cairo_family)
)

//  "تركيز جريء": عناوين Bold كبيرة، نصوص Normal
// letterSpacing = 0 دائماً — الـ letterSpacing بيكسر اتصال الحروف العربية
val Typography = Typography(
    headlineSmall = TextStyle(   // عنوان الشاشة "اليوم"
        fontFamily = Cairo,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(      // عناوين الأقسام والحوارات
        fontFamily = Cairo,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(       // عناوين المهام
        fontFamily = Cairo,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(      // نصوص ثانوية (تواريخ، عدّادات)
        fontFamily = Cairo,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(      // أزرار الحوار
        fontFamily = Cairo,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(      // ميتاداتا صغيرة (عدّاد، تاريخ)
        fontFamily = Cairo,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    )
)