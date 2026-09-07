package com.antcashmanager.android.ui.components.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Optimized window insets handling for foldable devices, notches, and tablets.
 *
 * This utility provides proper spacing for status bars, navigation bars,
 * notches, and foldable device hinges.
 *
 * Usage examples:
 * ```kotlin
 * // Use systemBars for most content
 * Scaffold(contentWindowInsets = WindowInsets.systemBars)
 *
 * // Or get padding dynamically in a Composable
 * @Composable
 * fun MyComponent() {
 *     val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
 * }
 * ```
 */

/**
 * Optimized Scaffold wrapper with proper window insets handling.
 *
 * Automatically applies system bar insets to content and layout properly
 * for status bars, navigation bars, and display cutouts.
 *
 * @param modifier Modifier for the scaffold
 * @param topBar Top app bar composable
 * @param bottomBar Bottom navigation/bar composable
 * @param floatingActionButton FAB composable
 * @param content Main content area
 */
@Composable
fun OptimizedScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        contentWindowInsets = WindowInsets.systemBars,
        content = content,
    )
}
