package com.antcashmanager.android.ui.components.animation

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

@Composable
fun AnimatedCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    border: BorderStroke? = null,
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
            .alpha(alpha.value),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = border,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(borderRadius.dp),
    ) {
        content()
    }
}

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

@Preview(name = "CardAnimations - Light", showBackground = true)
@Composable
private fun CardAnimationsPreviewLight() {
    AntCashManagerTheme(dynamicColor = false) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            AnimatedCard {
                AppText(
                    text = "Animated card",
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Preview(
    name = "CardAnimations - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun CardAnimationsPreviewDark() {
    CardAnimationsPreviewLight()
}

@Preview(name = "CardAnimations - Accessibility", showBackground = true, fontScale = 1.5f)
@Composable
private fun CardAnimationsPreviewAccessibility() {
    CardAnimationsPreviewLight()
}
