package com.antcashmanager.android.ui.screen.transactions.view

import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.dialog.AppHelpDialog
import com.antcashmanager.android.ui.components.dialog.HelpDialogFeatureSpec

/**
 * Dialog di aiuto specifico per la schermata delle transazioni.
 */
@Composable
internal fun HelpDialog(onDismiss: () -> Unit) {
    val features = listOf(
        HelpDialogFeatureSpec(
            titleResId = R.string.transactions_help_filter_title,
            descriptionResId = R.string.transactions_help_filter_desc,
            icon = Icons.Default.FilterList
        ),
        HelpDialogFeatureSpec(
            titleResId = R.string.transactions_help_search_title,
            descriptionResId = R.string.transactions_help_search_desc,
            icon = Icons.Default.Search
        ),
        HelpDialogFeatureSpec(
            titleResId = R.string.transactions_help_display_title,
            descriptionResId = R.string.transactions_help_display_desc,
            icon = Icons.AutoMirrored.Filled.ViewList
        )
    )

    AppHelpDialog(
        titleResId = R.string.transactions_help_title,
        descriptionResId = R.string.transactions_help_desc,
        onDismiss = onDismiss,
        features = features
    )
}
