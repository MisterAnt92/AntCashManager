package com.antcashmanager.android.ui.components.dialog

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.antcashmanager.android.ui.components.HelpDialogContent
import com.antcashmanager.android.ui.components.SimpleHelpFeature

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

