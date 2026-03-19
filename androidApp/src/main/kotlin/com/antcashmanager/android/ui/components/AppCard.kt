package com.antcashmanager.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

/**
 * A reusable, customizable Material Design 3 card component.
 *
 * @param title The primary text displayed in the card.
 * @param modifier Modifier for the card.
 * @param subtitle Optional secondary text displayed below the title.
 * @param leadingIcon Optional icon displayed on the leading side inside a colored circle.
 * @param iconTint The tint color for the leading icon.
 * @param iconBackgroundColor The background color of the circle behind the leading icon.
 * @param trailingContent Optional composable content on the trailing side (e.g., switch, badge).
 * @param showChevron Whether to show a chevron arrow on the trailing side (hidden if trailingContent is set).
 * @param enabled Whether the card is enabled for interaction.
 * @param onClick Optional click handler for the card.
 */
@Composable
fun AppCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    iconBackgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    trailingContent: @Composable (() -> Unit)? = null,
    showChevron: Boolean = true,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.6f),
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        enabled = enabled,
        onClick = onClick ?: {},
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                if (leadingIcon != null) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(iconBackgroundColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (enabled) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailingContent()
            } else if (showChevron && onClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * A section header for grouping AppCards.
 */
@Composable
fun AppCardSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 4.dp, top = 16.dp, bottom = 8.dp),
    )
}

// ── Previews ──

@Preview(showBackground = true, name = "AppCard - Basic")
@Composable
private fun AppCardBasicPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppCard(
                title = "Theme",
                subtitle = "System Default",
                leadingIcon = Icons.Default.Palette,
                onClick = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "AppCard - With Switch")
@Composable
private fun AppCardWithSwitchPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppCard(
                title = "Auto Backup",
                subtitle = "Automatically backup data",
                leadingIcon = Icons.Default.Settings,
                trailingContent = {
                    Switch(checked = true, onCheckedChange = {})
                },
                onClick = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "AppCard - Destructive")
@Composable
private fun AppCardDestructivePreview() {
    AntCashManagerTheme(dynamicColor = false) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppCard(
                title = "Delete All Data",
                subtitle = "Permanently remove all transactions",
                leadingIcon = Icons.Default.Delete,
                iconBackgroundColor = MaterialTheme.colorScheme.errorContainer,
                iconTint = MaterialTheme.colorScheme.onErrorContainer,
                showChevron = false,
                onClick = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "AppCard - Dark")
@Composable
private fun AppCardDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppCardSectionHeader(title = "Appearance")
            AppCard(
                title = "Theme",
                subtitle = "Dark",
                leadingIcon = Icons.Default.Palette,
                onClick = {},
            )
        }
    }
}

