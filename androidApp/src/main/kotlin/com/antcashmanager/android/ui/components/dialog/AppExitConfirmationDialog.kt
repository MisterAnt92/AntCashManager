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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

@Composable
fun AppExitConfirmationDialog(
    onConfirmExit: () -> Unit,
    onDismiss: () -> Unit,
    isVisible: Boolean = true,
) {
    if (!isVisible) {
        return
    }

    val mascotTransition = rememberInfiniteTransition(label = "exitMascotTransition")
    val mascotScale = mascotTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "exitMascotScale",
    )
    val mascotFloatY = mascotTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1400,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "exitMascotFloatY",
    )

    AlertDialog(
        onDismissRequest = onDismiss,
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
            TextButton(onClick = onConfirmExit) {
                AppText(text = stringResource(R.string.exit_app_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                AppText(text = stringResource(R.string.common_cancel))
            }
        },
    )
}

@Preview(name = "AppExitConfirmationDialog - Light", showBackground = true)
@Composable
private fun AppExitConfirmationDialogPreviewLight() {
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
private fun AppExitConfirmationDialogPreviewDark() {
    AppExitConfirmationDialogPreviewLight()
}
