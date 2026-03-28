package com.antcashmanager.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
 * Utilizzo: AppListItem(headlineContent = { Text("Title") })
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

/**
 * AppColumn - wrapper per Column con padding e spaziatura coerenti al tema
 * Utilizzo: AppColumn { AppText("Hello") }
 */
@Composable
fun AppColumn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.surface),
    ) {
        content()
    }
}

