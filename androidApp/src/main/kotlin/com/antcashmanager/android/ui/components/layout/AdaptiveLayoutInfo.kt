package com.antcashmanager.android.ui.components.layout

import android.graphics.Rect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.layout.DisplayFeature
import androidx.window.layout.FoldingFeature
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

private const val TABLET_MEDIUM_BREAKPOINT_DP = 600
private const val TABLET_EXPANDED_BREAKPOINT_DP = 840

/**
 * Describes the current window class used to adapt layouts for phones, tablets, and foldable devices.
 * Includes fold detection information for devices like Samsung Galaxy Z Fold and Z Flip.
 */
@Stable
data class AdaptiveLayoutInfo(
    val screenWidthDp: Int,
    val screenHeightDp: Int,
    val smallestScreenWidthDp: Int,
    val isCompact: Boolean,
    val isMedium: Boolean,
    val isExpanded: Boolean,
    val isLandscape: Boolean,
    val isTabletDevice: Boolean,
    val isFoldableDevice: Boolean,
    val preferRailNavigation: Boolean,
    val horizontalPadding: Dp,
    val maxContentWidth: Dp,
    // Fold detection (androidx.window)
    val foldingFeature: FoldingFeature? = null,
    val hasFold: Boolean = false,
    val isFoldHorizontal: Boolean = false, // Horizontal fold (Z Flip)
    val isFoldVertical: Boolean = false, // Vertical fold (Z Fold)
    val foldBounds: Rect? = null, // Pixel coordinates of fold
)

/**
 * Returns adaptive information based on width breakpoints.
 * - Compact: phones (< 600dp)
 * - Medium: 7" tablets (600..839dp)
 * - Expanded: 10" tablets and larger (>= 840dp)
 *
 * FASE 1: Now includes fold detection from displayFeatures (androidx.window)
 */
@Composable
fun rememberAdaptiveLayoutInfo(displayFeatures: List<DisplayFeature> = emptyList()): AdaptiveLayoutInfo {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp
    val smallestScreenWidthDp = configuration.smallestScreenWidthDp
    val isLandscape = screenWidthDp > screenHeightDp
    val isTabletDevice = smallestScreenWidthDp >= TABLET_MEDIUM_BREAKPOINT_DP
    val isFoldableDevice = !isTabletDevice && screenWidthDp >= TABLET_MEDIUM_BREAKPOINT_DP

    // FASE 1: Extract fold detection from displayFeatures
    val foldingFeature = displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()
    val hasFold = foldingFeature != null && foldingFeature.isSeparating
    val isFoldHorizontal = foldingFeature?.orientation == FoldingFeature.Orientation.HORIZONTAL
    val isFoldVertical = foldingFeature?.orientation == FoldingFeature.Orientation.VERTICAL
    val foldBounds = foldingFeature?.bounds

    return when {
        screenWidthDp >= TABLET_EXPANDED_BREAKPOINT_DP ->
            AdaptiveLayoutInfo(
                screenWidthDp = screenWidthDp,
                screenHeightDp = screenHeightDp,
                smallestScreenWidthDp = smallestScreenWidthDp,
                isCompact = false,
                isMedium = false,
                isExpanded = true,
                isLandscape = isLandscape,
                isTabletDevice = isTabletDevice,
                isFoldableDevice = isFoldableDevice,
                preferRailNavigation = true,
                horizontalPadding = 24.dp,
                maxContentWidth = 1200.dp,
                // FASE 1: Use fold detection fields
                foldingFeature = foldingFeature,
                hasFold = hasFold,
                isFoldHorizontal = isFoldHorizontal,
                isFoldVertical = isFoldVertical,
                foldBounds = foldBounds,
            )

        screenWidthDp >= TABLET_MEDIUM_BREAKPOINT_DP ->
            AdaptiveLayoutInfo(
                screenWidthDp = screenWidthDp,
                screenHeightDp = screenHeightDp,
                smallestScreenWidthDp = smallestScreenWidthDp,
                isCompact = false,
                isMedium = true,
                isExpanded = false,
                isLandscape = isLandscape,
                isTabletDevice = isTabletDevice,
                isFoldableDevice = isFoldableDevice,
                preferRailNavigation = isTabletDevice || isLandscape,
                horizontalPadding = if (isFoldableDevice && !isLandscape) 16.dp else 20.dp,
                maxContentWidth = if (isFoldableDevice) 880.dp else 960.dp,
                // FASE 1: Use fold detection fields
                foldingFeature = foldingFeature,
                hasFold = hasFold,
                isFoldHorizontal = isFoldHorizontal,
                isFoldVertical = isFoldVertical,
                foldBounds = foldBounds,
            )

        else ->
            AdaptiveLayoutInfo(
                screenWidthDp = screenWidthDp,
                screenHeightDp = screenHeightDp,
                smallestScreenWidthDp = smallestScreenWidthDp,
                isCompact = true,
                isMedium = false,
                isExpanded = false,
                isLandscape = isLandscape,
                isTabletDevice = isTabletDevice,
                isFoldableDevice = false,
                preferRailNavigation = false,
                horizontalPadding = 8.dp,
                maxContentWidth = 680.dp,
                // FASE 1: Use fold detection fields (no fold on compact phones typically)
                foldingFeature = foldingFeature,
                hasFold = hasFold,
                isFoldHorizontal = isFoldHorizontal,
                isFoldVertical = isFoldVertical,
                foldBounds = foldBounds,
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
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = info.horizontalPadding)
                        .height(24.dp)
                        .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}
