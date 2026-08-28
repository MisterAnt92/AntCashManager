package com.antcashmanager.android.ui.components.animation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.layout.HorizontalSpacer
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.text.AppText
import kotlinx.coroutines.delay

@Composable
fun AntSplashScreen(
    onAnimationFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accessibilityManager = remember {
        context.getSystemService(android.content.Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
    }
    val isAccessibilityEnabled =
        accessibilityManager.isEnabled && accessibilityManager.isTouchExplorationEnabled

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BouncingAnt(modifier = Modifier.size(180.dp))

            VerticalSpacer(SpacingSize.XL)

            AppText(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Skip button - always available but especially prominent/needed for accessibility
        TextButton(
            onClick = onAnimationFinished,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            AppText(text = stringResource(R.string.tutorial_skip))
        }
    }

    LaunchedEffect(Unit) {
        // Automatically finish after 3 seconds if not skipped
        delay(3000)
        onAnimationFinished()
    }
}

@Composable
fun BouncingAnt(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "AntBounce")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scale"
    )

    val translationY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TranslationY"
    )

    Image(
        painter = painterResource(id = R.drawable.ic_ant_mascot),
        contentDescription = null,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.translationY = translationY
            }
    )
}

@Composable
fun AntEasterEggAnimation(
    versionName: String,
    onDismiss: () -> Unit
) {
    var isRunning by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0.dp) }
    var offsetY by remember { mutableStateOf(0.dp) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            // Animate ant moving around for 2 seconds
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < 2000) {
                offsetX = ((-100..100).random()).dp
                offsetY = ((-100..100).random()).dp
                delay(200)
            }
            offsetX = 0.dp
            offsetY = 0.dp
            isRunning = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                // Content area with clickable ant and text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    BouncingAnt(
                        modifier = Modifier
                            .size(160.dp)
                            .offset(offsetX, offsetY)
                            .clickable { isRunning = true }
                    )
                    VerticalSpacer(SpacingSize.LG)
                    AppText(
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        text = stringResource(R.string.settings_easter_egg_message),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { isRunning = true }
                    )
                    VerticalSpacer(SpacingSize.XS)
                    AppText(
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        text = stringResource(R.string.extra_thanks),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    VerticalSpacer(SpacingSize.XS)
                    AppText(
                        text = "v$versionName",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Chiudi button at bottom center
                TextButton(onClick = onDismiss) {
                    AppText(
                        text = stringResource(R.string.common_close),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
