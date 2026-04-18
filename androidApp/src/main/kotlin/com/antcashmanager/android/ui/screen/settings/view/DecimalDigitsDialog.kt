package com.antcashmanager.android.ui.screen.settings.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Exposure
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.AppListItem
import com.antcashmanager.android.ui.components.AppRadioButton

@Composable
fun DecimalDigitsDialog(
    currentDigits: Int,
    onDigitsSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Exposure,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { Text(stringResource(R.string.dialog_choose_decimal_digits)) },
        text = {
            Column {
                (0..4).forEach { digits ->
                    AppListItem(
                        headlineContent = {
                            Text(stringResource(R.string.settings_decimal_digits_subtitle, digits))
                        },
                        leadingContent = {
                            AppRadioButton(
                                selected = digits == currentDigits,
                                onClick = { onDigitsSelected(digits) },
                            )
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) }
        },
    )
}