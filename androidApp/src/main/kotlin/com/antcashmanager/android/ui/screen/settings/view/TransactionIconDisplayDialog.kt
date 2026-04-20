package com.antcashmanager.android.ui.screen.settings.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.AppListItem
import com.antcashmanager.android.ui.components.AppRadioButton
import com.antcashmanager.domain.model.TransactionDisplayType

@Composable
fun TransactionIconDisplayDialog(
    currentDisplayType: TransactionDisplayType,
    onDisplayTypeSelected: (TransactionDisplayType) -> Unit,
    onDismiss: () -> Unit,
) {
    val options = listOf(
        TransactionDisplayType.TREND to stringResource(R.string.settings_transaction_icon_display_trend),
        TransactionDisplayType.CATEGORY to stringResource(R.string.settings_transaction_icon_display_category),
        TransactionDisplayType.NONE to stringResource(R.string.settings_transaction_icon_display_none),
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { Text(stringResource(R.string.dialog_choose_transaction_icon_display)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                options.forEach { (displayType, label) ->
                    AppListItem(
                        headlineContent = { Text(label) },
                        leadingContent = {
                            AppRadioButton(
                                selected = displayType == currentDisplayType,
                                onClick = { onDisplayTypeSelected(displayType) },
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

