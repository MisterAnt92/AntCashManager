package com.antcashmanager.android.ui.screen.transaction_add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
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
import com.antcashmanager.android.ui.components.AppCategoryCard
import com.antcashmanager.android.ui.components.AppSelectionItemCard
import com.antcashmanager.android.ui.components.button.AppButton
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.domain.model.Category
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
fun AddTransactionScreen(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    transactionId: Long? = null, // nuovo parametro opzionale
    onNavigateBack: () -> Unit,
    onTransactionAdded: () -> Unit,
) {
    Logger.d("AddTransactionScreen") { "Displaying AddTransactionScreen" }

    val viewModel: AddTransactionViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AddTransactionViewModel(
                    transactionRepository,
                    categoryRepository,
                    transactionId
                ) as T
        },
    )

    val state by viewModel.state.collectAsState()

    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
    var hasSubmittedOnce by remember { mutableStateOf(false) }

    // Naviga indietro solo se: transazione è stata sottomessa E stato è stato resettato
    if (hasSubmittedOnce && state.selectedCategory == null && state.selectedType == null &&
        state.title.isEmpty() && state.currentStep == AddTransactionStep.CATEGORY_SELECTION &&
        state.isLoading == false
    ) {
        onTransactionAdded()
    }

    AddTransactionContent(
        state = state,
        onEvent = { event ->
            if (event is AddTransactionEvent.Submit) {
                hasSubmittedOnce = true
            }
            viewModel.onEvent(event)
        },
        onNavigateBack = onNavigateBack,
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// CONTENT
// ══════════════════════════════════════════════════════════════════════════════

@Composable
@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
internal fun AddTransactionContent(
    state: AddTransactionState,
    onEvent: (AddTransactionEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { _ ->
        // Content is provided by individual step Scaffolds which manage their own padding
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
                title = {
                    AppText(
                        stringResource(R.string.add_transaction_select_category),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            stringResource(R.string.add_transaction_back)
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            AppText(
                stringResource(R.string.add_transaction_choose_category),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 24.dp),
            )

            if (categories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    AppText(
                        stringResource(R.string.add_transaction_no_categories_available),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    items(categories) { category ->
                        AppCategoryCard(
                            category = category,
                            isSelected = selectedCategory?.id == category.id,
                            onClick = { onSelectCategory(category) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                title = {
                    AppText(
                        stringResource(R.string.add_transaction_select_type),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            stringResource(R.string.add_transaction_back)
                        )
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
                stringResource(
                    R.string.add_transaction_selected_category,
                    selectedCategory?.name ?: stringResource(R.string.add_transaction_none)
                ),
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
                title = {
                    AppText(
                        stringResource(R.string.add_transaction_details),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            stringResource(R.string.add_transaction_back)
                        )
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
                stringResource(
                    R.string.add_transaction_category_type,
                    state.selectedCategory?.name ?: stringResource(R.string.add_transaction_none),
                    if (state.selectedType == TransactionType.INCOME) stringResource(R.string.add_transaction_income_label) else stringResource(
                        R.string.add_transaction_expense_label
                    )
                ),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            // Category Selection Field - sempre visibile
            AppSelectionItemCard(
                label = stringResource(R.string.add_transaction_category),
                value = state.selectedCategory?.name ?: "-",
                icon = state.selectedCategory?.icon,
                isEditable = state.isModifying,
                onClick = if (state.isModifying) {
                    { onEvent(AddTransactionEvent.EditCategory) }
                } else null,
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Type Selection Field - sempre visibile
            AppSelectionItemCard(
                label = stringResource(R.string.add_transaction_type),
                value = if (state.selectedType == TransactionType.INCOME)
                    stringResource(R.string.add_transaction_income_label)
                else
                    stringResource(R.string.add_transaction_expense_label),
                icon = if (state.selectedType == TransactionType.INCOME) "💰" else "💸",
                isEditable = state.isModifying,
                onClick = if (state.isModifying) {
                    { onEvent(AddTransactionEvent.EditType) }
                } else null,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.title,
                onValueChange = { onEvent(AddTransactionEvent.UpdateTitle(it)) },
                label = { Text(stringResource(R.string.add_transaction_title_required)) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Amount
            OutlinedTextField(
                value = state.amount,
                onValueChange = { onEvent(AddTransactionEvent.UpdateAmount(it)) },
                label = { Text(stringResource(R.string.add_transaction_amount_required)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Date
            AppSelectionItemCard(
                label = stringResource(R.string.add_transaction_field_date),
                value = SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
                ).format(Date(state.timestamp)),
                isEditable = true,
                onClick = { showDatePicker = true },
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Notes
            OutlinedTextField(
                value = state.notes,
                onValueChange = { onEvent(AddTransactionEvent.UpdateNotes(it)) },
                label = { Text(stringResource(R.string.add_transaction_notes_label)) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Payee
            OutlinedTextField(
                value = state.payee,
                onValueChange = { onEvent(AddTransactionEvent.UpdatePayee(it)) },
                label = { Text(stringResource(R.string.add_transaction_payee_label)) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Location
            OutlinedTextField(
                value = state.location,
                onValueChange = { onEvent(AddTransactionEvent.UpdateLocation(it)) },
                label = { Text(stringResource(R.string.add_transaction_location_label)) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Tags
            OutlinedTextField(
                value = state.tags,
                onValueChange = { onEvent(AddTransactionEvent.UpdateTags(it)) },
                label = { Text(stringResource(R.string.add_transaction_tags_label)) },
                placeholder = { Text(stringResource(R.string.add_transaction_tags_placeholder)) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Recurring
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppText(
                    stringResource(R.string.add_transaction_recurring_label),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Checkbox(
                    checked = state.isRecurring,
                    onCheckedChange = { onEvent(AddTransactionEvent.SetRecurring(it)) },
                )
            }

            if (state.isRecurring) {
                Spacer(modifier = Modifier.height(12.dp))
                RecurrenceIntervalDropdown(
                    selectedInterval = state.recurrenceInterval,
                    onIntervalChange = { onEvent(AddTransactionEvent.UpdateRecurrenceInterval(it)) },
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (state.isModifying) {
                    AppButton(
                        text = stringResource(R.string.add_transaction_summary),
                        modifier = Modifier.weight(1f),
                        onClick = onPrevious,
                    )
                    AppButton(
                        text = stringResource(R.string.add_transaction_save),
                        modifier = Modifier.weight(1f),
                        enabled = state.title.isNotBlank() && state.amount.isNotBlank(),
                        onClick = onNext,
                    )
                } else {
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
                title = {
                    AppText(
                        stringResource(R.string.add_transaction_confirmation),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            stringResource(R.string.add_transaction_back)
                        )
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

            ConfirmationField(
                stringResource(R.string.add_transaction_field_category),
                state.selectedCategory?.name ?: "-",
                icon = state.selectedCategory?.icon
            )
            ConfirmationField(
                stringResource(R.string.add_transaction_field_type),
                if (state.selectedType == TransactionType.INCOME) stringResource(R.string.add_transaction_income_label) else stringResource(
                    R.string.add_transaction_expense_label
                ),
                icon = if (state.selectedType == TransactionType.INCOME) "💰" else "💸"
            )
            ConfirmationField(stringResource(R.string.add_transaction_field_title), state.title)
            ConfirmationField(stringResource(R.string.add_transaction_field_amount), state.amount)
            ConfirmationField(
                stringResource(R.string.add_transaction_field_date),
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(state.timestamp))
            )
            if (state.notes.isNotEmpty()) {
                ConfirmationField(stringResource(R.string.add_transaction_field_notes), state.notes)
            }
            if (state.payee.isNotEmpty()) {
                ConfirmationField(stringResource(R.string.add_transaction_field_payee), state.payee)
            }
            if (state.location.isNotEmpty()) {
                ConfirmationField(
                    stringResource(R.string.add_transaction_field_location),
                    state.location
                )
            }
            if (state.tags.isNotEmpty()) {
                val formattedTags = state.tags.split(",").joinToString(" ") { "#${it.trim()}" }
                ConfirmationField(
                    stringResource(R.string.add_transaction_field_tags),
                    formattedTags
                )
            }
            if (state.isRecurring) {
                ConfirmationField(
                    stringResource(R.string.add_transaction_field_recurrence),
                    state.recurrenceInterval.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (state.isModifying) {
                    AppButton(
                        text = stringResource(R.string.add_transaction_save),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onSubmit,
                    )
                } else {
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
}

@Composable
private fun ConfirmationField(label: String, value: String, icon: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppText(label, style = MaterialTheme.typography.labelMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                AppText(icon, style = MaterialTheme.typography.titleLarge)
            }
            AppText(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// RECURRENCE INTERVAL DROPDOWN
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun RecurrenceIntervalDropdown(
    selectedInterval: String,
    onIntervalChange: (String) -> Unit,
) {
    val intervals = listOf("daily", "weekly", "monthly", "yearly")
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { expanded = !expanded }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        AppText(
            text = selectedInterval.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(),
        )

        if (expanded) {
            androidx.compose.material3.DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f),
            ) {
                intervals.forEach { interval ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = {
                            AppText(
                                text = interval.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        onClick = {
                            onIntervalChange(interval)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// PREVIEWS
// ══════════════════════════════════════════════════════════════════════════════

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun CategoryCardPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Non-selected category
            AppCategoryCard(
                category = Category(
                    id = 1,
                    name = "Food",
                    icon = "🍔",
                    color = 0xFFFF6B6B,
                    type = "EXPENSE"
                ),
                isSelected = false,
                onClick = {}
            )

            // Selected category
            AppCategoryCard(
                category = Category(
                    id = 2,
                    name = "Salary",
                    icon = "💰",
                    color = 0xFF51CF66,
                    type = "INCOME"
                ),
                isSelected = true,
                onClick = {}
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun CategorySelectionStepPreview() {
    MaterialTheme {
        CategorySelectionStep(
            categories = listOf(
                Category(1, "Food", "🍔", 0xFFFF6B6B, "EXPENSE"),
                Category(2, "Transport", "🚗", 0xFFFFA500, "EXPENSE"),
                Category(3, "Entertainment", "🎮", 0xFF9C27B0, "EXPENSE"),
                Category(4, "Salary", "💰", 0xFF51CF66, "INCOME"),
                Category(5, "Gifts", "🎁", 0xFFE91E63, "INCOME"),
                Category(6, "Business", "💼", 0xFF2196F3, "INCOME"),
            ),
            selectedCategory = Category(2, "Transport", "🚗", 0xFFFFA500, "EXPENSE"),
            onSelectCategory = {},
            onNext = {},
            onCancel = {}
        )
    }
}
