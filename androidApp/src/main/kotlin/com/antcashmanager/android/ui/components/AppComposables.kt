package com.antcashmanager.android.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

// ══════════════════════════════════════════════════════════════════════════════
// SCREEN HEADER - Unified header for all screens
// ══════════════════════════════════════════════════════════════════════════════

/**
 * ScreenHeader - Composable riusabile per i titoli degli screen con icone/azioni
 * Standardizza padding e allineamento su tutti gli screen
 *
 * Utilizzo:
 * ScreenHeader(
 *     title = "Title",
 *     actions = { HelpButton(...) }
 * )
 */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        AppText(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        if (actions != null) {
            actions()
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// APP COMPOSABLE WRAPPERS - Material3 Components with Theme Consistency
// ══════════════════════════════════════════════════════════════════════════════

/**
 * AppSwitch - wrapper per Switch di Material3 con tema coerente
 * Utilizzo: AppSwitch(checked = state, onCheckedChange = { setState(it) })
 */
@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
    )
}

/**
 * AppRadioButton - wrapper per RadioButton di Material3
 * Utilizzo: AppRadioButton(selected = isSelected, onClick = { setSelected() })
 */
@Composable
fun AppRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    RadioButton(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    )
}

/**
 * AppListItem - wrapper per ListItem di Material3 con tema trasparente
 * Utilizzo: AppListItem(headlineContent = { AppText("Title") })
 */
@Composable
fun AppListItem(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    overlineContent: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    ListItem(
        headlineContent = headlineContent,
        modifier = modifier,
        overlineContent = overlineContent,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

/**
 * AppDivider - wrapper per HorizontalDivider di Material3
 * Utilizzo: AppDivider(modifier = Modifier.padding(horizontal = 16.dp))
 */
@Composable
fun AppDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = thickness,
        color = color,
    )
}

@Preview(name = "AppComposables - Light", showBackground = true)
@Composable
private fun AppComposablesPreviewLight() {
    AntCashManagerTheme(dynamicColor = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ScreenHeader(title = "Header", actions = { AppText("Action") })
            AppListItem(
                headlineContent = { AppText("List item") },
                supportingContent = { AppText("Support text") },
                trailingContent = { AppSwitch(checked = true, onCheckedChange = {}) },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                AppRadioButton(selected = true, onClick = {})
                AppText("Radio")
            }
            AppDivider()
        }
    }
}

@Preview(
    name = "AppComposables - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun AppComposablesPreviewDark() {
    AppComposablesPreviewLight()
}

@Preview(name = "AppComposables - Accessibility", showBackground = true, fontScale = 1.5f)
@Composable
private fun AppComposablesPreviewAccessibility() {
    AppComposablesPreviewLight()
}
