package com.antcashmanager.android.ui.components.button

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

/**
 * Reusable component for up/down reordering buttons with dynamic tint feedback.
 *
 * Features:
 * - Primary color tint when enabled
 * - Outline.copy(alpha=0.5f) tint when disabled
 * - Semantic contentDescription for accessibility
 * - Consistent icon usage (ArrowUpward/ArrowDownward)
 *
 * @param onMoveUp Callback when move up button is clicked
 * @param onMoveDown Callback when move down button is clicked
 * @param canMoveUp Whether the up button should be enabled
 * @param canMoveDown Whether the down button should be enabled
 * @param upDescription Accessibility description for up button
 * @param downDescription Accessibility description for down button
 * @param modifier Optional modifier for the Row container
 */
@Composable
fun ReorderButtons(
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    canMoveUp: Boolean = true,
    canMoveDown: Boolean = true,
    upDescription: String = stringResource(R.string.home_move_up),
    downDescription: String = stringResource(R.string.home_move_down),
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        IconButton(
            onClick = onMoveUp,
            enabled = canMoveUp,
        ) {
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = upDescription,
                tint =
                    if (canMoveUp) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    },
            )
        }
        IconButton(
            onClick = onMoveDown,
            enabled = canMoveDown,
        ) {
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = downDescription,
                tint =
                    if (canMoveDown) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    },
            )
        }
    }
}

@Preview(showBackground = true, name = "ReorderButtons - Light")
@Composable
private fun ReorderButtonsLightPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        ReorderButtons(
            onMoveUp = {},
            onMoveDown = {},
            canMoveUp = true,
            canMoveDown = true,
        )
    }
}

@Preview(showBackground = true, name = "ReorderButtons - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReorderButtonsDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        ReorderButtons(
            onMoveUp = {},
            onMoveDown = {},
            canMoveUp = true,
            canMoveDown = true,
        )
    }
}

@Preview(showBackground = true, name = "ReorderButtons - 2x", fontScale = 2.0f)
@Composable
private fun ReorderButtonsLargeTextPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        ReorderButtons(
            onMoveUp = {},
            onMoveDown = {},
            canMoveUp = true,
            canMoveDown = true,
        )
    }
}

@Preview(showBackground = true, name = "ReorderButtons Disabled - Light")
@Composable
private fun ReorderButtonsDisabledLightPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        ReorderButtons(
            onMoveUp = {},
            onMoveDown = {},
            canMoveUp = false,
            canMoveDown = false,
        )
    }
}

@Preview(showBackground = true, name = "ReorderButtons Disabled - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReorderButtonsDisabledDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        ReorderButtons(
            onMoveUp = {},
            onMoveDown = {},
            canMoveUp = false,
            canMoveDown = false,
        )
    }
}

@Preview(showBackground = true, name = "ReorderButtons Disabled - 2x", fontScale = 2.0f)
@Composable
private fun ReorderButtonsDisabledLargeTextPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        ReorderButtons(
            onMoveUp = {},
            onMoveDown = {},
            canMoveUp = false,
            canMoveDown = false,
        )
    }
}
