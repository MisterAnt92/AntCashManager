package com.antcashmanager.android.ui.screen.transaction_add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import co.touchlab.kermit.Logger
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.button.AppButton
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ══════════════════════════════════════════════════════════════════════════════
// SCREEN
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun AddTransactionScreen(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    onNavigateBack: () -> Unit,
    onTransactionAdded: () -> Unit,
) {
    Logger.d("AddTransactionScreen") { "Displaying AddTransactionScreen" }

    val viewModel: AddTransactionViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AddTransactionViewModel(transactionRepository, categoryRepository) as T
        },
    )

    val state by viewModel.state.collectAsState()
    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
    var hasNavigatedBack by remember { mutableStateOf(false) }

    // Naviga indietro solo una volta dopo il reset dello state (transazione completata)
    if (!hasNavigatedBack && state.selectedCategory == null && state.selectedType == null && 
        state.title.isEmpty() && state.currentStep == AddTransactionStep.CATEGORY_SELECTION &&
        state.isLoading == false) {
        hasNavigatedBack = true
        onTransactionAdded()
    }

    AddTransactionContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// CONTENT
// ══════════════════════════════════════════════════════════════════════════════

@Composable
internal fun AddTransactionContent(
    state: AddTransactionState,
    onEvent: (AddTransactionEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { _ ->
        // Content padding is applied via Column modifier below
        when (state.currentStep) {
            AddTransactionStep.CATEGORY_SELECTION -> {
                CategorySelectionStep(
                    categories = state.categories,
                    selectedCategory = state.selectedCategory,
                    onSelectCategory = { category ->
                        onEvent(AddTransactionEvent.SelectCategory(category))
                    },
                    onNext = {
                        onEvent(AddTransactionEvent.NextStep)
                    },
                    onCancel = onNavigateBack,
                )
            }

            AddTransactionStep.TYPE_SELECTION -> {
                TypeSelectionStep(
                    selectedType = state.selectedType,
                    selectedCategory = state.selectedCategory,
                    onSelectType = { type ->
                        onEvent(AddTransactionEvent.SelectType(type))
                    },
                    onNext = {
                        onEvent(AddTransactionEvent.NextStep)
                    },
                    onPrevious = {
                        onEvent(AddTransactionEvent.PreviousStep)
                    },
                    onCancel = onNavigateBack,
                )
            }

            AddTransactionStep.DETAILS -> {
                DetailsStep(
                    state = state,
                    onEvent = onEvent,
                    onNext = {
                        onEvent(AddTransactionEvent.NextStep)
                    },
                    onPrevious = {
                        onEvent(AddTransactionEvent.PreviousStep)
                    },
                    onCancel = onNavigateBack,
                )
            }

            AddTransactionStep.CONFIRMATION -> {
                ConfirmationStep(
                    state = state,
                    onSubmit = {
                        onEvent(AddTransactionEvent.Submit)
                    },
                    onPrevious = {
                        onEvent(AddTransactionEvent.PreviousStep)
                    },
                    onCancel = onNavigateBack,
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP 1: CATEGORY SELECTION
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySelectionStep(
    categories: List<Category>,
    selectedCategory: Category?,
    onSelectCategory: (Category) -> Unit,
    onNext: () -> Unit,
    onCancel: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText(stringResource(R.string.add_transaction_select_category), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.add_transaction_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            AppText(
                stringResource(R.string.add_transaction_choose_category),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            if (categories.isEmpty()) {
                AppText(stringResource(R.string.add_transaction_no_categories_available), style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                ) {
                    items(categories) { category ->
                        CategoryCard(
                            category = category,
                            isSelected = selectedCategory?.id == category.id,
                            onClick = { onSelectCategory(category) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppButton(
                    text = stringResource(R.string.add_transaction_cancel),
                    modifier = Modifier.weight(1f),
                    onClick = onCancel,
                )
                AppButton(
                    text = stringResource(R.string.add_transaction_next),
                    modifier = Modifier.weight(1f),
                    enabled = selectedCategory != null,
                    onClick = onNext,
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = category.icon,
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        AppText(
            text = category.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP 2: TYPE SELECTION
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeSelectionStep(
    selectedType: TransactionType?,
    selectedCategory: Category?,
    onSelectType: (TransactionType) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onCancel: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText(stringResource(R.string.add_transaction_select_type), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.add_transaction_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            AppText(
                stringResource(R.string.add_transaction_selected_category, selectedCategory?.name ?: stringResource(R.string.add_transaction_none)),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            AppText(
                stringResource(R.string.add_transaction_choose_type),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TransactionType.entries.forEach { type ->
                    TypeRadioButton(
                        type = type,
                        isSelected = selectedType == type,
                        onClick = { onSelectType(type) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppButton(
                    text = stringResource(R.string.add_transaction_previous),
                    modifier = Modifier.weight(1f),
                    onClick = onPrevious,
                )
                AppButton(
                    text = stringResource(R.string.add_transaction_next),
                    modifier = Modifier.weight(1f),
                    enabled = selectedType != null,
                    onClick = onNext,
                )
            }
        }
    }
}

@Composable
private fun TypeRadioButton(
    type: TransactionType,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = isSelected, onClick = onClick)
        val typeLabel = when (type) {
            TransactionType.INCOME -> stringResource(R.string.add_transaction_income_label)
            TransactionType.EXPENSE -> stringResource(R.string.add_transaction_expense_label)
        }
        AppText(
            text = typeLabel,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP 3: DETAILS
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsStep(
    state: AddTransactionState,
    onEvent: (AddTransactionEvent) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onCancel: () -> Unit,
) {
    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.timestamp)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { date ->
                            onEvent(AddTransactionEvent.UpdateTimestamp(date))
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
                title = { AppText(stringResource(R.string.add_transaction_details), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.add_transaction_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            AppText(
                stringResource(R.string.add_transaction_category_type, state.selectedCategory?.name ?: stringResource(R.string.add_transaction_none), if (state.selectedType == TransactionType.INCOME) stringResource(R.string.add_transaction_income_label) else stringResource(R.string.add_transaction_expense_label)),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            // Title
            OutlinedTextField(
                value = state.title,
                onValueChange = { onEvent(AddTransactionEvent.UpdateTitle(it)) },
                label = { Text(stringResource(R.string.add_transaction_title_required)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Amount
            OutlinedTextField(
                value = state.amount,
                onValueChange = { onEvent(AddTransactionEvent.UpdateAmount(it)) },
                label = { Text(stringResource(R.string.add_transaction_amount_required)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Date
            TextButton(onClick = { showDatePicker = true }) {
                Text(stringResource(R.string.add_transaction_date_label, SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(state.timestamp))))
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Notes
            OutlinedTextField(
                value = state.notes,
                onValueChange = { onEvent(AddTransactionEvent.UpdateNotes(it)) },
                label = { Text(stringResource(R.string.add_transaction_notes_label)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Payee
            OutlinedTextField(
                value = state.payee,
                onValueChange = { onEvent(AddTransactionEvent.UpdatePayee(it)) },
                label = { Text(stringResource(R.string.add_transaction_payee_label)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Location
            OutlinedTextField(
                value = state.location,
                onValueChange = { onEvent(AddTransactionEvent.UpdateLocation(it)) },
                label = { Text(stringResource(R.string.add_transaction_location_label)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppButton(
                    text = stringResource(R.string.add_transaction_previous),
                    modifier = Modifier.weight(1f),
                    onClick = onPrevious,
                )
                AppButton(
                    text = stringResource(R.string.add_transaction_next),
                    modifier = Modifier.weight(1f),
                    enabled = state.title.isNotBlank() && state.amount.isNotBlank(),
                    onClick = onNext,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmationStep(
    state: AddTransactionState,
    onSubmit: () -> Unit,
    onPrevious: () -> Unit,
    onCancel: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText(stringResource(R.string.add_transaction_confirmation), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.add_transaction_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            AppText(
                stringResource(R.string.add_transaction_summary),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            ConfirmationField(stringResource(R.string.add_transaction_field_category), state.selectedCategory?.name ?: "-")
            ConfirmationField(stringResource(R.string.add_transaction_field_type), if (state.selectedType == TransactionType.INCOME) stringResource(R.string.add_transaction_income_label) else stringResource(R.string.add_transaction_expense_label))
            ConfirmationField(stringResource(R.string.add_transaction_field_title), state.title)
            ConfirmationField(stringResource(R.string.add_transaction_field_amount), state.amount)
            ConfirmationField(stringResource(R.string.add_transaction_field_date), SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(state.timestamp)))
            if (state.notes.isNotEmpty()) {
                ConfirmationField(stringResource(R.string.add_transaction_field_notes), state.notes)
            }
            if (state.payee.isNotEmpty()) {
                ConfirmationField(stringResource(R.string.add_transaction_field_payee), state.payee)
            }
            if (state.location.isNotEmpty()) {
                ConfirmationField(stringResource(R.string.add_transaction_field_location), state.location)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppButton(
                    text = stringResource(R.string.add_transaction_previous),
                    modifier = Modifier.weight(1f),
                    onClick = onPrevious,
                )
                AppButton(
                    text = stringResource(R.string.add_transaction_save),
                    modifier = Modifier.weight(1f),
                    onClick = onSubmit,
                )
            }
        }
    }
}

@Composable
private fun ConfirmationField(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        AppText(label, style = MaterialTheme.typography.labelMedium)
        AppText(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

