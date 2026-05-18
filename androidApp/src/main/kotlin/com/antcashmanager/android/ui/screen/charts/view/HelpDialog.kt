package com.antcashmanager.android.ui.screen.charts.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.runtime.Composable
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.dialog.AppHelpDialog
import com.antcashmanager.android.ui.components.dialog.HelpDialogFeatureSpec

@Composable
internal fun HelpDialog(onDismiss: () -> Unit) {
    val helpFeatures = listOf(
        HelpDialogFeatureSpec(
            titleResId = R.string.help_charts_feature_visualization_title,
            descriptionResId = R.string.help_charts_feature_visualization_desc,
            icon = Icons.Default.BarChart,
        ),
        HelpDialogFeatureSpec(
            titleResId = R.string.help_charts_feature_filters_title,
            descriptionResId = R.string.help_charts_feature_filters_desc,
            icon = Icons.Default.CalendarMonth,
        ),
        HelpDialogFeatureSpec(
            titleResId = R.string.help_charts_feature_analysis_title,
            descriptionResId = R.string.help_charts_feature_analysis_desc,
            icon = Icons.AutoMirrored.Default.TrendingUp,
        ),
    )

    AppHelpDialog(
        titleResId = R.string.help_charts_title,
        descriptionResId = R.string.help_charts_desc,
        features = helpFeatures,
        onDismiss = onDismiss,
    )
}

