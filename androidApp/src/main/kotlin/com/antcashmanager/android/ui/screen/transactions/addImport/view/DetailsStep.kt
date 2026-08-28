package com.antcashmanager.android.ui.screen.transactions.addImport.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.layout.HorizontalSpacer
import com.antcashmanager.android.R
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.ui.components.button.AppButton
import com.antcashmanager.android.ui.components.input.AutocompleteTextField
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.transactions.addImport.AddTransactionState
import com.antcashmanager.android.ui.screen.transactions.addImport.event.AddTransactionEvent
import com.antcashmanager.domain.model.TransactionType
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun DetailsStep(
    state: AddTransactionState,
    onEvent: (AddTransactionEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val analyticsManager: AnalyticsManager = koinInject()
    val setRecurring: (Boolean) -> Unit = { recurring ->
        analyticsManager.logEvent("transaction_recurring_toggled")
        onEvent(AddTransactionEvent.SetRecurring(recurring))
    }
    val isMealVouchersPayment = state.isMealVouchersPayment

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
                    AppText(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(AddTransactionEvent.DismissDatePicker) }) {
                    AppText(stringResource(R.string.common_cancel))
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
                    text = stringResource(R.string.add_transaction_delete_confirm_msg),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        analyticsManager.logEvent("transaction_deleted")
                        onEvent(AddTransactionEvent.ConfirmDelete)
                    }
                ) {
                    AppText(
                        stringResource(R.string.dialog_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(AddTransactionEvent.DismissDeleteConfirmDialog) }) {
                    AppText(stringResource(R.string.common_cancel))
                }
            },
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                            stringResource(R.string.common_back),
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
                .padding(
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                    top = innerPadding.calculateTopPadding() + 12.dp,
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    bottom = innerPadding.calculateBottomPadding(),
                )
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Categoria, Tipo, Data, Payment Type ──
            DetailsCategoryTypeSection(
                selectedCategory = state.selectedCategory,
                selectedType = state.selectedType,
                selectedPaymentType = state.selectedPaymentType,
                timestamp = state.timestamp,
                onEditCategory = { onEvent(AddTransactionEvent.EditCategory) },
                onEditType = { onEvent(AddTransactionEvent.EditType) },
                onEditDate = { onEvent(AddTransactionEvent.EditDate) },
                onEditPaymentType = { onEvent(AddTransactionEvent.EditPaymentType) },
            )

            // ── Titolo ──
            val analyticsManager: AnalyticsManager = koinInject()
            AutocompleteTextField(
                value = state.title,
                onValueChange = { onEvent(AddTransactionEvent.UpdateTitle(it)) },
                suggestions = state.titleSuggestions,
                label = stringResource(R.string.add_transaction_title_required),
                modifier = Modifier.fillMaxWidth(),
                onSuggestionSelected = { suggestion ->
                    // Track transaction duplicate suggestion accepted
                    analyticsManager.logEvent("transaction_duplicate_suggestion_accepted", android.os.Bundle().apply {
                        putString("suggestion_type", "title")
                        putInt("suggestion_length", suggestion.length)
                    })
                }
            )
            VerticalSpacer(SpacingSize.SM)

            // ── Importo ──
            DetailsAmountField(
                amount = state.amount,
                onAmountChanged = { onEvent(AddTransactionEvent.UpdateAmount(it)) },
                isMealVouchersPayment = state.isMealVouchersPayment,
                selectedCategoryName = state.selectedCategory?.name,
                selectedType = state.selectedType,
                modifier = Modifier.fillMaxWidth(),
            )
            if (!state.isMealVouchersPayment) {
                VerticalSpacer(SpacingSize.SM)
            }

            // ── Note, Payee, Location ──
            DetailsOptionalFieldsSection(
                notes = state.notes,
                payee = state.payee,
                location = state.location,
                notesSuggestions = state.notesSuggestions,
                payeeSuggestions = state.payeeSuggestions,
                locationSuggestions = state.locationSuggestions,
                onNotesChanged = { onEvent(AddTransactionEvent.UpdateNotes(it)) },
                onPayeeChanged = { onEvent(AddTransactionEvent.UpdatePayee(it)) },
                onLocationChanged = { onEvent(AddTransactionEvent.UpdateLocation(it)) },
            )
            VerticalSpacer(SpacingSize.SM)

            // ── Buoni Pasto ──
            DetailsMealVoucherSection(
                mealVoucherCount = state.mealVoucherCount,
                mealVoucherValue = state.mealVoucherValue,
                mealVoucherDifference = state.mealVoucherDifference,
                totalAmount = String.format("%.2f", state.totalAmount),
                onMealVoucherCountChanged = { onEvent(AddTransactionEvent.UpdateMealVoucherCount(it)) },
                onMealVoucherDifferenceChanged = { onEvent(AddTransactionEvent.UpdateMealVoucherDifference(it)) },
                isMealVouchersPayment = isMealVouchersPayment,
                isExpenseType = state.selectedType == TransactionType.EXPENSE,
            )
            if (isMealVouchersPayment) {
                VerticalSpacer(SpacingSize.SM)
            }

            // ── Tags – Improved management with chips ──
            DetailsTagsSection(
                tags = state.tags,
                onTagsChange = { onEvent(AddTransactionEvent.UpdateTags(it)) },
                suggestions = state.tagsSuggestions
            )
            VerticalSpacer(SpacingSize.SM)

            // ── Ricorrenza ──
            DetailsRecurrenceSection(
                isRecurring = state.isRecurring,
                recurrenceInterval = state.recurrenceInterval,
                onRecurringChanged = setRecurring,
                onIntervalChanged = { onEvent(AddTransactionEvent.UpdateRecurrenceInterval(it)) },
            )

            // ── Errore ──
            if (state.error != null) {
                VerticalSpacer(SpacingSize.XS)
                AppText(
                    text = state.error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            VerticalSpacer(SpacingSize.XL)

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
            VerticalSpacer(SpacingSize.MD)
        }
    }
}

