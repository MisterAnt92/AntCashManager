package com.antcashmanager.android.ui.components.dialog

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

// 500ms ensures WithAppLocale recomposition always completes before Activity.finish(),
// even if a language change is in progress (was 300ms + conditional 500ms via koinInject bug).
private const val EXIT_DIALOG_DELAY_MS = 500
private const val MASCOT_SCALE_ANIMATION_MS = 1000
private const val MASCOT_FLOAT_ANIMATION_MS = 1400
private const val MASCOT_SCALE_MIN = 0.94f
private const val MASCOT_SCALE_MAX = 1.04f
private const val MASCOT_FLOAT_MIN = -4f
private const val MASCOT_FLOAT_MAX = 4f

/**
 * Exit confirmation dialog with synchronized dismissal and exit handling.
 *
 * On Android 16 (API 35+), the dialog dismissal and Activity.finish() are properly
 * synchronized using LaunchedEffect with a 500ms delay to prevent race conditions,
 * including the case where a language change is in progress (WithAppLocale recomposition).
 *
 * @param onConfirmExit Called when user confirms exit. Must call Activity.safeFinish()
 * @param onDismiss Called when user cancels or dismisses the dialog
 * @param isVisible Controls dialog visibility
 */
@Composable
fun appExitConfirmationDialog(
    onConfirmExit: () -> Unit,
    onDismiss: () -> Unit,
    isVisible: Boolean = true,
) {
    val logger = Logger.withTag("AppExitDialog")
    val (shouldExit, setShouldExit) = remember { mutableStateOf(false) }

    // Synchronized exit handler for Android 16+ (API 35+)
    // 500ms delay ensures Compose finishes recomposition (including WithAppLocale locale changes)
    // before Activity.finish() is called. Keep outside the isVisible check so the
    // coroutine survives even after the parent sets showExitDialog = false.
    LaunchedEffect(shouldExit) {
        if (shouldExit) {
            logger.d("Exit confirmed, waiting ${EXIT_DIALOG_DELAY_MS}ms for dialog dismissal + recomposition...")
            delay(EXIT_DIALOG_DELAY_MS.milliseconds)
            logger.d("Calling Activity.finish() after delay")
            onConfirmExit()
        }
    }

    if (!isVisible) {
        return
    }

    val mascotTransition = rememberInfiniteTransition(label = "exitMascotTransition")
    val mascotScale = mascotTransition.animateFloat(
        initialValue = MASCOT_SCALE_MIN,
        targetValue = MASCOT_SCALE_MAX,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = MASCOT_SCALE_ANIMATION_MS,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "exitMascotScale",
    )
    val mascotFloatY = mascotTransition.animateFloat(
        initialValue = MASCOT_FLOAT_MIN,
        targetValue = MASCOT_FLOAT_MAX,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = MASCOT_FLOAT_ANIMATION_MS,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "exitMascotFloatY",
    )

    AlertDialog(
        onDismissRequest = {
            logger.d("Dialog dismissed via onDismissRequest")
            onDismiss()
        },
        icon = {
            Image(
                painter = painterResource(id = R.drawable.ic_ant_mascot),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .graphicsLayer {
                        scaleX = mascotScale.value
                        scaleY = mascotScale.value
                        translationY = mascotFloatY.value
                    }
            )
        },
        title = { AppText(text = stringResource(R.string.exit_app_title)) },
        text = { AppText(text = stringResource(R.string.exit_app_message)) },
        confirmButton = {
            TextButton(
                onClick = {
                    logger.d("Confirm button clicked, setting shouldExit = true")
                    setShouldExit(true)
                    // Inform the parent to start the dismissal process immediately
                    onDismiss()
                }
            ) {
                AppText(text = stringResource(R.string.exit_app_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    logger.d("Cancel button clicked")
                    onDismiss()
                }
            ) {
                AppText(text = stringResource(R.string.common_cancel))
            }
        },
    )
}

// Wrapper Composable function with Kotlin naming convention
@Composable
fun AppExitConfirmationDialog(
    onConfirmExit: () -> Unit,
    onDismiss: () -> Unit,
    isVisible: Boolean = true,
) {
    appExitConfirmationDialog(onConfirmExit, onDismiss, isVisible)
}

@Preview(name = "AppExitConfirmationDialog - Light", showBackground = true)
@Composable
private fun previewAppExitConfirmationDialogLight() {
    AntCashManagerTheme(dynamicColor = false) {
        AppExitConfirmationDialog(
            onConfirmExit = {},
            onDismiss = {},
            isVisible = true,
        )
    }
}

@Preview(
    name = "AppExitConfirmationDialog - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun previewAppExitConfirmationDialogDark() {
    previewAppExitConfirmationDialogLight()
}


