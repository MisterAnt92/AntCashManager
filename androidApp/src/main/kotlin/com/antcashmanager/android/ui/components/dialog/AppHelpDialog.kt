package com.antcashmanager.android.ui.components.dialog

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.HelpDialogContent
import com.antcashmanager.android.ui.components.SimpleHelpFeature
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

/**
 * Feature descriptor for a localized help dialog item.
 */
data class HelpDialogFeatureSpec(
    @StringRes val titleResId: Int,
    @StringRes val descriptionResId: Int,
    val icon: ImageVector,
    val iconTint: Color = Color.Unspecified,
)

/**
 * Shared help dialog that resolves all text from string resources.
 */
@Composable
fun AppHelpDialog(
    @StringRes titleResId: Int,
    @StringRes descriptionResId: Int,
    features: List<HelpDialogFeatureSpec>,
    onDismiss: () -> Unit,
    isVisible: Boolean = true,
) {
    val localizedFeatures = features.map { feature ->
        SimpleHelpFeature(
            title = stringResource(feature.titleResId),
            description = stringResource(feature.descriptionResId),
            icon = feature.icon,
            iconTint = feature.iconTint,
        )
    }

    HelpDialogContent(
        isVisible = isVisible,
        title = stringResource(titleResId),
        description = stringResource(descriptionResId),
        features = localizedFeatures,
        onDismiss = onDismiss,
    )
}

@Preview(name = "AppHelpDialog - Light", showBackground = true)
@Composable
private fun AppHelpDialogPreviewLight() {
    AntCashManagerTheme(dynamicColor = false) {
        AppHelpDialog(
            titleResId = R.string.help_home_title,
            descriptionResId = R.string.help_home_desc,
            features = listOf(
                HelpDialogFeatureSpec(
                    titleResId = R.string.help_home_feature_date_filters_title,
                    descriptionResId = R.string.help_home_feature_date_filters_desc,
                    icon = Icons.Default.Info,
                ),
            ),
            onDismiss = {},
            isVisible = true,
        )
    }
}

@Preview(
    name = "AppHelpDialog - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun AppHelpDialogPreviewDark() {
    AppHelpDialogPreviewLight()
}

