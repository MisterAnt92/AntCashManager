package com.antcashmanager.android.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val TABLET_MEDIUM_BREAKPOINT_DP = 600
private const val TABLET_EXPANDED_BREAKPOINT_DP = 840

/**
 * Describes the current window class used to adapt layouts for phones and tablets.
 */
@Stable
data class AdaptiveLayoutInfo(
    val screenWidthDp: Int,
    val isCompact: Boolean,
    val isMedium: Boolean,
    val isExpanded: Boolean,
    val horizontalPadding: Dp,
    val maxContentWidth: Dp,
)

/**
 * Returns adaptive information based on width breakpoints.
 * - Compact: phones (< 600dp)
 * - Medium: 7" tablets (600..839dp)
 * - Expanded: 10" tablets and larger (>= 840dp)
 */
@Composable
fun rememberAdaptiveLayoutInfo(): AdaptiveLayoutInfo {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp

    return when {
        screenWidthDp >= TABLET_EXPANDED_BREAKPOINT_DP -> AdaptiveLayoutInfo(
            screenWidthDp = screenWidthDp,
            isCompact = false,
            isMedium = false,
            isExpanded = true,
            horizontalPadding = 24.dp,
            maxContentWidth = 1200.dp,
        )

        screenWidthDp >= TABLET_MEDIUM_BREAKPOINT_DP -> AdaptiveLayoutInfo(
            screenWidthDp = screenWidthDp,
            isCompact = false,
            isMedium = true,
            isExpanded = false,
            horizontalPadding = 20.dp,
            maxContentWidth = 960.dp,
        )

        else -> AdaptiveLayoutInfo(
            screenWidthDp = screenWidthDp,
            isCompact = true,
            isMedium = false,
            isExpanded = false,
            horizontalPadding = 8.dp,
            maxContentWidth = 680.dp,
        )
    }
}


