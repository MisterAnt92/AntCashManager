package com.antcashmanager.android.ui.screen.settings.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

/**
 * Dialog for editing the meal voucher value.
 *
 * @param currentValue The current meal voucher value.
 * @param onDismiss Callback invoked when the dialog is dismissed without saving.
 * @param onConfirm Callback invoked when the user confirms the new value.
 */
@Composable
fun MealVoucherDialog(
    currentValue: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
) {
    var inputText by remember { mutableStateOf(currentValue.toString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.dialog_meal_voucher_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.dialog_meal_voucher_message),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { newInput ->
                        inputText = newInput
                        errorMessage = null
                    },
                    label = { Text(stringResource(R.string.dialog_meal_voucher_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val normalized = inputText.replace(',', '.')
                    val parsed = normalized.toDoubleOrNull()
                    when {
                        parsed == null -> {
                            errorMessage = "Valore non valido"
                        }

                        parsed <= 0.0 -> {
                            errorMessage = "Il valore deve essere maggiore di 0"
                        }

                        else -> {
                            onConfirm(parsed)
                        }
                    }
                },
            ) {
                Text(stringResource(R.string.common_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    )
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MealVoucherDialogPreview() {
    AntCashManagerTheme {
        MealVoucherDialog(
            currentValue = 5.29,
            onDismiss = {},
            onConfirm = {},
        )
    }
}
