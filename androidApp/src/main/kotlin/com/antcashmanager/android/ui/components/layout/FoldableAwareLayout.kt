package com.antcashmanager.android.ui.components.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.layout.FoldingFeature

/**
 * Composable that renders content aware of foldable device configuration.
 *
 * For foldable devices like Samsung Galaxy Z Fold (vertical fold) or Z Flip (horizontal fold),
 * this layout ensures no important content is hidden by the fold/hinge region.
 *
 * When a fold is detected, the screen is split into panes that avoid the hinge area.
 *
 * @param foldingFeature Information about fold orientation and bounds (from androidx.window)
 * @param modifier Modifier for this layout
 * @param topContent Composable to render in top/left pane (first pane)
 * @param bottomContent Composable to render in bottom/right pane (second pane) - optional
 */
@Composable
fun FoldableAwareLayout(
    foldingFeature: FoldingFeature?,
    modifier: Modifier = Modifier,
    topContent: @Composable (paneWidth: Dp, paneHeight: Dp) -> Unit,
    bottomContent: (@Composable (paneWidth: Dp, paneHeight: Dp) -> Unit)? = null,
) {
    if (foldingFeature == null || !foldingFeature.isSeparating) {
        // No fold detected - render normally
        Box(modifier = modifier.fillMaxWidth()) {
            topContent(Dp.Unspecified, Dp.Unspecified)
        }
        return
    }

    // Fold detected - split layout based on orientation
    when (foldingFeature.orientation) {
        FoldingFeature.Orientation.VERTICAL -> {
            // Vertical fold (Z Fold style) - split left/right
            Row(modifier = modifier.fillMaxWidth()) {
                // Left pane
                Box(modifier = Modifier.weight(1f)) {
                    topContent(Dp.Unspecified, Dp.Unspecified)
                }

                // Hinge spacer
                Spacer(modifier = Modifier.width(HINGE_WIDTH))

                // Right pane
                Box(modifier = Modifier.weight(1f)) {
                    bottomContent?.invoke(Dp.Unspecified, Dp.Unspecified)
                        ?: topContent(Dp.Unspecified, Dp.Unspecified)
                }
            }
        }

        FoldingFeature.Orientation.HORIZONTAL -> {
            // Horizontal fold (Z Flip style) - split top/bottom
            Column(modifier = modifier.fillMaxWidth()) {
                // Top pane
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    topContent(Dp.Unspecified, Dp.Unspecified)
                }

                // Hinge spacer
                Spacer(modifier = Modifier.height(HINGE_WIDTH))

                // Bottom pane
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    bottomContent?.invoke(Dp.Unspecified, Dp.Unspecified)
                        ?: topContent(Dp.Unspecified, Dp.Unspecified)
                }
            }
        }
    }
}

/**
 * Simpler version: Returns whether content should be rendered in single or split pane.
 * Useful when you want more fine-grained control over split-pane behavior.
 *
 * @param foldingFeature Fold information from androidx.window
 * @return True if layout should use split panes, False for single pane
 */
fun shouldUseSplitPane(foldingFeature: FoldingFeature?): Boolean {
    return foldingFeature != null && foldingFeature.isSeparating
}

/**
 * Determine if fold is vertical (Z Fold) or horizontal (Z Flip).
 *
 * @param foldingFeature Fold information
 * @return True if vertical fold, False if horizontal or no fold
 */
fun isFoldVertical(foldingFeature: FoldingFeature?): Boolean {
    return foldingFeature?.orientation == FoldingFeature.Orientation.VERTICAL
}

/**
 * Determine if fold is horizontal (Z Flip).
 *
 * @param foldingFeature Fold information
 * @return True if horizontal fold, False if vertical or no fold
 */
fun isFoldHorizontal(foldingFeature: FoldingFeature?): Boolean {
    return foldingFeature?.orientation == FoldingFeature.Orientation.HORIZONTAL
}

// Constants
private val HINGE_WIDTH = 20.dp  // Approximate hinge/fold width for spacing
