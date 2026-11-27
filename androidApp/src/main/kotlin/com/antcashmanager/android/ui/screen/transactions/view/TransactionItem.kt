package com.antcashmanager.android.ui.screen.transactions.view

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.animation.AnimatedCard
import com.antcashmanager.android.ui.components.animation.AnimatedListItem
import com.antcashmanager.android.ui.components.layout.HorizontalSpacer
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.components.text.TransactionAmountText
import com.antcashmanager.android.ui.components.transaction.TransactionIconBadge
import com.antcashmanager.android.ui.mapper.recurrenceIntervalLabelRes
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.ui.theme.LocalAnnaTheme
import com.antcashmanager.android.util.LocalAmountsMasked
import com.antcashmanager.android.util.isProtectedSalaryTransaction
import com.antcashmanager.android.util.isValidNote
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

@Composable
fun TransactionItem(
    transaction: Transaction,
    index: Int = 0,
    onClick: (() -> Unit)? = null,
    displayType: TransactionDisplayType = TransactionDisplayType.TREND,
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val isAnnaTheme = LocalAnnaTheme.current
    val cardBackgroundColor = when {
        isAnnaTheme && isIncome -> MaterialTheme.colorScheme.primaryContainer
        isAnnaTheme && !isIncome -> MaterialTheme.colorScheme.secondaryContainer
        isIncome -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.errorContainer
    }

    AnimatedListItem(index = index) {
        AnimatedCard(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .let { if (onClick != null) it.clickable { onClick() } else it },
            backgroundColor = cardBackgroundColor,
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TransactionIconBadge(
                    isIncome = isIncome,
                    displayType = displayType,
                    categoryIcon = transaction.categoryIcon,
                    categoryColor = transaction.categoryColor,
                    categoryName = transaction.category,
                    categoryInitial = transaction.category.take(1).uppercase(),
                )

                // Content
                Column(modifier = Modifier.weight(1f)) {
                    AppText(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isIncome) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                    )

                    // Subtitle
                    val subtitleParts =
                        buildList {
                            add(transaction.category)
                            add(dateFormat.format(Date(transaction.timestamp)))
                            if (transaction.payee.isNotBlank()) add(transaction.payee)
                            if (transaction.location.isNotBlank()) add(transaction.location)
                        }
                    AppText(
                        text = subtitleParts.joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall,
                        color =
                            if (isIncome) {
                                MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                    alpha = 0.7f,
                                )
                            } else {
                                MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                            },
                    )

                    // Notes
                    if (transaction.notes.isValidNote()) {
                        AppText(
                            text = transaction.notes,
                            style = MaterialTheme.typography.labelSmall,
                            color =
                                if (isIncome) {
                                    MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                        alpha = 0.6f,
                                    )
                                } else {
                                    MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f)
                                },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    // Tags
                    if (transaction.tags.isNotBlank()) {
                        AppText(
                            text =
                                transaction.tags
                                    .split(",")
                                    .joinToString(" ") { "#${it.trim()}" },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isIncome) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    // Recurring indicator
                    if (transaction.isRecurring) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = stringResource(R.string.transactions_recurring),
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.tertiary,
                            )
                            HorizontalSpacer(SpacingSize.XXXS)
                            AppText(
                                text =
                                    stringResource(recurrenceIntervalLabelRes(transaction.recurrenceInterval)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                // Amount with background
                Box(
                    modifier =
                        Modifier
                            .padding(8.dp),
                ) {
                    TransactionAmountText(
                        amount = transaction.amount, // Amount will already be negative for expenses
                        masked =
                            LocalAmountsMasked.current &&
                                isProtectedSalaryTransaction(
                                    transaction,
                                ),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "TransactionItem - Light")
@Composable
private fun TransactionItemLightPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        TransactionItem(
            transaction = Transaction(
                id = 1L,
                amount = 50.0,
                type = TransactionType.EXPENSE,
                title = "Caffè",
                category = "Food",
                timestamp = System.currentTimeMillis(),
            ),
            displayType = TransactionDisplayType.TREND,
            onClick = {},
        )
    }
}

@Preview(showBackground = true, name = "TransactionItem - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TransactionItemDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        TransactionItem(
            transaction = Transaction(
                id = 1L,
                amount = 100.0,
                type = TransactionType.INCOME,
                title = "Pagamento",
                category = "Salary",
                timestamp = System.currentTimeMillis(),
            ),
            displayType = TransactionDisplayType.CATEGORY,
            onClick = {},
        )
    }
}

@Preview(showBackground = true, name = "TransactionItem - 2x", fontScale = 2.0f)
@Composable
private fun TransactionItemLargeTextPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        TransactionItem(
            transaction = Transaction(
                id = 1L,
                amount = 30.50,
                type = TransactionType.EXPENSE,
                title = "Spesa alimentare",
                category = "Food",
                timestamp = System.currentTimeMillis(),
            ),
            displayType = TransactionDisplayType.CATEGORY,
            onClick = {},
        )
    }
}
