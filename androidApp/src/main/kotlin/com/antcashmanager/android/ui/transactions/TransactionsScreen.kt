package com.antcashmanager.android.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import co.touchlab.kermit.Logger
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.AntEmptyState
import com.antcashmanager.android.ui.components.AnimatedCard
import com.antcashmanager.android.ui.components.AnimatedListItem
import com.antcashmanager.android.ui.components.DateRangeFilter
import com.antcashmanager.android.ui.components.SkeletonLoader
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.ui.theme.IncomeGreen
import com.antcashmanager.android.ui.theme.ExpenseRed
import com.antcashmanager.android.util.LocalCurrencyFormat
import com.antcashmanager.android.util.formatAmountWithSign
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.TransactionRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ══════════════════════════════════════════════════════════════════════════════
// SCREEN
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun TransactionsScreen(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    navController: NavController? = null,
) {
    Logger.d("TransactionsScreen") { "Displaying TransactionsScreen" }

    val viewModel: TransactionsViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                TransactionsViewModel(transactionRepository, categoryRepository) as T
        },
    )

    val state by viewModel.state.collectAsState()

    TransactionsContent(
        state = state,
        onEvent = viewModel::onEvent,
        navController = navController,
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// CONTENT
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransactionsContent(
    state: TransactionsState,
    onEvent: (TransactionsEvent) -> Unit,
    navController: NavController? = null,
) {
    // Date picker state
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // From date picker dialog
    if (showFromDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.dateRangeFrom)
        DatePickerDialog(
            onDismissRequest = { showFromDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate ->
                            onEvent(TransactionsEvent.SetDateRange(selectedDate, state.dateRangeTo))
                        }
                        showFromDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showFromDatePicker = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // To date picker dialog
    if (showToDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.dateRangeTo)
        DatePickerDialog(
            onDismissRequest = { showToDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate ->
                            onEvent(TransactionsEvent.SetDateRange(state.dateRangeFrom, selectedDate))
                        }
                        showToDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showToDatePicker = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController?.navigate("add_transaction") },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp)),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.transactions_add),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp),
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
        ) {
            // Header
            Text(
                text = stringResource(R.string.transactions_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date Range Filter
            DateRangeFilter(
                selectedPresetIndex = state.selectedPresetIndex,
                presets = TransactionsState.PRESETS,
                dateRangeFrom = state.dateRangeFrom,
                dateRangeTo = state.dateRangeTo,
                onPresetSelected = { onEvent(TransactionsEvent.SelectPreset(it)) },
                onFromDateEdit = { showFromDatePicker = true },
                onToDateEdit = { showToDatePicker = true },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            androidx.compose.material3.OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Cerca per nome o importo...") },
                placeholder = { Text("Es: Stipendio, 100") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                isError = searchQuery.length > 100,
            )

            if (searchQuery.isNotEmpty()) {
                Text(
                    text = "Risultati trovati: ${state.filteredTransactions.size}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content based on state
            when {
                state.isLoading -> LoadingState()
                state.transactions.isEmpty() -> EmptyState()
                else -> TransactionsList(
                    transactions = state.filteredTransactions,
                    onDelete = { onEvent(TransactionsEvent.DeleteTransaction(it)) },
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// COMPONENTS
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun LoadingState() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(5) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
            ) {
                // Header skeleton
                SkeletonLoader(height = 16.dp, cornerRadius = 8)
                Spacer(modifier = Modifier.height(8.dp))
                
                // Subtitle skeleton
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SkeletonLoader(
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp),
                        cornerRadius = 6,
                    )
                    SkeletonLoader(
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp),
                        cornerRadius = 6,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                // Amount skeleton
                SkeletonLoader(height = 20.dp, cornerRadius = 8)
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun EmptyState() {
    AntEmptyState(
        mascotRes = R.drawable.ic_ant_mascot,
        title = stringResource(R.string.transactions_empty),
        subtitle = stringResource(R.string.transactions_empty_ant),
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun TransactionsList(
    transactions: List<Transaction>,
    onDelete: (Transaction) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = transactions,
            key = { it.id },
        ) { transaction ->
            TransactionItem(transaction = transaction)
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
@Composable
private fun TransactionItem(transaction: Transaction) {
    val fmt = LocalCurrencyFormat.current
    val isIncome = transaction.type == TransactionType.INCOME
    val cardBackgroundColor = if (isIncome) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer

    AnimatedListItem(index = transaction.id.toInt()) {
        AnimatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(12.dp)),
            backgroundColor = cardBackgroundColor,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Icon with background
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (isIncome) IncomeGreen.copy(alpha = 0.25f) else ExpenseRed.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(8.dp),
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (isIncome) IncomeGreen else ExpenseRed,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isIncome) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                    )

                    // Subtitle
                    val subtitleParts = buildList {
                        add(transaction.category)
                        add(dateFormat.format(Date(transaction.timestamp)))
                        if (transaction.payee.isNotBlank()) add(transaction.payee)
                        if (transaction.location.isNotBlank()) add(transaction.location)
                    }
                    Text(
                        text = subtitleParts.joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isIncome) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                    )

                    // Notes
                    if (transaction.notes.isNotBlank()) {
                        Text(
                            text = transaction.notes,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isIncome) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    // Tags
                    if (transaction.tags.isNotBlank()) {
                        Text(
                            text = transaction.tags.split(",").joinToString(" ") { "#${it.trim()}" },
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
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = transaction.recurrenceInterval.ifBlank {
                                    stringResource(R.string.transactions_recurring)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                // Amount with background
                Box(
                    modifier = Modifier
                        .background(
                            if (isIncome) IncomeGreen.copy(alpha = 0.15f) else ExpenseRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                        )
                        .padding(8.dp),
                ) {
                    Text(
                        text = formatAmountWithSign(transaction.amount, fmt, isIncome),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isIncome) IncomeGreen else ExpenseRed,
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// PREVIEWS
// ══════════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true, name = "TransactionsScreen - With Data")
@Composable
private fun TransactionsContentPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        TransactionsContent(
            state = TransactionsState(
                transactions = sampleTransactions,
                filteredTransactions = sampleTransactions,
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "TransactionsScreen - Empty")
@Composable
private fun TransactionsContentEmptyPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        TransactionsContent(
            state = TransactionsState(),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "TransactionsScreen - Loading")
@Composable
private fun TransactionsContentLoadingPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        TransactionsContent(
            state = TransactionsState(isLoading = true),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "TransactionsScreen - Dark Theme")
@Composable
private fun TransactionsContentDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        TransactionsContent(
            state = TransactionsState(
                transactions = sampleTransactions,
                filteredTransactions = sampleTransactions,
            ),
            onEvent = {},
        )
    }
}

private val sampleTransactions = listOf(
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
)
