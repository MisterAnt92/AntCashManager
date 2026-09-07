package com.antcashmanager.android.ui.components.animation

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

@Composable
fun ShimmerGradientBackground(
    modifier: Modifier = Modifier,
    color1: Color,
    color2: Color,
    height: androidx.compose.ui.unit.Dp = 120.dp,
    content: @Composable () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "offset",
    )

    val gradientColors = listOf(color1, color2, color1)
    val brush =
        Brush.linearGradient(
            colors = gradientColors,
            start = Offset(offset * 500, 0f),
            end = Offset(offset * 500 + 500, 0f),
        )

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(height)
                .background(brush, shape = RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
fun PulsingElement(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "pulse_scale",
    )

    Box(
        modifier = modifier.scale(scale),
    ) {
        content()
    }
}

@Composable
fun BouncingElement(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")

    Box(modifier = modifier) {
        Box(
            modifier = Modifier,
        ) {
            content()
        }
    }
}

@Preview(name = "EffectAnimations - Light", showBackground = true)
@Composable
private fun EffectAnimationsPreviewLight() {
    AntCashManagerTheme(dynamicColor = false) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            ShimmerGradientBackground(
                color1 = MaterialTheme.colorScheme.primary,
                color2 = MaterialTheme.colorScheme.secondary,
            ) {
                AppText(text = "Shimmer effect")
            }
        }
    }
}

@Preview(
    name = "EffectAnimations - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun EffectAnimationsPreviewDark() {
    EffectAnimationsPreviewLight()
}

@Preview(name = "EffectAnimations - Accessibility", showBackground = true, fontScale = 1.5f)
@Composable
private fun EffectAnimationsPreviewAccessibility() {
    EffectAnimationsPreviewLight()
}
