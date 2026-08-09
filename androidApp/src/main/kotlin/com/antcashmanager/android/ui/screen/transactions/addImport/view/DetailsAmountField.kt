package com.antcashmanager.android.ui.screen.transactions.addImport.view

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.transactions.addImport.validator.TransactionValidator
import com.antcashmanager.android.util.LocalAmountsMasked
import com.antcashmanager.android.util.isProtectedSalaryCategory
import com.antcashmanager.android.util.maskDigits
import com.antcashmanager.domain.model.TransactionType

/**
 * Composable per il campo "Importo Totale" nella form di aggiunta/modifica transazione.
 *
 * Responsabilità:
 * - Renderizzare il campo di input per l'importo
 * - Normalizzare l'input (virgola → punto, previene multipli decimali)
 * - Applicare mascheratura se necessario (per categorie sensibili come Stipendio)
 *
 * Note:
 * - Per MEAL_VOUCHERS: il campo è nascosto (non viene renderizzato)
 * - La normalizzazione avviene in tempo reale mentre l'utente digita
 * - La mascheratura è applicata visivamente se [LocalAmountsMasked] è true
 * - La validazione del valore finale avviene al submit
 */
@Composable
internal fun DetailsAmountField(
    amount: String,
    onAmountChanged: (String) -> Unit,
    isMealVouchersPayment: Boolean,
    selectedCategoryName: String? = null,
    selectedType: TransactionType? = null,
    modifier: Modifier = Modifier,
) {
    // Se MEAL_VOUCHERS, il campo è nascosto (gestito da DetailsStep)
    if (isMealVouchersPayment) {
        return
    }

    val shouldMaskAmount = LocalAmountsMasked.current &&
            isProtectedSalaryCategory(selectedCategoryName, selectedType)

    OutlinedTextField(
        value = amount,
        onValueChange = { newValue ->
            val normalized = TransactionValidator.normalizeAmount(newValue)
            onAmountChanged(normalized)
        },
        label = {
            AppText(
                stringResource(R.string.add_transaction_amount_required)
            )
        },
        placeholder = { AppText("0.00") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier,
        singleLine = true,
        visualTransformation = if (shouldMaskAmount) {
            MaskDigitsVisualTransformation
        } else {
            VisualTransformation.None
        },
    )
}

/**
 * Maschera visivamente le cifre del campo importo (come un campo password) mantenendo il
 * valore reale digitato/precaricato nello state, per lo Stipendio quando il mascheramento è
 * attivo. [OffsetMapping.Identity] è corretto perché [maskDigits] sostituisce carattere per
 * carattere senza cambiare la lunghezza della stringa.
 */
private object MaskDigitsVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText =
        TransformedText(AnnotatedString(maskDigits(text.text)), OffsetMapping.Identity)
}
