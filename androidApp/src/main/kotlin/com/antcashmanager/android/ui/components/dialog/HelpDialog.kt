package com.antcashmanager.android.ui.components.dialog

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.layout.HorizontalSpacer
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

data class SimpleHelpFeature(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconTint: Color = Color.Unspecified,
)

@Composable
fun HelpButton(
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onHelpClick,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
            ),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Default.Help,
            contentDescription = stringResource(R.string.common_help),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun HelpDialogContent(
    isVisible: Boolean,
    title: String,
    description: String,
    features: List<SimpleHelpFeature>,
    onDismiss: () -> Unit,
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    AppText(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.common_close),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AppText(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    VerticalSpacer(SpacingSize.XS)

                    AppText(
                        text = stringResource(R.string.help_features_label),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    features.forEach { feature ->
                        HelpFeatureItemRow(feature = feature)
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = onDismiss) {
                    AppText(stringResource(R.string.common_close))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
    }
}

@Composable
private fun HelpFeatureItemRow(
    feature: SimpleHelpFeature,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = feature.icon,
            contentDescription = null,
            tint = if (feature.iconTint == Color.Unspecified) MaterialTheme.colorScheme.primary else feature.iconTint,
            modifier = Modifier.size(24.dp),
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            AppText(
                text = feature.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            AppText(
                text = feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(name = "HelpDialog - Light", showBackground = true)
@Composable
private fun HelpDialogPreviewLight() {
    AntCashManagerTheme(dynamicColor = false) {
        Box(modifier = Modifier.fillMaxWidth()) {
            HelpDialogContent(
                isVisible = true,
                title = "Help Title",
                description = "This is a help description",
                features = listOf(
                    SimpleHelpFeature(
                        title = "Feature 1",
                        description = "Description of feature 1",
                        icon = Icons.AutoMirrored.Default.Help,
                    ),
                ),
                onDismiss = {},
            )
        }
    }
}

@Preview(
    name = "HelpDialog - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HelpDialogPreviewDark() {
    HelpDialogPreviewLight()
}

@Preview(name = "HelpDialog - Accessibility", showBackground = true, fontScale = 1.5f)
@Composable
private fun HelpDialogPreviewAccessibility() {
    HelpDialogPreviewLight()
}
