package com.antcashmanager.android.ui.screen.home.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.layout.HorizontalSpacer
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.animation.AnimatedCard
import com.antcashmanager.android.ui.components.animation.AnimatedListItem
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.components.text.TransactionAmountText
import com.antcashmanager.android.ui.screen.categories.view.categoryIconMap
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.ui.theme.ExpenseRed
import com.antcashmanager.android.ui.theme.IncomeGreen
import com.antcashmanager.android.util.LocalAmountsMasked
import com.antcashmanager.android.util.isProtectedSalaryTransaction
import com.antcashmanager.android.util.isValidNote
import com.antcashmanager.android.util.translateCategory
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

@Composable
fun getRecurrenceIntervalLabel(interval: String): String {
    return when (interval.lowercase()) {
        "daily" -> stringResource(R.string.transactions_interval_daily)
        "weekly" -> stringResource(R.string.transactions_interval_weekly)
        "monthly" -> stringResource(R.string.transactions_interval_monthly)
        "yearly" -> stringResource(R.string.transactions_interval_yearly)
        else -> stringResource(R.string.transactions_recurring)
    }
}

@Composable
fun RecentTransactionItem(
    transaction: Transaction,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    displayType: TransactionDisplayType = TransactionDisplayType.TREND,
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val cardBackgroundColor =
        if (isIncome) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer

    AnimatedListItem(index = transaction.id.toInt()) {
        AnimatedCard(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            backgroundColor = cardBackgroundColor,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Icon based on display type
                when (displayType) {
                    TransactionDisplayType.TREND -> {
                        // Icon with background
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (isIncome) IncomeGreen.copy(alpha = 0.25f) else ExpenseRed.copy(
                                        alpha = 0.25f
                                    ),
                                    shape = RoundedCornerShape(32.dp),
                                )
                                .padding(8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = if (isIncome) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = if (isIncome) IncomeGreen else ExpenseRed,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        HorizontalSpacer(SpacingSize.SM)
                    }

                    TransactionDisplayType.CATEGORY -> {
                        // Category badge with icon from category data
                        val categoryIconVector = categoryIconMap[transaction.categoryIcon]
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    color = Color(transaction.categoryColor),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (categoryIconVector != null) {
                                // Show category icon
                                Icon(
                                    imageVector = categoryIconVector,
                                    contentDescription = transaction.category,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp),
                                )
                            } else {
                                // Fallback: show first letter of category name
                                AppText(
                                    text = transaction.category.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        HorizontalSpacer(SpacingSize.SM)
                    }

                    TransactionDisplayType.NONE -> {
                        // No icon - no spacer needed
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    AppText(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isIncome) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                    )
                    AppText(
                        text = stringResource(
                            R.string.home_transaction_item_subtitle,
                            translateCategory(transaction.category),
                            dateFormat.format(Date(transaction.timestamp))
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isIncome) MaterialTheme.colorScheme.onSecondaryContainer.copy(
                            alpha = 0.7f
                        ) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                    )
                    if (transaction.notes.isValidNote()) {
                        AppText(
                            text = transaction.notes,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isIncome) MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                alpha = 0.6f
                            ) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (transaction.isRecurring) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = stringResource(R.string.transactions_recurring),
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.tertiary,
                            )
                            HorizontalSpacer(SpacingSize.XXXS)
                            AppText(
                                text = if (transaction.recurrenceInterval.isNotBlank()) {
                                    getRecurrenceIntervalLabel(transaction.recurrenceInterval)
                                } else {
                                    stringResource(R.string.transactions_recurring)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                HorizontalSpacer(SpacingSize.XS)

                // Amount with background
                Box(
                    modifier = Modifier
                        .padding(8.dp),
                ) {
                    TransactionAmountText(
                        amount = transaction.amount, // Amount will already be negative for expenses
                        masked = LocalAmountsMasked.current && isProtectedSalaryTransaction(
                            transaction
                        ),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Recent Transaction Item - Income")
@Composable
private fun RecentTransactionItemIncomePreview() {
    AntCashManagerTheme(dynamicColor = false) {
        RecentTransactionItem(
            transaction = Transaction(
                id = 1,
                title = "Salary",
                amount = 2500.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = System.currentTimeMillis(),
                categoryIcon = "payments",
                categoryColor = 0xFF81C784,
            ),
        )
    }
}

@Preview(showBackground = true, name = "Recent Transaction Item - Expense")
@Composable
private fun RecentTransactionItemExpensePreview() {
    AntCashManagerTheme(dynamicColor = false) {
        RecentTransactionItem(
            transaction = Transaction(
                id = 2,
                title = "Groceries",
                amount = 85.50,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis(),
                notes = "Weekly shopping",
                categoryIcon = "restaurant",
                categoryColor = 0xFFE57373,
            ),
        )
    }
}

@Preview(showBackground = true, name = "Recent Transaction Item - Recurring Expense")
@Composable
private fun RecentTransactionItemRecurringExpensePreview() {
    AntCashManagerTheme(dynamicColor = false) {
        RecentTransactionItem(
            transaction = Transaction(
                id = 3,
                title = "Electric Bill",
                amount = 120.0,
                category = "Utilities",
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis(),
                isRecurring = true,
                recurrenceInterval = "monthly",
                categoryIcon = "receipt_long",
                categoryColor = 0xFFFFB74D,
            ),
        )
    }
}

@Preview(showBackground = true, name = "Transaction Item - Trend Display")
@Composable
private fun TransactionItemTrendPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        RecentTransactionItem(
            transaction = Transaction(
                id = 1,
                title = "Groceries",
                amount = -85.50,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis(),
            ),
            displayType = TransactionDisplayType.TREND,
        )
    }
}

@Preview(showBackground = true, name = "Transaction Item - Category Display")
@Composable
private fun TransactionItemCategoryPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        RecentTransactionItem(
            transaction = Transaction(
                id = 2,
                title = "Groceries",
                amount = -85.50,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis(),
                categoryIcon = "restaurant",
                categoryColor = 0xFFE57373,
            ),
            displayType = TransactionDisplayType.CATEGORY,
        )
    }
}

@Preview(showBackground = true, name = "Transaction Item - None Display")
@Composable
private fun TransactionItemNonePreview() {
    AntCashManagerTheme(dynamicColor = false) {
        RecentTransactionItem(
            transaction = Transaction(
                id = 3,
                title = "Groceries",
                amount = -85.50,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis(),
            ),
            displayType = TransactionDisplayType.NONE,
        )
    }
}
