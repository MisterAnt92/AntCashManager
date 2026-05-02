package com.antcashmanager.android.ui.screen.settings.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.AppListItem
import com.antcashmanager.android.ui.components.AppRadioButton
import com.antcashmanager.domain.model.TransactionDisplayType

@Composable
fun TransactionDisplayDialog(
    currentDisplayType: TransactionDisplayType,
    onDisplayTypeSelected: (TransactionDisplayType) -> Unit,
    onDismiss: () -> Unit,
) {
    val options = listOf(
        TransactionDisplayType.TREND to stringResource(R.string.settings_transaction_display_trend),
        TransactionDisplayType.CATEGORY to stringResource(R.string.settings_transaction_display_category),
        TransactionDisplayType.NONE to stringResource(R.string.settings_transaction_display_none),
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(imageVector = Icons.Default.Visibility, contentDescription = null) },
        title = { Text(stringResource(R.string.dialog_choose_transaction_display)) },
        text = {
            Column {
                options.forEach { (type, label) ->
                    AppListItem(
                        headlineContent = { Text(label) },
                        leadingContent = {
                            AppRadioButton(
                                selected = type == currentDisplayType,
                                onClick = { onDisplayTypeSelected(type) })
                        },
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) } },
    )
}

