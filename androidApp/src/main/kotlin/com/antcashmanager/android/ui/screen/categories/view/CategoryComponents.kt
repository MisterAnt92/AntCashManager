package com.antcashmanager.android.ui.screen.categories.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.dialog.AppHelpDialog
import com.antcashmanager.android.ui.components.dialog.HelpDialogFeatureSpec

@Composable
internal fun HelpDialog(onDismiss: () -> Unit) {
    val helpFeatures =
        listOf(
            HelpDialogFeatureSpec(
                titleResId = R.string.help_categories_feature_management_title,
                descriptionResId = R.string.help_categories_feature_management_desc,
                icon = Icons.Default.Add,
            ),
            HelpDialogFeatureSpec(
                titleResId = R.string.help_categories_feature_income_expense_title,
                descriptionResId = R.string.help_categories_feature_income_expense_desc,
                icon = Icons.AutoMirrored.Filled.List,
            ),
            HelpDialogFeatureSpec(
                titleResId = R.string.help_categories_feature_delete_title,
                descriptionResId = R.string.help_categories_feature_delete_desc,
                icon = Icons.Default.Delete,
            ),
        )

    AppHelpDialog(
        titleResId = R.string.help_categories_title,
        descriptionResId = R.string.help_categories_desc,
        features = helpFeatures,
        onDismiss = onDismiss,
    )
}
