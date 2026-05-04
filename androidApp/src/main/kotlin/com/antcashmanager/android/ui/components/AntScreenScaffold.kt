package com.antcashmanager.android.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Centralized scaffold wrapper for consistent padding, top bar, and content.
 *
 * @param modifier Modifier to apply to the Scaffold.
 * @param showTopBar If true, displays the top bar with the given title and actions.
 * @param topBarTitle Title for the top bar (ignored if showTopBar is false).
 * @param topBarActions Optional composable for top bar actions.
 * @param bottomBar Optional composable for the bottom bar.
 * @param floatingActionButton Optional composable for the FAB.
 * @param content Main content, receives the inner PaddingValues.
 */
@Composable
fun AntScreenScaffold(
    modifier: Modifier = Modifier,
    showTopBar: Boolean = false,
    topBarTitle: String = "",
    topBarActions: (@Composable () -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            if (showTopBar) {
                ScreenHeader(title = topBarTitle, actions = topBarActions)
            }
        },
        bottomBar = { bottomBar?.invoke() },
        floatingActionButton = { floatingActionButton?.invoke() },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 6.dp,//innerPadding.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                    end = 6.dp,//innerPadding.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    top = 2.dp,//innerPadding.calculateTopPadding(),
                    bottom = 2.dp//innerPadding.calculateBottomPadding(),
                )
        ) {
            content(innerPadding)
        }
    }
}
