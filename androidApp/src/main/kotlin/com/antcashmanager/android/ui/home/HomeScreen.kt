package com.antcashmanager.android.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.transaction.GetTransactionsUseCase

@Composable
fun HomeScreen(transactionRepository: TransactionRepository) {
    Logger.d("HomeScreen") { "Displaying HomeScreen" }
    val getTransactionsUseCase = GetTransactionsUseCase(transactionRepository)
    val transactions by getTransactionsUseCase().collectAsState(initial = emptyList())

    HomeContent(transactions = transactions)
}

@Composable
internal fun HomeContent(transactions: List<Transaction>) {
    val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val balance = totalIncome - totalExpense

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "Total Balance", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "€%.2f".format(balance),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Income", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "€%.2f".format(totalIncome),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Expenses", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "€%.2f".format(totalExpense),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Recent Transactions (${transactions.size})",
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Preview(showBackground = true, name = "HomeScreen - With Transactions")
@Composable
private fun HomeContentPreview() {
    AntCashManagerTheme {
        HomeContent(
            transactions = listOf(
                Transaction(
                    id = 1,
                    title = "Salary",
                    amount = 2500.0,
                    category = "Work",
                    type = TransactionType.INCOME,
                    timestamp = System.currentTimeMillis(),
                ),
                Transaction(
                    id = 2,
                    title = "Groceries",
                    amount = 85.50,
                    category = "Food",
                    type = TransactionType.EXPENSE,
                    timestamp = System.currentTimeMillis(),
                ),
                Transaction(
                    id = 3,
                    title = "Electric Bill",
                    amount = 120.0,
                    category = "Utilities",
                    type = TransactionType.EXPENSE,
                    timestamp = System.currentTimeMillis(),
                ),
            ),
        )
    }
}

@Preview(showBackground = true, name = "HomeScreen - Empty")
@Composable
private fun HomeContentEmptyPreview() {
    AntCashManagerTheme {
        HomeContent(transactions = emptyList())
    }
}

@Preview(showBackground = true, name = "HomeScreen - Dark Theme")
@Composable
private fun HomeContentDarkPreview() {
    AntCashManagerTheme(darkTheme = true) {
        HomeContent(
            transactions = listOf(
                Transaction(
                    id = 1,
                    title = "Salary",
                    amount = 2500.0,
                    category = "Work",
                    type = TransactionType.INCOME,
                    timestamp = System.currentTimeMillis(),
                ),
                Transaction(
                    id = 2,
                    title = "Rent",
                    amount = 800.0,
                    category = "Housing",
                    type = TransactionType.EXPENSE,
                    timestamp = System.currentTimeMillis(),
                ),
            ),
        )
    }
}
