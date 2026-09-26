package com.antcashmanager.android.ui.screen.transactions.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.dialog.AppHelpDialog
import com.antcashmanager.android.ui.components.dialog.HelpDialogFeatureSpec

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
/**
 * Dialog di aiuto specifico per la schermata delle transazioni.
 */
@Composable
internal fun HelpDialog(onDismiss: () -> Unit) {
    val features =
        listOf(
            HelpDialogFeatureSpec(
                titleResId = R.string.transactions_help_filter_title,
                descriptionResId = R.string.transactions_help_filter_desc,
                icon = Icons.Default.FilterList,
            ),
            HelpDialogFeatureSpec(
                titleResId = R.string.transactions_help_search_title,
                descriptionResId = R.string.transactions_help_search_desc,
                icon = Icons.Default.Search,
            ),
            HelpDialogFeatureSpec(
                titleResId = R.string.transactions_help_display_title,
                descriptionResId = R.string.transactions_help_display_desc,
                icon = Icons.AutoMirrored.Filled.ViewList,
            ),
        )

    AppHelpDialog(
        titleResId = R.string.transactions_help_title,
        descriptionResId = R.string.transactions_help_desc,
        onDismiss = onDismiss,
        features = features,
    )
}


@Preview(showBackground = true, name = "HelpDialog - Light")
@Composable
private fun HelpDialogLightPreview() {
    AntCashManagerTheme(dynamicColor = false) { HelpDialog(onDismiss = {}) }
}

@Preview(showBackground = true, name = "HelpDialog - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HelpDialogDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) { HelpDialog(onDismiss = {}) }
}

@Preview(showBackground = true, name = "HelpDialog - 2x", fontScale = 2.0f)
@Composable
private fun HelpDialogLargeTextPreview() {
    AntCashManagerTheme(dynamicColor = false) { HelpDialog(onDismiss = {}) }
}