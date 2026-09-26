package com.antcashmanager.android.ui.screen.transactions.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.ui.mapper.recurrenceIntervalLabelRes
import com.antcashmanager.android.ui.components.animation.AnimatedCard
import com.antcashmanager.android.ui.components.animation.AnimatedListItem
import com.antcashmanager.android.ui.components.layout.HorizontalSpacer
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.components.text.TransactionAmountText
import com.antcashmanager.android.ui.screen.categories.view.categoryIconMap
import com.antcashmanager.android.ui.theme.ExpenseRed
import com.antcashmanager.android.ui.theme.IncomeGreen
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
    onClick: (() -> Unit)? = null,
    displayType: TransactionDisplayType = TransactionDisplayType.TREND,
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val isAnnaTheme = LocalAnnaTheme.current
    val cardBackgroundColor = when {
        isAnnaTheme && isIncome  -> MaterialTheme.colorScheme.primaryContainer
        isAnnaTheme && !isIncome -> MaterialTheme.colorScheme.secondaryContainer
        isIncome                 -> MaterialTheme.colorScheme.secondaryContainer
        else                     -> MaterialTheme.colorScheme.errorContainer
    }

    AnimatedListItem(index = transaction.id.toInt()) {
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
                // Icon based on display type
                when (displayType) {
                    TransactionDisplayType.TREND -> {
                        Box(
                            modifier =
                                Modifier
                                    .size(44.dp)
                                    .background(
                                        if (isIncome) {
                                            IncomeGreen.copy(alpha = 0.25f)
                                        } else {
                                            ExpenseRed.copy(
                                                alpha = 0.25f,
                                            )
                                        },
                                        shape = RoundedCornerShape(32.dp),
                                    ).padding(8.dp),
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
                        val categoryIconVector = categoryIconMap[transaction.categoryIcon]
                        Box(
                            modifier =
                                Modifier
                                    .size(44.dp)
                                    .background(
                                        color =
                                            androidx.compose.ui.graphics
                                                .Color(transaction.categoryColor),
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                    ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (categoryIconVector != null) {
                                Icon(
                                    imageVector = categoryIconVector,
                                    contentDescription = transaction.category,
                                    tint = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.size(24.dp),
                                )
                            } else {
                                AppText(
                                    text = transaction.category.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = androidx.compose.ui.graphics.Color.White,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        HorizontalSpacer(SpacingSize.SM)
                    }

                    TransactionDisplayType.NONE -> {
                        // No icon
                    }
                }

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
