package com.antcashmanager.android.ui.screen.transaction_add.view

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.AppSelectionItemCard
import com.antcashmanager.android.ui.components.button.AppButton
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.transaction_add.AddTransactionEvent
import com.antcashmanager.android.ui.screen.transaction_add.AddTransactionState
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DetailsStep(
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
        AlertDialog(
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
                    modifier = if (state.isModifying) Modifier.fillMaxWidth() else Modifier.weight(
                        1f
                    ),
                    enabled = state.isFormValid,
                    onClick = { onEvent(AddTransactionEvent.Submit) },
                )
            }
        }
    }
}

