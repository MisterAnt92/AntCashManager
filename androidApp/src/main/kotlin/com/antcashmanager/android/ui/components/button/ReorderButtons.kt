package com.antcashmanager.android.ui.components.button

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.antcashmanager.android.R

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
                tint = if (canMoveUp)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            )
        }
        IconButton(
            onClick = onMoveDown,
            enabled = canMoveDown,
        ) {
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = downDescription,
                tint = if (canMoveDown)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            )
        }
    }
}
