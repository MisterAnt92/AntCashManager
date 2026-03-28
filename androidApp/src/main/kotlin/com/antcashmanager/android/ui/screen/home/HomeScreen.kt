package com.antcashmanager.android.ui.screen.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import co.touchlab.kermit.Logger
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.AnimatedCard
import com.antcashmanager.android.ui.components.AnimatedListItem
import com.antcashmanager.android.ui.components.AntEmptyState
import com.antcashmanager.android.ui.components.DateRangeFilter
import com.antcashmanager.android.ui.components.FadeInOnAppear
import com.antcashmanager.android.ui.components.HelpButton
import com.antcashmanager.android.ui.components.HelpDialogContent
import com.antcashmanager.android.ui.components.SimpleHelpFeature
import com.antcashmanager.android.ui.components.text.BalanceText
import com.antcashmanager.android.ui.components.text.CompactMoneyText
import com.antcashmanager.android.ui.components.text.TransactionAmountText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.ui.theme.ExpenseRed
import com.antcashmanager.android.ui.theme.IncomeGreen
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.TransactionRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ══════════════════════════════════════════════════════════════════════════════
// SCREEN
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun HomeScreen(transactionRepository: TransactionRepository) {
    Logger.d("HomeScreen") { "Displaying HomeScreen" }

    val viewModel: HomeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HomeViewModel(transactionRepository) as T
        },
    )

    val state by viewModel.state.collectAsState()

    HomeContent(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// CONTENT
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeContent(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
) {

    // Date picker state
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    // From date picker dialog
    if (showFromDatePicker) {
        val datePickerState =
            rememberDatePickerState(initialSelectedDateMillis = state.dateRangeFrom)
        DatePickerDialog(
            onDismissRequest = { showFromDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate ->
                            onEvent(HomeEvent.SetDateRange(selectedDate, state.dateRangeTo))
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
                            onEvent(HomeEvent.SetDateRange(state.dateRangeFrom, selectedDate))
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

    // Help dialog
    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }

    when {
        state.isLoading -> LoadingState()
        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Header with Help Button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(R.string.home_dashboard),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        HelpButton(onHelpClick = { showHelpDialog = true })
                    }
                }

                // Date Range Filter
                item {
                    DateRangeFilter(
                        selectedPresetIndex = state.selectedPresetIndex,
                        presets = HomeState.PRESETS,
                        dateRangeFrom = state.dateRangeFrom,
                        dateRangeTo = state.dateRangeTo,
                        onPresetSelected = { onEvent(HomeEvent.SelectPreset(it)) },
                        onFromDateEdit = { showFromDatePicker = true },
                        onToDateEdit = { showToDatePicker = true },
                    )
                }

                // Balance Card
                item {
                    BalanceCard(balance = state.balance)
                }

                // Income / Expense Row
                item {
                    IncomeExpenseRow(
                        totalIncome = state.totalIncome,
                        totalExpense = state.totalExpense,
                    )
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

                // Transactions content
                if (state.transactions.isEmpty()) {
                    item {
                        AntEmptyState(
                            mascotRes = R.drawable.ic_ant_mascot,
                            title = stringResource(R.string.home_no_transactions),
                            subtitle = stringResource(R.string.home_empty_ant),
                        )
                    }
                } else {
                    items(
                        items = state.recentTransactions,
                        key = { it.id },
                    ) { transaction ->
                        RecentTransactionItem(transaction = transaction)
                    }
                }

                // Bottom spacer
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// COMPONENTS
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun BalanceCard(
    balance: Double,
) {
    val balanceColor by animateColorAsState(
        targetValue = if (balance >= 0) IncomeGreen else ExpenseRed,
        animationSpec = tween(600),
        label = "balance_color",
    )

    FadeInOnAppear(durationMillis = 600) {
        AnimatedCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.home_total_balance),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(12.dp))
                BalanceText(
                    amount = balance,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontSize = 32,
                )
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(50.dp),
                        )
                        .background(
                            balanceColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = if (balance >= 0) "📈 Positive" else "📉 Negative",
                        style = MaterialTheme.typography.labelSmall,
                        color = balanceColor,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun IncomeExpenseRow(
    totalIncome: Double,
    totalExpense: Double,
) {
    FadeInOnAppear(durationMillis = 800) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Income Card
            AnimatedCard(
                modifier = Modifier
                    .weight(1f)
                    .shadow(6.dp, RoundedCornerShape(16.dp)),
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    IncomeGreen.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = IncomeGreen,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.home_income),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CompactMoneyText(
                        amount = totalIncome,
                        fontSize = 18,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // Expense Card
            AnimatedCard(
                modifier = Modifier
                    .weight(1f)
                    .shadow(6.dp, RoundedCornerShape(16.dp)),
                backgroundColor = MaterialTheme.colorScheme.errorContainer,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    ExpenseRed.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = ExpenseRed,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.home_expenses),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CompactMoneyText(
                        amount = totalExpense,
                        fontSize = 18,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

private val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

@Composable
private fun RecentTransactionItem(transaction: Transaction) {
    val isIncome = transaction.type == TransactionType.INCOME
    val cardBackgroundColor =
        if (isIncome) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer

    AnimatedListItem(index = transaction.id.toInt()) {
        AnimatedCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = cardBackgroundColor,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isIncome) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Text(
                        text = "${transaction.category} • ${dateFormat.format(Date(transaction.timestamp))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isIncome) MaterialTheme.colorScheme.onSecondaryContainer.copy(
                            alpha = 0.7f
                        ) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                    )
                    if (transaction.notes.isNotBlank()) {
                        Text(
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
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.transactions_recurring),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Amount with background
                Box(
                    modifier = Modifier
                        .background(
                            if (isIncome) IncomeGreen.copy(alpha = 0.15f) else ExpenseRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                        )
                        .padding(8.dp),
                ) {
                    TransactionAmountText(
                        amount = if (isIncome) transaction.amount else -transaction.amount,
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// PREVIEWS
// ══════════════════════════════════════════════════════════════════════════════

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

@Preview(showBackground = true, name = "HomeScreen - With Transactions")
@Composable
private fun HomeContentPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        HomeContent(
            state = HomeState(
                transactions = sampleTransactions,
                filteredTransactions = sampleTransactions,
                recentTransactions = sampleTransactions,
                totalIncome = 2500.0,
                totalExpense = 205.5,
                balance = 2294.5,
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "HomeScreen - Empty")
@Composable
private fun HomeContentEmptyPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        HomeContent(
            state = HomeState(),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "HomeScreen - Loading")
@Composable
private fun HomeContentLoadingPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        HomeContent(
            state = HomeState(isLoading = true),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "HomeScreen - Dark Theme")
@Composable
private fun HomeContentDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        HomeContent(
            state = HomeState(
                transactions = sampleTransactions,
                filteredTransactions = sampleTransactions,
                recentTransactions = sampleTransactions,
                totalIncome = 2500.0,
                totalExpense = 205.5,
                balance = 2294.5,
            ),
            onEvent = {},
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// HELP DIALOG
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun HelpDialog(onDismiss: () -> Unit) {
    val helpFeatures = listOf(
        SimpleHelpFeature(
            title = "Dashboard",
            description = "Visualizza il saldo totale, entrate e uscite nel periodo selezionato.",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
        ),
        SimpleHelpFeature(
            title = "Filtri Intervallo Date",
            description = "Filtra le transazioni per date specifiche o usa i preset disponibili.",
            icon = Icons.Default.ArrowUpward,
        ),
        SimpleHelpFeature(
            title = "Transazioni Recenti",
            description = "Visualizza le ultime transazioni aggiunte con dettagli e categoria.",
            icon = Icons.Default.Repeat,
        ),
    )

    HelpDialogContent(
        isVisible = true,
        title = "Guida Dashboard",
        description = "Benvenuto nel Dashboard! Qui puoi visualizzare il riepilogo finanziario e le transazioni recenti.",
        features = helpFeatures,
        onDismiss = onDismiss,
    )
}
