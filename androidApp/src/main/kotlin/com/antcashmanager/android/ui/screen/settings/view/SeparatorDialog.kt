package com.antcashmanager.android.ui.screen.settings.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.common.AppListItem
import com.antcashmanager.android.ui.components.common.AppRadioButton
import com.antcashmanager.android.ui.components.text.AppText

@Composable
fun SeparatorDialog(
    title: String,
    options: List<Pair<String, String>>,
    currentValue: String,
    onSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.TextFields,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { AppText(title) },
        text = {
            Column {
                options.forEach { (value, label) ->
                    AppListItem(
                        headlineContent = { AppText(label) },
                        leadingContent = {
                            AppRadioButton(
                                selected = value == currentValue,
                                onClick = { onSelected(value) },
                            )
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { AppText(stringResource(R.string.common_cancel)) }
        },
    )
}
