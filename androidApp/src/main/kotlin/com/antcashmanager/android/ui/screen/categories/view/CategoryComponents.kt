package com.antcashmanager.android.ui.screen.categories.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.dialog.AppHelpDialog
import com.antcashmanager.android.ui.components.dialog.HelpDialogFeatureSpec
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

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

@Preview(showBackground = true, name = "CategoriesHelpDialog - Light")
@Composable
private fun CategoriesHelpDialogLightPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        HelpDialog(onDismiss = {})
    }
}

@Preview(showBackground = true, name = "CategoriesHelpDialog - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategoriesHelpDialogDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        HelpDialog(onDismiss = {})
    }
}

@Preview(showBackground = true, name = "CategoriesHelpDialog - 2x", fontScale = 2.0f)
@Composable
private fun CategoriesHelpDialogLargeTextPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        HelpDialog(onDismiss = {})
    }
}
