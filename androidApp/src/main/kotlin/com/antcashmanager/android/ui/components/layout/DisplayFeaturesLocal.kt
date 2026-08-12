package com.antcashmanager.android.ui.components.layout

import androidx.compose.runtime.compositionLocalOf
import androidx.window.layout.DisplayFeature
import androidx.window.layout.FoldingFeature

/**
 * CompositionLocal providing access to display features (folds, hinges) on this device.
 *
 * Use [LocalDisplayFeatures].current to access the list of display features in any Composable.
 * Typically provided by MainActivity via CompositionLocalProvider.
 */
val LocalDisplayFeatures = compositionLocalOf<List<DisplayFeature>> { emptyList() }

/**
 * Get the first folding feature (hinge) if available.
 * Useful for devices like Samsung Galaxy Z Fold and Z Flip.
 *
 * @return FoldingFeature if device has a fold, null otherwise
 */
fun List<DisplayFeature>.getFoldingFeature(): FoldingFeature? {
    return filterIsInstance<FoldingFeature>().firstOrNull()
}

/**
 * Check if device has a folding feature (is foldable).
 *
 * @return True if device has fold/hinge
 */
fun List<DisplayFeature>.hasFoldingFeature(): Boolean {
    return any { it is FoldingFeature }
}
