package com.antcashmanager.android.ui.components.animation

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

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
        AppText(
            text = targetValue,
            modifier =
                Modifier
                    .animateContentSize(
                        animationSpec = tween(animationDurationMillis, easing = FastOutSlowInEasing),
                    ),
        )
    }
}

@Composable
fun AnimatedListItem(
    modifier: Modifier = Modifier,
    index: Int = 0,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = true,
        enter =
            slideInVertically(
                initialOffsetY = { 50 },
                animationSpec = tween(300 + index * 50),
            ) + fadeIn(animationSpec = tween(300 + index * 50)),
        label = "list_item",
    ) {
        content()
    }
}

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

@Composable
fun SlideInOnAppear(
    modifier: Modifier = Modifier,
    durationMillis: Int = 400,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = true,
        enter =
            slideInVertically(
                initialOffsetY = { 50 },
                animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
            ) + fadeIn(animationSpec = tween(durationMillis)),
        label = "slide_in",
    ) {
        content()
    }
}

@Preview(name = "ContentAnimations - Light", showBackground = true)
@Composable
private fun ContentAnimationsPreviewLight() {
    AntCashManagerTheme(dynamicColor = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedCounter(value = "42")
            SlideInOnAppear {
                AppText(text = "Sliding in content")
            }
        }
    }
}

@Preview(
    name = "ContentAnimations - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ContentAnimationsPreviewDark() {
    ContentAnimationsPreviewLight()
}

@Preview(name = "ContentAnimations - Accessibility", showBackground = true, fontScale = 1.5f)
@Composable
private fun ContentAnimationsPreviewAccessibility() {
    ContentAnimationsPreviewLight()
}
