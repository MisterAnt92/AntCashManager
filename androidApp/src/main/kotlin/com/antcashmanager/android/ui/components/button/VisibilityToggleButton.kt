package com.antcashmanager.android.ui.components.button

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.antcashmanager.android.R

/**
 * Reusable component for show/hide toggle button.
 *
 * Features:
 * - Switches between Visibility and VisibilityOff icons
 * - Dynamic content description based on state
 * - Consistent icon usage across the app
 *
 * @param isVisible Current visibility state
 * @param onToggle Callback when button is clicked with new visibility state
 * @param modifier Optional modifier for the IconButton
 */
@Composable
fun VisibilityToggleButton(
    isVisible: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = { onToggle(!isVisible) },
        modifier = modifier,
    ) {
        Icon(
            imageVector =
                if (isVisible) {
                    Icons.Default.Visibility
                } else {
                    Icons.Default.VisibilityOff
                },
            contentDescription =
                stringResource(
                    if (isVisible) {
                        R.string.home_move_up // TODO: Add specific string for visibility
                    } else {
                        R.string.home_move_down // TODO: Add specific string for visibility
                    },
                ),
        )
    }
}
