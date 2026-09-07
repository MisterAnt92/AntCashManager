package com.antcashmanager.android.ui.components.layout

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.common.ScreenHeader
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

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
    val adaptiveLayoutInfo = rememberAdaptiveLayoutInfo()

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
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = adaptiveLayoutInfo.horizontalPadding,
                        end = adaptiveLayoutInfo.horizontalPadding,
                    ),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .widthIn(max = adaptiveLayoutInfo.maxContentWidth),
            ) {
                content(innerPadding)
            }
        }
    }
}

@Preview(name = "AntScreenScaffold - Light", showBackground = true)
@Composable
private fun AntScreenScaffoldPreviewLight() {
    AntCashManagerTheme(dynamicColor = false) {
        AntScreenScaffold(
            showTopBar = true,
            topBarTitle = "Screen Title",
            bottomBar = { AppText("Bottom Bar", modifier = Modifier.padding(8.dp)) },
            floatingActionButton = { AppText("FAB", color = MaterialTheme.colorScheme.primary) },
        ) {
            AppText("Screen content", modifier = Modifier.padding(16.dp))
        }
    }
}

@Preview(
    name = "AntScreenScaffold - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun AntScreenScaffoldPreviewDark() {
    AntScreenScaffoldPreviewLight()
}

@Preview(name = "AntScreenScaffold - Accessibility", showBackground = true, fontScale = 1.5f)
@Composable
private fun AntScreenScaffoldPreviewAccessibility() {
    AntScreenScaffoldPreviewLight()
}
