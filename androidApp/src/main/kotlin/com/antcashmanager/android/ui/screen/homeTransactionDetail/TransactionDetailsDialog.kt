package com.antcashmanager.android.ui.screen.homeTransactionDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.home.view.getRecurrenceIntervalLabel
import com.antcashmanager.android.ui.screen.homeTransactionDetail.view.TransactionDetailRow
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.util.isValidNote
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

@Composable
fun TransactionDetailsDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: TransactionDetailsViewModel = viewModel()
    val isIncome = transaction.type == TransactionType.INCOME

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            AppText(
                text = stringResource(R.string.transaction_details_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Title
                TransactionDetailRow(
                    label = stringResource(R.string.transaction_details_title),
                    value = transaction.title,
                )

                // Amount
                TransactionDetailRow(
                    label = stringResource(R.string.transaction_details_amount),
                    value = "${String.format("%.2f", kotlin.math.abs(transaction.amount))}€",
                )

                // Category
                TransactionDetailRow(
                    label = stringResource(R.string.transaction_details_category),
                    value = transaction.category,
                )

                // Type
                TransactionDetailRow(
                    label = stringResource(R.string.transaction_details_type),
                    value = if (isIncome) {
                        stringResource(R.string.transactions_type_income)
                    } else {
                        stringResource(R.string.transactions_type_expense)
                    },
                )

                // Payment Type
                TransactionDetailRow(
                    label = stringResource(R.string.transaction_details_payment_type),
                    value = when (transaction.paymentType) {
                        PaymentType.ELECTRONIC -> stringResource(R.string.payment_type_electronic)
                        PaymentType.CASH -> stringResource(R.string.payment_type_cash)
                        PaymentType.MEAL_VOUCHERS -> stringResource(R.string.payment_type_meal_vouchers)
                    },
                )

                // Date
                TransactionDetailRow(
                    label = stringResource(R.string.transaction_details_date),
                    value = dateFormat.format(Date(transaction.timestamp)),
                )

                // Time
                TransactionDetailRow(
                    label = stringResource(R.string.transaction_details_time),
                    value = timeFormat.format(Date(transaction.timestamp)),
                )

                // Notes (if not empty)
                if (transaction.notes.isValidNote()) {
                    TransactionDetailRow(
                        label = stringResource(R.string.transaction_details_notes),
                        value = transaction.notes,
                    )
                }

                // Payee (if not empty)
                if (transaction.payee.isNotBlank()) {
                    TransactionDetailRow(
                        label = stringResource(R.string.transaction_details_payee),
                        value = transaction.payee,
                    )
                }

                // Location (if not empty)
                if (transaction.location.isNotBlank()) {
                    TransactionDetailRow(
                        label = stringResource(R.string.transaction_details_location),
                        value = transaction.location,
                    )
                }

                // Recurrence (if enabled)
                if (transaction.isRecurring) {
                    TransactionDetailRow(
                        label = stringResource(R.string.transaction_details_recurrence),
                        value = if (transaction.recurrenceInterval.isNotBlank()) {
                            getRecurrenceIntervalLabel(
                                transaction.recurrenceInterval
                            )
                        } else {
                            stringResource(R.string.transactions_recurring)
                        },
                    )
                }

                // Tags (if not empty) - as colored chips
                if (transaction.tags.isNotBlank()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AppText(
                            text = stringResource(R.string.transaction_details_tags),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            val tagsList = transaction.tags.split(",").map { it.trim() }
                                .filter { it.isNotBlank() }
                            items(tagsList) { tag ->
                                SuggestionChip(
                                    onClick = { },
                                    label = {
                                        AppText(
                                            text = tag,
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                AppText(
                    text = stringResource(R.string.transaction_details_close),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    viewModel.shareTransaction(transaction, context)
                }
            ) {
                AppText(
                    text = "Condividi transazione",
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Preview(showBackground = true, name = "Transaction Details Dialog - Income")
@Composable
private fun TransactionDetailsDialogIncomePreview() {
    AntCashManagerTheme(dynamicColor = false) {
        TransactionDetailsDialog(
            transaction = Transaction(
                id = 1,
                title = "Salary Payment",
                amount = 2500.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = System.currentTimeMillis(),
                notes = "Monthly salary",
                payee = "Acme Corp",
                location = "Office",
                isRecurring = true,
                recurrenceInterval = "monthly",
                tags = "salary, income, work",
                paymentType = PaymentType.ELECTRONIC,
            ),
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true, name = "Transaction Details Dialog - Expense")
@Composable
private fun TransactionDetailsDialogExpensePreview() {
    AntCashManagerTheme(dynamicColor = false) {
        TransactionDetailsDialog(
            transaction = Transaction(
                id = 2,
                title = "Groceries",
                amount = 85.50,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis(),
                notes = "Weekly shopping",
                tags = "food, groceries, shopping",
                paymentType = PaymentType.CASH,
            ),
            onDismiss = {},
        )
    }
}
