package com.antcashmanager.android.ui.components.animation

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 80.dp,
    cornerRadius: Int = 12,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton_shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmer_alpha",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = shimmerAlpha),
                shape = RoundedCornerShape(cornerRadius.dp),
            ),
    )
}

@Composable
fun TransactionSkeletonLoader(
    modifier: Modifier = Modifier,
    itemCount: Int = 5,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        items(itemCount) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
            ) {
                SkeletonLoader(height = 16.dp, cornerRadius = 8)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SkeletonLoader(
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp),
                        cornerRadius = 6,
                    )
                    SkeletonLoader(
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp),
                        cornerRadius = 6,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                SkeletonLoader(height = 20.dp, cornerRadius = 8)
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Preview(name = "SkeletonLoaders - Light", showBackground = true)
@Composable
private fun SkeletonLoadersPreviewLight() {
    AntCashManagerTheme(dynamicColor = false) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            SkeletonLoader(height = 48.dp)
        }
    }
}

@Preview(
    name = "SkeletonLoaders - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SkeletonLoadersPreviewDark() {
    SkeletonLoadersPreviewLight()
}

@Preview(name = "SkeletonLoaders - Accessibility", showBackground = true, fontScale = 1.5f)
@Composable
private fun SkeletonLoadersPreviewAccessibility() {
    SkeletonLoadersPreviewLight()
}
