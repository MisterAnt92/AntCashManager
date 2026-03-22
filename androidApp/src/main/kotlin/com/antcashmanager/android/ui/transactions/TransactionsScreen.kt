package com.antcashmanager.android.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.antcashmanager.android.R
import com.antcashmanager.android.util.LocalCurrencyFormat
import com.antcashmanager.android.util.formatAmountWithSign
import com.antcashmanager.android.ui.components.AntEmptyState
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.ui.components.DateRangeFilter
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.transaction.GetTransactionsUseCase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionsScreen(transactionRepository: TransactionRepository) {
    Logger.d("TransactionsScreen") { "Displaying TransactionsScreen" }
    val getTransactionsUseCase = GetTransactionsUseCase(transactionRepository)
    val transactions by getTransactionsUseCase().collectAsState(initial = emptyList())

    TransactionsContent(transactions = transactions)
}

@Composable
internal fun TransactionsContent(transactions: List<Transaction>) {
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

    androidx.compose.material3.Scaffold(
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(
                onClick = {
                    Logger.d("TransactionsScreen") { "Add transaction button clicked" }
                    // TODO: Navigate to add transaction screen
                },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add transaction",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
                .padding(padding),
        ) {
        Text(
            text = stringResource(R.string.transactions_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        // Date range filter
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
        Spacer(modifier = Modifier.height(16.dp))
        if (transactions.isEmpty()) {
            AntEmptyState(
                mascotRes = R.drawable.ic_ant_mascot,
                title = stringResource(R.string.transactions_empty),
                subtitle = stringResource(R.string.transactions_empty_ant),
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredTransactions) { transaction ->
                    TransactionItem(transaction = transaction)
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
        }
    }
}

private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

@Composable
private fun TransactionItem(transaction: Transaction) {
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
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
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
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val subtitleParts = buildList {
                    add(transaction.category)
                    add(dateFormat.format(Date(transaction.timestamp)))
                    if (transaction.payee.isNotBlank()) add(transaction.payee)
                    if (transaction.location.isNotBlank()) add(transaction.location)
                }
                Text(
                    text = subtitleParts.joinToString(" • "),
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
                if (transaction.tags.isNotBlank()) {
                    Text(
                        text = transaction.tags.split(",").joinToString(" ") { "#${it.trim()}" },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (transaction.isRecurring) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = stringResource(R.string.transactions_recurring),
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.tertiary,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = transaction.recurrenceInterval.ifBlank {
                                stringResource(R.string.transactions_recurring)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
            }
            Text(
                text = formatAmountWithSign(transaction.amount, fmt, transaction.type == TransactionType.INCOME),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
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

@Preview(showBackground = true, name = "TransactionsScreen - With Data")
@Composable
private fun TransactionsContentPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        TransactionsContent(
            transactions = listOf(
                Transaction(id = 1, title = "Salary", amount = 2500.0, category = "Work", type = TransactionType.INCOME, timestamp = System.currentTimeMillis()),
                Transaction(id = 2, title = "Groceries", amount = 85.50, category = "Food", type = TransactionType.EXPENSE, timestamp = System.currentTimeMillis()),
                Transaction(id = 3, title = "Electric Bill", amount = 120.0, category = "Utilities", type = TransactionType.EXPENSE, timestamp = System.currentTimeMillis()),
            ),
        )
    }
}

@Preview(showBackground = true, name = "TransactionsScreen - Empty")
@Composable
private fun TransactionsContentEmptyPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        TransactionsContent(transactions = emptyList())
    }
}

@Preview(showBackground = true, name = "TransactionsScreen - Dark Theme")
@Composable
private fun TransactionsContentDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        TransactionsContent(
            transactions = listOf(
                Transaction(id = 1, title = "Freelance Payment", amount = 1200.0, category = "Work", type = TransactionType.INCOME, timestamp = System.currentTimeMillis()),
                Transaction(id = 2, title = "Rent", amount = 800.0, category = "Housing", type = TransactionType.EXPENSE, timestamp = System.currentTimeMillis()),
            ),
        )
    }
}
