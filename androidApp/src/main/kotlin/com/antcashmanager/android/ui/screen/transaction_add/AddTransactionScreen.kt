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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import com.antcashmanager.android.ui.components.AppCategoryListItem
import com.antcashmanager.android.ui.components.AppSelectionItemCard
import com.antcashmanager.android.ui.components.button.AppButton
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
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
    transactionId: Long? = null,
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
                    transactionId,
                ) as T
        },
    )

    val state by viewModel.state.collectAsState()

    // Naviga indietro quando la transazione è stata salvata con successo
    if (state.isTransactionSaved) {
        onTransactionAdded()
    }

    AddTransactionContent(
        state = state,
        onEvent = { event -> viewModel.onEvent(event) },
        onNavigateBack = onNavigateBack,
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// CONTENT – flusso semplificato a 2 step
// ══════════════════════════════════════════════════════════════════════════════

@Composable
internal fun AddTransactionContent(
    state: AddTransactionState,
    onEvent: (AddTransactionEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    when {
        state.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        }
        state.currentStep == AddTransactionStep.CATEGORY_SELECTION -> {
            CategorySelectionStep(
                categories = state.categories,
                selectedCategory = state.selectedCategory,
                onSelectCategory = { onEvent(AddTransactionEvent.SelectCategory(it)) },
                onCancel = onNavigateBack,
            )
        }
        state.currentStep == AddTransactionStep.DETAILS -> {
            DetailsStep(
                state = state,
                onEvent = onEvent,
                onNavigateBack = onNavigateBack,
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP 1: CATEGORY SELECTION (solo in creazione)
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySelectionStep(
    categories: List<Category>,
    selectedCategory: Category?,
    onSelectCategory: (Category) -> Unit,
    onCancel: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AppText(
                        stringResource(R.string.add_transaction_select_category),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            stringResource(R.string.add_transaction_back),
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
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(categories) { category ->
                        AppCategoryListItem(
                            category = category,
                            isSelected = selectedCategory?.id == category.id,
                            onClick = { onSelectCategory(category) },
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP 2: DETTAGLI + SALVATAGGIO DIRETTO
// Categoria, Tipo e Data sono sempre modificabili al tap tramite dialog.
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsStep(
    state: AddTransactionState,
    onEvent: (AddTransactionEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    // ── Dialog: Categoria ──
    if (state.showCategoryDialog) {
        CategorySelectionDialog(
            categories = state.categories,
            selectedCategory = state.selectedCategory,
            onSelectCategory = { onEvent(AddTransactionEvent.SelectCategory(it)) },
            onDismiss = { onEvent(AddTransactionEvent.DismissCategoryDialog) },
        )
    }

    // ── Dialog: Tipo ──
    if (state.showTypeDialog) {
        TypeSelectionDialog(
            selectedType = state.selectedType,
            onSelectType = { onEvent(AddTransactionEvent.SelectType(it)) },
            onDismiss = { onEvent(AddTransactionEvent.DismissTypeDialog) },
        )
    }

    // ── Dialog: Data ──
    if (state.showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.timestamp)
        DatePickerDialog(
            onDismissRequest = { onEvent(AddTransactionEvent.DismissDatePicker) },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { date ->
                            onEvent(AddTransactionEvent.UpdateTimestamp(date))
                        }
                        onEvent(AddTransactionEvent.DismissDatePicker)
                    },
                ) {
                    Text(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(AddTransactionEvent.DismissDatePicker) }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ── Dialog: Tipo di Pagamento ──
    if (state.showPaymentTypeDialog) {
        PaymentTypeSelectionDialog(
            selectedPaymentType = state.selectedPaymentType,
            onSelectPaymentType = { onEvent(AddTransactionEvent.SelectPaymentType(it)) },
            onDismiss = { onEvent(AddTransactionEvent.DismissPaymentTypeDialog) },
        )
    }

    // ── Dialog: Conferma eliminazione ──
    if (state.showDeleteConfirmDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { onEvent(AddTransactionEvent.DismissDeleteConfirmDialog) },
            title = {
                AppText(
                    text = stringResource(R.string.dialog_delete),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                AppText(
                    text = "Sei sicuro di voler eliminare questa transazione? Questa azione non può essere annullata.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { onEvent(AddTransactionEvent.ConfirmDelete) }
                ) {
                    Text(
                        stringResource(R.string.dialog_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(AddTransactionEvent.DismissDeleteConfirmDialog) }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AppText(
                        if (state.isModifying)
                            stringResource(R.string.edit_transaction_title)
                        else
                            stringResource(R.string.add_transaction_details),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            stringResource(R.string.add_transaction_back),
                        )
                    }
                },
                actions = {
                    // Pulsante elimina (solo in modalità modifica)
                    if (state.isModifying) {
                        IconButton(
                            onClick = { onEvent(AddTransactionEvent.ShowDeleteConfirmDialog) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.dialog_delete),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
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
            // ── Categoria – sempre editabile al tap ──
            AppSelectionItemCard(
                label = stringResource(R.string.add_transaction_category),
                value = state.selectedCategory?.name
                    ?: stringResource(R.string.add_transaction_none),
                icon = state.selectedCategory?.icon,
                isEditable = true,
                onClick = { onEvent(AddTransactionEvent.EditCategory) },
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ── Tipo – sempre editabile al tap ──
            AppSelectionItemCard(
                label = stringResource(R.string.add_transaction_type),
                value = when (state.selectedType) {
                    TransactionType.INCOME -> stringResource(R.string.add_transaction_income_label)
                    TransactionType.EXPENSE -> stringResource(R.string.add_transaction_expense_label)
                    null -> stringResource(R.string.add_transaction_none)
                },
                icon = when (state.selectedType) {
                    TransactionType.INCOME -> "💰"
                    TransactionType.EXPENSE -> "💸"
                    null -> null
                },
                isEditable = true,
                onClick = { onEvent(AddTransactionEvent.EditType) },
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ── Titolo ──
            OutlinedTextField(
                value = state.title,
                onValueChange = { onEvent(AddTransactionEvent.UpdateTitle(it)) },
                label = { Text(stringResource(R.string.add_transaction_title_required)) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ── Importo ──
            OutlinedTextField(
                value = state.amount,
                onValueChange = { onEvent(AddTransactionEvent.UpdateAmount(it)) },
                label = { Text(stringResource(R.string.add_transaction_amount_required)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ── Data – sempre editabile al tap ──
            AppSelectionItemCard(
                label = stringResource(R.string.add_transaction_field_date),
                value = SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault(),
                ).format(Date(state.timestamp)),
                isEditable = true,
                onClick = { onEvent(AddTransactionEvent.EditDate) },
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ── Note ──
            OutlinedTextField(
                value = state.notes,
                onValueChange = { onEvent(AddTransactionEvent.UpdateNotes(it)) },
                label = { Text(stringResource(R.string.add_transaction_notes_label)) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ── Beneficiario ──
            OutlinedTextField(
                value = state.payee,
                onValueChange = { onEvent(AddTransactionEvent.UpdatePayee(it)) },
                label = { Text(stringResource(R.string.add_transaction_payee_label)) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ── Luogo ──
            OutlinedTextField(
                value = state.location,
                onValueChange = { onEvent(AddTransactionEvent.UpdateLocation(it)) },
                label = { Text(stringResource(R.string.add_transaction_location_label)) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ── Tipo di Pagamento – sempre editabile al tap ──
            AppSelectionItemCard(
                label = stringResource(R.string.add_transaction_payment_type),
                value = when (state.selectedPaymentType) {
                    PaymentType.ELECTRONIC -> stringResource(R.string.add_transaction_payment_type_electronic)
                    PaymentType.CASH -> stringResource(R.string.add_transaction_payment_type_cash)
                    PaymentType.MEAL_VOUCHERS -> stringResource(R.string.add_transaction_payment_type_meal_vouchers)
                },
                isEditable = true,
                onClick = { onEvent(AddTransactionEvent.EditPaymentType) },
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ── Tags ──
            OutlinedTextField(
                value = state.tags,
                onValueChange = { onEvent(AddTransactionEvent.UpdateTags(it)) },
                label = { Text(stringResource(R.string.add_transaction_tags_label)) },
                placeholder = { Text(stringResource(R.string.add_transaction_tags_placeholder)) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ── Ricorrente ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (state.isRecurring)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onEvent(AddTransactionEvent.SetRecurring(!state.isRecurring)) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    AppText(
                        stringResource(R.string.add_transaction_recurring_label),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (state.isRecurring) {
                        AppText(
                            text = stringResource(R.string.recurrence_interval_label),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
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

            // ── Errore ──
            if (state.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AppText(
                    text = state.error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Pulsanti azione ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!state.isModifying) {
                    // Nuova transazione: Indietro + Salva
                    AppButton(
                        text = stringResource(R.string.add_transaction_previous),
                        modifier = Modifier.weight(1f),
                        onClick = { onEvent(AddTransactionEvent.PreviousStep) },
                    )
                }
                AppButton(
                    text = if (state.isModifying)
                        stringResource(R.string.add_transaction_update)
                    else
                        stringResource(R.string.add_transaction_save),
                    modifier = if (state.isModifying) Modifier.fillMaxWidth() else Modifier.weight(1f),
                    enabled = state.isFormValid,
                    onClick = { onEvent(AddTransactionEvent.Submit) },
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// DIALOGS
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun CategorySelectionDialog(
    categories: List<Category>,
    selectedCategory: Category?,
    onSelectCategory: (Category) -> Unit,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            AppText(
                text = stringResource(R.string.add_transaction_select_category),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(categories) { category ->
                    AppCategoryCard(
                        category = category,
                        isSelected = selectedCategory?.id == category.id,
                        onClick = { onSelectCategory(category) },
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.add_transaction_cancel))
            }
        },
    )
}

@Composable
private fun TypeSelectionDialog(
    selectedType: TransactionType?,
    onSelectType: (TransactionType) -> Unit,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            AppText(
                text = stringResource(R.string.add_transaction_select_type),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TransactionType.entries.forEach { type ->
                    TypeRadioButton(
                        type = type,
                        isSelected = selectedType == type,
                        onClick = { onSelectType(type) },
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.add_transaction_cancel))
            }
        },
    )
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

@Composable
private fun PaymentTypeSelectionDialog(
    selectedPaymentType: PaymentType?,
    onSelectPaymentType: (PaymentType) -> Unit,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            AppText(
                text = stringResource(R.string.add_transaction_select_payment_type),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PaymentType.entries.forEach { paymentType ->
                    PaymentTypeRadioButton(
                        paymentType = paymentType,
                        isSelected = selectedPaymentType == paymentType,
                        onClick = { onSelectPaymentType(paymentType) },
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.add_transaction_cancel))
            }
        },
    )
}

@Composable
private fun PaymentTypeRadioButton(
    paymentType: PaymentType,
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
        val paymentTypeLabel = when (paymentType) {
            PaymentType.CASH -> stringResource(R.string.add_transaction_payment_type_cash)
            PaymentType.ELECTRONIC -> stringResource(R.string.add_transaction_payment_type_electronic)
            PaymentType.MEAL_VOUCHERS -> stringResource(R.string.add_transaction_payment_type_meal_vouchers)
        }
        AppText(
            text = paymentTypeLabel,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
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

    // Resolve localized strings upfront
    val dailyLabel = stringResource(R.string.recurrence_interval_daily)
    val weeklyLabel = stringResource(R.string.recurrence_interval_weekly)
    val monthlyLabel = stringResource(R.string.recurrence_interval_monthly)
    val yearlyLabel = stringResource(R.string.recurrence_interval_yearly)

    // Map interval to localized string
    val intervalDisplayName: (String) -> String = { interval ->
        when (interval) {
            "daily" -> dailyLabel
            "weekly" -> weeklyLabel
            "monthly" -> monthlyLabel
            "yearly" -> yearlyLabel
            else -> interval.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Label per il dropdown
        AppText(
            text = stringResource(R.string.recurrence_interval_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Dropdown
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppText(
                    text = if (selectedInterval.isNotBlank()) {
                        intervalDisplayName(selectedInterval)
                    } else {
                        dailyLabel
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                // Icona dropdown
                Icon(
                    imageVector = if (expanded) {
                        Icons.Filled.KeyboardArrowUp
                    } else {
                        Icons.Filled.KeyboardArrowDown
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

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
                                    text = intervalDisplayName(interval),
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Non-selected category
            AppCategoryCard(
                category = Category(
                    id = 1,
                    name = "Food",
                    icon = "🍔",
                    color = 0xFFFF6B6B,
                    type = "EXPENSE",
                ),
                isSelected = false,
                onClick = {},
            )
            // Selected category
            AppCategoryCard(
                category = Category(
                    id = 2,
                    name = "Salary",
                    icon = "💰",
                    color = 0xFF51CF66,
                    type = "INCOME",
                ),
                isSelected = true,
                onClick = {},
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
                Category(1, "Food & Dining", "🍔", 0xFFFF6B6B, "EXPENSE"),
                Category(2, "Transportation", "🚗", 0xFFFFA500, "EXPENSE"),
                Category(3, "Entertainment", "🎮", 0xFF9C27B0, "EXPENSE"),
                Category(4, "Salary & Wages", "💰", 0xFF51CF66, "INCOME"),
                Category(5, "Gifts & Bonuses", "🎁", 0xFFE91E63, "INCOME"),
                Category(6, "Business Income", "💼", 0xFF2196F3, "INCOME"),
            ),
            selectedCategory = Category(2, "Transportation", "🚗", 0xFFFFA500, "EXPENSE"),
            onSelectCategory = {},
            onCancel = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun DetailsStepNewPreview() {
    MaterialTheme {
        AddTransactionContent(
            state = AddTransactionState(
                currentStep = AddTransactionStep.DETAILS,
                selectedCategory = Category(1, "Food", "🍔", 0xFFFF6B6B, "EXPENSE"),
                selectedType = TransactionType.EXPENSE,
                title = "Pizza",
                amount = "12.50",
            ),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun DetailsStepEditPreview() {
    MaterialTheme {
        AddTransactionContent(
            state = AddTransactionState(
                currentStep = AddTransactionStep.DETAILS,
                isModifying = true,
                selectedCategory = Category(2, "Salary", "💰", 0xFF51CF66, "INCOME"),
                selectedType = TransactionType.INCOME,
                title = "Stipendio Marzo",
                amount = "1500.00",
                notes = "Stipendio mensile",
            ),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}
