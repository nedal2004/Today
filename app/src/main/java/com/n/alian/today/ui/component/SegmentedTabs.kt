package com.n.alian.today.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.n.alian.today.data.local.Bucket
import com.n.alian.today.ui.theme.Spacing

/**
 * Segmented control بديل عن TabRow:
 * حاوية مستديرة واحدة، والتبويب المختار بيطفو فوقها ككبسولة بلون الـ surface.
 */
@Composable
fun SegmentedTabs(
    selected: Bucket,
    onSelect: (Bucket) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium, vertical = Spacing.small)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(Spacing.extraSmall),
        horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
    ) {
        Bucket.entries.forEach { bucket ->
            val isSelected = bucket == selected

            // أنيميشن ناعم: الخلفية والنص بيتبدلوا تدريجياً
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surfaceContainer,
                label = "tabBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "tabText"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .shadow(
                        elevation = if (isSelected) 2.dp else 0.dp,
                        shape = RoundedCornerShape(50)
                    )
                    .clip(RoundedCornerShape(50))
                    .background(bgColor)
                    .clickable { onSelect(bucket) }
                    .padding(vertical = Spacing.small),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = bucket.displayName(),
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor
                )
            }
        }
    }
}

fun Bucket.displayName(): String = when (this) {
    Bucket.TODAY -> "اليوم"
    Bucket.TOMORROW -> "بكرة"
    Bucket.LATER -> "لاحقاً"
}