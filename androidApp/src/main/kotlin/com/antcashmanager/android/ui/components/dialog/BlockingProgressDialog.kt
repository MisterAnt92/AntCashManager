package com.antcashmanager.android.ui.components.dialog

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.RestorePage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import kotlin.math.PI
import kotlin.math.sin

/**
 * Dialog di caricamento non dismissabile: blocca l'interazione (niente tap fuori, niente back)
 * finché l'operazione sottostante non si conclude. Pensata per operazioni distruttive/lunghe
 * come backup e ripristino dei dati, dove è importante che l'utente non possa uscire a metà.
 */
@Composable
fun BlockingProgressDialog(
    message: String,
    icon: ImageVector = Icons.Default.Backup,
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .widthIn(min = 220.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "blocking_progress_bounce")
                val bounce by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 2f * PI.toFloat(),
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1400, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart,
                    ),
                    label = "blocking_progress_bounce_angle",
                )

                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(72.dp),
                        strokeWidth = 4.dp,
                    )
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .graphicsLayer {
                                // Piccolo "respiro" verticale continuo: rende l'attesa più simpatica
                                // senza distrarre dallo spinner, che resta l'indicatore di stato reale.
                                translationY = sin(bounce) * 6f
                            },
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                AppText(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 24.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "BlockingProgressDialog - Backup")
@Composable
private fun BlockingProgressDialogBackupPreview() {
    AntCashManagerTheme {
        BlockingProgressDialog(
            message = "Creazione backup in corso…",
            icon = Icons.Default.Backup,
        )
    }
}

@Preview(showBackground = true, name = "BlockingProgressDialog - Restore")
@Composable
private fun BlockingProgressDialogRestorePreview() {
    AntCashManagerTheme {
        BlockingProgressDialog(
            message = "Ripristino dei dati in corso…",
            icon = Icons.Default.RestorePage,
        )
    }
}

@Preview(
    name = "BlockingProgressDialog - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun BlockingProgressDialogDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        BlockingProgressDialog(message = "Creazione backup in corso…")
    }
}
