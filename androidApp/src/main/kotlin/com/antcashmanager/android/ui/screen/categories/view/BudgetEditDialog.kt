package com.antcashmanager.android.ui.screen.categories.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.antcashmanager.domain.model.CategoryBudget

/**
 * Dialog to set or update a monthly budget limit for a category.
 *
 * Shows the current limit pre-filled (if any). Calls [onConfirm] only when the
 * entered value is a valid positive number.
 */
@Composable
fun BudgetEditDialog(
    categoryName: String,
    existingBudget: CategoryBudget?,
    onConfirm: (monthlyLimit: Double) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    var input by remember {
        mutableStateOf(existingBudget?.monthlyLimit?.let { "%.2f".format(it) } ?: "")
    }
    val parsed = input.replace(",", ".").toDoubleOrNull()
    val isValid = parsed != null && parsed > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.categories_budget_set),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column {
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text(stringResource(R.string.categories_budget_monthly)) },
                    singleLine = true,
                    isError = input.isNotEmpty() && !isValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (isValid) onConfirm(parsed!!) },
                enabled = isValid,
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            if (onDelete != null && existingBudget != null) {
                TextButton(onClick = onDelete) {
                    Text(
                        text = stringResource(R.string.dialog_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        },
    )
}

@Preview(showBackground = true, name = "BudgetEditDialog - Light")
@Composable
private fun BudgetEditDialogLightPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        BudgetEditDialog(
            categoryName = "Food",
            existingBudget = null,
            onConfirm = {},
            onDelete = null,
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true, name = "BudgetEditDialog - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BudgetEditDialogDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        BudgetEditDialog(
            categoryName = "Transport",
            existingBudget = CategoryBudget(categoryId = 2, monthlyLimit = 150.0),
            onConfirm = {},
            onDelete = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true, name = "BudgetEditDialog - 2x", fontScale = 2.0f)
@Composable
private fun BudgetEditDialogLargeTextPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        BudgetEditDialog(
            categoryName = "Utilities",
            existingBudget = CategoryBudget(categoryId = 3, monthlyLimit = 80.0),
            onConfirm = {},
            onDelete = {},
            onDismiss = {},
        )
    }
}
