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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
    ) {
        Text(
            text = "Transactions",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (transactions.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "No transactions yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your transactions will appear here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(transactions) { transaction ->
                    TransactionItem(transaction = transaction)
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

@Composable
private fun TransactionItem(transaction: Transaction) {
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
                Text(
                    text = "${transaction.category} • ${dateFormat.format(Date(transaction.timestamp))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}€%.2f".format(transaction.amount),
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
