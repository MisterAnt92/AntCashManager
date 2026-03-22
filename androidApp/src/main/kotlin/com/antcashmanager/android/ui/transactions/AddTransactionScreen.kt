package com.antcashmanager.android.ui.transactions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.touchlab.kermit.Logger
import com.antcashmanager.android.R
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.TransactionRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ══════════════════════════════════════════════════════════════════════════════
// SCREEN
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    onNavigateBack: () -> Unit,
) {
    Logger.d("AddTransactionScreen") { "Displaying AddTransactionScreen" }

    val viewModel: TransactionsViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                TransactionsViewModel(transactionRepository, categoryRepository) as T
        },
    )

    val state by viewModel.state.collectAsState()

    AddTransactionContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onAddTransaction = { title, amount, category, type, timestamp, notes, payee, location, tags, isRecurring, recurrenceInterval ->
            viewModel.addTransaction(title, amount, category, type, timestamp, notes, payee, location, tags, isRecurring, recurrenceInterval)
            onNavigateBack()
        },
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// CONTENT
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddTransactionContent(
    state: TransactionsState,
    onNavigateBack: () -> Unit,
    onAddTransaction: (String, Double, String, TransactionType, Long, String, String, String, String, Boolean, String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Additional fields
    var notes by remember { mutableStateOf("") }
    var payee by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var isRecurring by remember { mutableStateOf(false) }
    var recurrenceInterval by remember { mutableStateOf("") }
    var showRecurrenceMenu by remember { mutableStateOf(false) }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { date ->
                            selectedDate = date
                        }
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_transaction)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_cancel),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            // === REQUIRED FIELDS SECTION ===
            Text(
                text = "Informazioni obbligatorie",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Title field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.transaction_title)) },
                modifier = Modifier
                    .fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Amount field
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(stringResource(R.string.transaction_amount)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Transaction type (Radio buttons)
            Text(stringResource(R.string.transaction_type), style = MaterialTheme.typography.labelLarge)
            Column(modifier = Modifier.selectableGroup()) {
                TransactionType.entries.forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = transactionType == type,
                            onClick = { transactionType = type },
                        )
                        Text(
                            text = type.name,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category dropdown
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = { },
                label = { Text(stringResource(R.string.transaction_category)) },
                modifier = Modifier
                    .fillMaxWidth(),
                readOnly = true,
                shape = RoundedCornerShape(16.dp),
                trailingIcon = {
                    IconButton(onClick = { showCategoryMenu = !showCategoryMenu }) {
                        Text("▼")
                    }
                },
            )
            if (showCategoryMenu) {
                DropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    state.categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                selectedCategory = category.name
                                showCategoryMenu = false
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date field
            OutlinedTextField(
                value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selectedDate)),
                onValueChange = { },
                label = { Text(stringResource(R.string.transaction_date)) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                shape = RoundedCornerShape(16.dp),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Text("📅")
                    }
                },
            )

            Spacer(modifier = Modifier.height(24.dp))

            // === OPTIONAL FIELDS SECTION ===
            Text(
                text = "Informazioni aggiuntive (opzionali)",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Payee field
            OutlinedTextField(
                value = payee,
                onValueChange = { payee = it },
                label = { Text("Beneficiario") },
                placeholder = { Text("Es: Ristorante, Farmacia") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Location field
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Luogo") },
                placeholder = { Text("Es: Centro Commerciale") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Notes field
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Note") },
                placeholder = { Text("Aggiungi note o dettagli...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(16.dp),
                maxLines = 4,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tags field
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Tag") },
                placeholder = { Text("Es: urgente, importante (separati da virgola)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // === RECURRING SECTION ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                androidx.compose.material3.Checkbox(
                    checked = isRecurring,
                    onCheckedChange = { isRecurring = it },
                )
                Text(
                    text = "Ricorrente",
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (isRecurring) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = recurrenceInterval,
                    onValueChange = { },
                    label = { Text("Intervallo di ricorrenza") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = {
                        IconButton(onClick = { showRecurrenceMenu = !showRecurrenceMenu }) {
                            Text("▼")
                        }
                    },
                )
                if (showRecurrenceMenu) {
                    DropdownMenu(
                        expanded = showRecurrenceMenu,
                        onDismissRequest = { showRecurrenceMenu = false },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        listOf("Giornaliero", "Settimanale", "Mensile", "Annuale").forEach { interval ->
                            DropdownMenuItem(
                                text = { Text(interval) },
                                onClick = {
                                    recurrenceInterval = interval
                                    showRecurrenceMenu = false
                                },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit button
            Button(
                onClick = {
                    if (title.isNotBlank() && amount.isNotBlank() && selectedCategory.isNotBlank()) {
                        onAddTransaction(
                            title,
                            amount.toDoubleOrNull() ?: 0.0,
                            selectedCategory,
                            transactionType,
                            selectedDate,
                            notes,
                            payee,
                            location,
                            tags,
                            isRecurring,
                            recurrenceInterval
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Text(stringResource(R.string.add_transaction))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
