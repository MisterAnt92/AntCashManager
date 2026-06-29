package com.antcashmanager.android.ui.screen.home.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.runtime.Composable
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.dialog.AppHelpDialog
import com.antcashmanager.android.ui.components.dialog.HelpDialogFeatureSpec

// ══════════════════════════════════════════════════════════════════════════════
// HELP DIALOG
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    val helpFeatures = listOf(
        HelpDialogFeatureSpec(
            titleResId = R.string.common_dashboard,
            descriptionResId = R.string.help_dashboard_desc,
            icon = Icons.AutoMirrored.Filled.TrendingUp,
        ),
        HelpDialogFeatureSpec(
            titleResId = R.string.help_home_feature_date_filters_title,
            descriptionResId = R.string.help_home_feature_date_filters_desc,
            icon = Icons.Default.ArrowUpward,
        ),
        HelpDialogFeatureSpec(
            titleResId = R.string.help_home_feature_recent_transactions_title,
            descriptionResId = R.string.help_home_feature_recent_transactions_desc,
            icon = Icons.Default.Repeat,
        ),
    )

    AppHelpDialog(
        titleResId = R.string.help_home_title,
        descriptionResId = R.string.help_home_desc,
        features = helpFeatures,
        onDismiss = onDismiss,
    )
}