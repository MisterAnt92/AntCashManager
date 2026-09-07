package com.antcashmanager.android.ui.screen.settings.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.settings.displaySettings.DisplayConstant
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

/**
 * Dialog per la scelta del colore di sfondo dei widget della home screen.
 * La selezione di uno swatch applica subito il colore e chiude il dialog.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WidgetBackgroundColorDialog(
    currentColor: Long,
    onColorSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { AppText(stringResource(R.string.dialog_choose_widget_background_color)) },
        text = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DisplayConstant.WIDGET_BACKGROUND_COLOR_PRESETS.forEach { color ->
                    val isSelected = color == currentColor
                    Box(
                        modifier =
                            Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .then(
                                    if (isSelected) {
                                        Modifier.border(
                                            3.dp,
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape,
                                        )
                                    } else {
                                        Modifier
                                    },
                                ).clickable { onColorSelected(color) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.categories_selected),
                                tint = if (isLightColor(color)) Color.Black else Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { AppText(stringResource(R.string.common_cancel)) }
        },
    )
}

private fun isLightColor(color: Long): Boolean {
    val r = (color shr 16 and 0xFF) / 255f
    val g = (color shr 8 and 0xFF) / 255f
    val b = (color and 0xFF) / 255f
    val luminance = 0.299f * r + 0.587f * g + 0.114f * b
    return luminance > 0.6f
}

@Preview(showBackground = true)
@Composable
private fun WidgetBackgroundColorDialogPreview() {
    AntCashManagerTheme {
        WidgetBackgroundColorDialog(
            currentColor = 0xFFFFFFFFL,
            onColorSelected = {},
            onDismiss = {},
        )
    }
}
