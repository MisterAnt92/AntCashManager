package com.antcashmanager.android.ui.screen.homeTransactionDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import org.koin.androidx.compose.koinViewModel

private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionDetailsDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
) {
    val viewModel: TransactionDetailsViewModel = koinViewModel()

    TransactionDetailsDialogContent(
        transaction = transaction,
        onDismiss = onDismiss,
        onShareTransaction = viewModel::shareTransaction,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TransactionDetailsDialogContent(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onShareTransaction: (Transaction, android.content.Context) -> Unit,
) {
    val context = LocalContext.current
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

                // Tags (if not empty) - as colored round chips
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
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            val tagsList = transaction.tags.split(",").map { it.trim() }
                                .filter { it.isNotBlank() }
                            tagsList.forEach { tag ->
                                AssistChip(
                                    onClick = { },
                                    label = {
                                        AppText(
                                            text = tag,
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Tag,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                            alpha = 0.4f
                                        ),
                                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    border = null
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
                    text = stringResource(R.string.common_close),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onShareTransaction(transaction, context)
                }
            ) {
                AppText(
                    text = stringResource(R.string.transaction_details_share),
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
        TransactionDetailsDialogContent(
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
            onShareTransaction = { _, _ -> },
        )
    }
}

@Preview(showBackground = true, name = "Transaction Details Dialog - Expense")
@Composable
private fun TransactionDetailsDialogExpensePreview() {
    AntCashManagerTheme(dynamicColor = false) {
        TransactionDetailsDialogContent(
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
            onShareTransaction = { _, _ -> },
        )
    }
}
