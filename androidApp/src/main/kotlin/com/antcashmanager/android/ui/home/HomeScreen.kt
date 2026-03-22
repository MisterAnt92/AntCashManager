package com.antcashmanager.android.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.AntEmptyState
import com.antcashmanager.android.ui.components.DateRangeFilter
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.util.LocalCurrencyFormat
import com.antcashmanager.android.util.formatAmount
import com.antcashmanager.android.util.formatAmountWithSign
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.transaction.GetTransactionsUseCase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(transactionRepository: TransactionRepository) {
    Logger.d("HomeScreen") { "Displaying HomeScreen" }
    val getTransactionsUseCase = GetTransactionsUseCase(transactionRepository)
    val transactions by getTransactionsUseCase().collectAsState(initial = emptyList())

    HomeContent(transactions = transactions)
}

@Composable
internal fun HomeContent(transactions: List<Transaction>) {
    val selectedPresetIndex = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(1) }
    val dateRangeFrom = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)) }
    val dateRangeTo = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(System.currentTimeMillis()) }

    // Preset labels
    val presets = listOf(
        "Oggi" to "today",
        "7 giorni" to "week",
        "Mese" to "month",
        "Anno" to "year",
    )

    // Filter transactions by date range
    val filteredTransactions = transactions.filter { it.timestamp in dateRangeFrom.value..dateRangeTo.value }

    val fmt = LocalCurrencyFormat.current
    val totalIncome = filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val balance = totalIncome - totalExpense

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.home_dashboard),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        
        item {
            DateRangeFilter(
                selectedPresetIndex = selectedPresetIndex.value,
                presets = presets,
                dateRangeFrom = dateRangeFrom.value,
                dateRangeTo = dateRangeTo.value,
                onPresetSelected = { index ->
                    selectedPresetIndex.value = index
                    when (index) {
                        0 -> {
                            dateRangeFrom.value = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
                        }
                        1 -> {
                            dateRangeFrom.value = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
                        }
                        2 -> {
                            dateRangeFrom.value = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000)
                        }
                        3 -> {
                            dateRangeFrom.value = System.currentTimeMillis() - (365 * 24 * 60 * 60 * 1000)
                        }
                    }
                    dateRangeTo.value = System.currentTimeMillis()
                },
                onFromDateEdit = { /* TODO: Show date picker */ },
                onToDateEdit = { /* TODO: Show date picker */ },
            )
        }

        // Balance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.home_total_balance),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatAmount(balance, fmt),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (balance >= 0) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                    )
                }
            }
        }

        // Income / Expense Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.padding(start = 8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.home_income),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                            )
                            Text(
                                text = formatAmount(totalIncome, fmt),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.padding(start = 8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.home_expenses),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                            )
                            Text(
                                text = formatAmount(totalExpense, fmt),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                }
            }
        }

        // Recent Transactions header
        item {
            Text(
                text = stringResource(R.string.home_recent_transactions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        if (transactions.isEmpty()) {
            item {
                AntEmptyState(
                    mascotRes = R.drawable.ic_ant_mascot,
                    title = stringResource(R.string.home_no_transactions),
                    subtitle = stringResource(R.string.home_empty_ant),
                )
            }
        } else {
            items(filteredTransactions.take(5)) { transaction ->
                RecentTransactionItem(transaction = transaction)
            }
        }

        // Bottom spacer
        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

private val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

@Composable
private fun RecentTransactionItem(transaction: Transaction) {
    val fmt = LocalCurrencyFormat.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (transaction.type == TransactionType.INCOME) {
                    Icons.Default.ArrowDownward
                } else {
                    Icons.Default.ArrowUpward
                },
                contentDescription = null,
                tint = if (transaction.type == TransactionType.INCOME) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.padding(start = 12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${transaction.category} • ${dateFormat.format(Date(transaction.timestamp))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (transaction.notes.isNotBlank()) {
                    Text(
                        text = transaction.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (transaction.isRecurring) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = stringResource(R.string.transactions_recurring),
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.tertiary,
                        )
                        Spacer(modifier = Modifier.padding(start = 4.dp))
                        Text(
                            text = stringResource(R.string.transactions_recurring),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
            }
            Text(
                text = formatAmountWithSign(transaction.amount, fmt, transaction.type == TransactionType.INCOME),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (transaction.type == TransactionType.INCOME) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
        }
    }
}

// ── Previews ──

@Preview(showBackground = true, name = "HomeScreen - With Transactions")
@Composable
private fun HomeContentPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        HomeContent(
            transactions = listOf(
                Transaction(id = 1, title = "Salary", amount = 2500.0, category = "Work", type = TransactionType.INCOME, timestamp = System.currentTimeMillis()),
                Transaction(id = 2, title = "Groceries", amount = 85.50, category = "Food", type = TransactionType.EXPENSE, timestamp = System.currentTimeMillis()),
                Transaction(id = 3, title = "Electric Bill", amount = 120.0, category = "Utilities", type = TransactionType.EXPENSE, timestamp = System.currentTimeMillis()),
            ),
        )
    }
}

@Preview(showBackground = true, name = "HomeScreen - Empty")
@Composable
private fun HomeContentEmptyPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        HomeContent(transactions = emptyList())
    }
}

@Preview(showBackground = true, name = "HomeScreen - Dark Theme")
@Composable
private fun HomeContentDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        HomeContent(
            transactions = listOf(
                Transaction(id = 1, title = "Salary", amount = 2500.0, category = "Work", type = TransactionType.INCOME, timestamp = System.currentTimeMillis()),
                Transaction(id = 2, title = "Rent", amount = 800.0, category = "Housing", type = TransactionType.EXPENSE, timestamp = System.currentTimeMillis()),
            ),
        )
    }
}
