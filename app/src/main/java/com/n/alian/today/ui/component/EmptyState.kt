package com.n.alian.today.ui.component

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.n.alian.today.ui.theme.Spacing
import kotlinx.coroutines.delay

/**
 * حالة فارغة بأيقونة + أنيميشن دخول ناعم (fade + scale spring).
 * الأنيميشن بيبلش تلقائياً أول ما الـ Composable يدخل الشاشة —
 * مفيد لأن AnimatedContent بالشاشة الأم بيعيد تركيب هذا كل مرة يظهر فيها.
 */
@Composable
fun EmptyState(
    title: String,
    subTitle: String,
    icon: ImageVector
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffectVisible { visible = true }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "emptyStateScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "emptyStateAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.height(48.dp)
        )
        Spacer(modifier = Modifier.height(Spacing.small))
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(Spacing.small))
        Text(
            text = subTitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// يشغّل الأنيميشن مرة وحدة
@Composable
private fun LaunchedEffectVisible(onReady: () -> Unit) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        delay(16)
        onReady()
    }
}