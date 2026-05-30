package com.antcashmanager.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

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

@Preview(name = "AdaptiveLayoutInfo - Compact", showBackground = true, widthDp = 360)
@Preview(
    name = "AdaptiveLayoutInfo - Accessibility",
    showBackground = true,
    widthDp = 360,
    fontScale = 1.5f,
)
@Composable
private fun AdaptiveLayoutInfoPreview() {
    val info = rememberAdaptiveLayoutInfo()
    AntCashManagerTheme(dynamicColor = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = info.horizontalPadding)
                    .height(24.dp)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}
