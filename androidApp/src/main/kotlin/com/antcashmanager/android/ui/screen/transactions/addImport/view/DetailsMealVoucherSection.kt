package com.antcashmanager.android.ui.screen.transactions.addImport.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.layout.HorizontalSpacer
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.transactions.addImport.validator.TransactionValidator

/**
 * Composable per la sezione "Buoni Pasto" nella form di aggiunta/modifica transazione.
 *
 * Responsabilità:
 * - Mostrare il valore unitario dei buoni pasto
 * - Permettere l'inserimento del numero di voucher
 * - Mostrare l'importo totale calcolato (read-only)
 * - Mostrare il subtotale dei voucher e la differenza pagata
 *
 * Visibilità:
 * - Mostrato solo quando PaymentType == MEAL_VOUCHERS
 *
 * Note:
 * - L'importo totale è calcolato dal ViewModel
 * - Il numero di voucher è limitato a 3 cifre (max 999)
 * - Il campo amount è read-only e mostra l'importo totale pagato
 */
@Composable
internal fun DetailsMealVoucherSection(
    mealVoucherCount: String,
    mealVoucherValue: Double,
    totalAmount: String,
    onMealVoucherCountChanged: (String) -> Unit,
    isMealVouchersPayment: Boolean,
    modifier: Modifier = Modifier,
) {
    // Mostra solo se MEAL_VOUCHERS è il payment type
    if (!isMealVouchersPayment) {
        return
    }

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            // Titolo sezione
            AppText(
                text = stringResource(R.string.add_transaction_meal_voucher_section_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            VerticalSpacer(SpacingSize.XS)

            // Valore unitario del voucher
            AppText(
                text = stringResource(
                    R.string.add_transaction_meal_voucher_unit_value,
                    String.format("%.2f", mealVoucherValue)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            VerticalSpacer(SpacingSize.SM)

            // Campo numero voucher
            OutlinedTextField(
                value = mealVoucherCount,
                onValueChange = { newValue ->
                    val normalized = TransactionValidator.normalizeMealVoucherCount(newValue)
                    if (normalized.length <= 3) { // Max 999 buoni
                        onMealVoucherCountChanged(normalized)
                    }
                },
                label = {
                    AppText(stringResource(R.string.add_transaction_meal_voucher_count))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            )

            // Campo importo totale (read-only)
            if (totalAmount.isNotBlank()) {
                VerticalSpacer(SpacingSize.XS)
                OutlinedTextField(
                    value = totalAmount,
                    onValueChange = {}, // Read-only
                    label = {
                        AppText(stringResource(R.string.add_transaction_amount_total_meal_vouchers))
                    },
                    placeholder = { AppText("0.00") },
                    enabled = false,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.7f), // Rendi più trasparente per indicare disabilitato
                    shape = RoundedCornerShape(16.dp),
                )
            }

            // Subtotale voucher
            val voucherCount = mealVoucherCount.toIntOrNull() ?: 0
            val voucherTotal = voucherCount * mealVoucherValue

            if (voucherCount > 0) {
                VerticalSpacer(SpacingSize.XS)
                AppText(
                    text = stringResource(
                        R.string.add_transaction_meal_voucher_subtotal,
                        String.format("%.2f", voucherTotal)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )

                // Differenza pagata
                val totalAmountDouble = totalAmount.toDoubleOrNull() ?: 0.0
                val differencePaid = totalAmountDouble - voucherTotal
                if (differencePaid > 0) {
                    VerticalSpacer(SpacingSize.XXXS)
                    AppText(
                        text = stringResource(
                            R.string.add_transaction_meal_voucher_difference_paid
                        ) + ": €" + String.format("%.2f", differencePaid),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
