package com.antcashmanager.android.ui.screen.settings.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.common.AppListItem
import com.antcashmanager.android.ui.components.common.AppRadioButton
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.domain.model.CurrencyFormat

/**
 * Dialog per la selezione del simbolo di valuta.
 */
@Composable
fun CurrencySymbolDialog(
    currentSymbol: String,
    onSymbolSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.MonetizationOn,
                contentDescription = null,
            )
        },
        title = { AppText(stringResource(R.string.dialog_choose_currency)) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState()),
            ) {
                CurrencyFormat.SUPPORTED_CURRENCIES.forEach { (symbol, label) ->
                    AppListItem(
                        headlineContent = { AppText(label) },
                        leadingContent = {
                            AppRadioButton(
                                selected = symbol == currentSymbol,
                                onClick = { onSymbolSelected(symbol) },
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

