package com.antcashmanager.android.ui.screen.transactions.addImport.view

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.selection.AppSelectionItemCard
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Composable per i campi di selezione principali nella form di aggiunta/modifica transazione.
 *
 * Campi inclusi:
 * - Categoria (AppSelectionItemCard, editabile al tap)
 * - Tipo Transazione (AppSelectionItemCard, editabile al tap)
 * - Data (AppSelectionItemCard, editabile al tap)
 * - Tipo di Pagamento (AppSelectionItemCard, editabile al tap)
 *
 * Responsabilità:
 * - Renderizzare i campi di selezione principali
 * - Formattare i valori per la visualizzazione
 * - Gestire i callback di modifica
 *
 * Visibilità:
 * - Sempre visibili
 * - Ordine: Categoria → Tipo → Data → Payment Type
 *
 * Note:
 * - I campi sono editabili solo al tap (dialog)
 * - La categoria mostra l'icona (emoji)
 * - La data è formattata come dd/MM/yyyy
 * - Il payment type mostra il nome localizzato
 */
@Composable
internal fun DetailsCategoryTypeSection(
    selectedCategory: Category?,
    selectedType: TransactionType?,
    selectedPaymentType: PaymentType,
    timestamp: Long,
    onEditCategory: () -> Unit,
    onEditType: () -> Unit,
    onEditDate: () -> Unit,
    onEditPaymentType: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // ── Categoria – sempre editabile al tap ──
    AppSelectionItemCard(
        label = stringResource(R.string.add_transaction_category),
        value = selectedCategory?.name
            ?: stringResource(R.string.add_transaction_none),
        icon = selectedCategory?.icon,
        isEditable = true,
        onClick = onEditCategory,
    )
    Spacer(modifier = Modifier.height(12.dp))

    // ── Tipo Transazione – sempre editabile al tap ──
    AppSelectionItemCard(
        label = stringResource(R.string.add_transaction_type),
        value = when (selectedType) {
            TransactionType.INCOME -> stringResource(R.string.add_transaction_income_label)
            TransactionType.EXPENSE -> stringResource(R.string.add_transaction_expense_label)
            null -> stringResource(R.string.add_transaction_none)
        },
        icon = when (selectedType) {
            TransactionType.INCOME -> "💰"
            TransactionType.EXPENSE -> "💸"
            null -> null
        },
        isEditable = true,
        onClick = onEditType,
    )
    Spacer(modifier = Modifier.height(12.dp))

    // ── Data – sempre editabile al tap ──
    AppSelectionItemCard(
        label = stringResource(R.string.add_transaction_field_date),
        value = SimpleDateFormat(
            "dd/MM/yyyy",
            LocalLocale.current.platformLocale,
        ).format(Date(timestamp)),
        isEditable = true,
        onClick = onEditDate,
    )
    Spacer(modifier = Modifier.height(12.dp))

    // ── Tipo di Pagamento – sempre editabile al tap ──
    AppSelectionItemCard(
        label = stringResource(R.string.add_transaction_payment_type),
        value = when (selectedPaymentType) {
            PaymentType.ELECTRONIC -> stringResource(R.string.add_transaction_payment_type_electronic)
            PaymentType.CASH -> stringResource(R.string.add_transaction_payment_type_cash)
            PaymentType.MEAL_VOUCHERS -> stringResource(R.string.add_transaction_payment_type_meal_vouchers)
        },
        isEditable = true,
        onClick = onEditPaymentType,
    )
    Spacer(modifier = Modifier.height(12.dp))
}
