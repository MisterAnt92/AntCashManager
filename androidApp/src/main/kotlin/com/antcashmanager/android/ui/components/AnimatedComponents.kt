package com.antcashmanager.android.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Animated card with scale and fade effect when appears
 */
@Composable
fun AnimatedCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderRadius: Int = 12,
    content: @Composable () -> Unit,
) {
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = tween(300, easing = FastOutSlowInEasing))
        alpha.animateTo(1f, animationSpec = tween(300, easing = FastOutSlowInEasing))
    }

    Card(
        modifier = modifier
            .scale(scale.value)
            .alpha(alpha.value)
            .shadow(8.dp, RoundedCornerShape(borderRadius.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(borderRadius.dp),
    ) {
        content()
    }
}

/**
 * Animated gradient background with moving shimmer effect
 */
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
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "offset",
    )

    val gradientColors = listOf(color1, color2, color1)
    val brush = Brush.linearGradient(
        colors = gradientColors,
        start = androidx.compose.ui.geometry.Offset(offset * 500, 0f),
        end = androidx.compose.ui.geometry.Offset(offset * 500 + 500, 0f),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(brush, shape = RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/**
 * Animated number counter for displaying values with animation
 */
@Composable
fun AnimatedCounter(
    value: String,
    modifier: Modifier = Modifier,
    animationDurationMillis: Int = 600,
) {
    AnimatedContent(
        targetState = value,
        modifier = modifier,
        label = "counter",
    ) { targetValue ->
        androidx.compose.material3.Text(
            text = targetValue,
            modifier = Modifier
                .animateContentSize(
                    animationSpec = tween(animationDurationMillis, easing = FastOutSlowInEasing)
                ),
        )
    }
}

/**
 * Animated expandable card
 */
@Composable
fun ExpandableAnimatedCard(
    title: @Composable () -> Unit,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(300)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            title()
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                content()
            }
        }
    }
}

/**
 * Animated list item entrance
 */
@Composable
fun AnimatedListItem(
    modifier: Modifier = Modifier,
    index: Int = 0,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            initialOffsetY = { 50 },
            animationSpec = tween(300 + index * 50),
        ) + fadeIn(animationSpec = tween(300 + index * 50)),
        label = "list_item",
    ) {
        content()
    }
}

/**
 * Pulsing animation for important elements
 */
@Composable
fun PulsingElement(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
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

/**
 * Bouncing animation
 */
@Composable
fun BouncingElement(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bounce_offset",
    )

    Box(modifier = modifier) {
        Box(
            modifier = Modifier,
        ) {
            content()
        }
    }
}

/**
 * Fade in animation on composition
 */
@Composable
fun FadeInOnAppear(
    modifier: Modifier = Modifier,
    durationMillis: Int = 500,
    content: @Composable () -> Unit,
) {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(durationMillis, easing = FastOutSlowInEasing))
    }

    Box(modifier = modifier.alpha(alpha.value)) {
        content()
    }
}

/**
 * Slide in animation
 */
@Composable
fun SlideInOnAppear(
    modifier: Modifier = Modifier,
    durationMillis: Int = 400,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            initialOffsetY = { 50 },
            animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
        ) + fadeIn(animationSpec = tween(durationMillis)),
        label = "slide_in",
    ) {
        content()
    }
}

