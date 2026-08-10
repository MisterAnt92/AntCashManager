package com.antcashmanager.android.ui.screen.transactions.addImport.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.layout.HorizontalSpacer
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.input.AutocompleteTextField

/**
 * Composable per i campi opzionali nella form di aggiunta/modifica transazione.
 *
 * Campi inclusi:
 * - Note (AutocompleteTextField)
 * - Beneficiario/Payee (AutocompleteTextField)
 * - Luogo/Location (AutocompleteTextField)
 *
 * Responsabilità:
 * - Renderizzare i campi opzionali (Note, Payee, Location)
 * - Fornire suggerimenti da storici (autocomplete)
 * - Gestire gli aggiornamenti via callback
 *
 * Visibilità:
 * - Sempre visibili
 * - Ordine: Note → Payee → Location
 *
 * Note:
 * - Tutti i campi sono facoltativi (non validati)
 * - Supportano autocomplete da storici
 * - Spacer di 12.dp tra i campi
 * - Tag rimane in DetailsStep per ora (componente privato)
 */
@Composable
internal fun DetailsOptionalFieldsSection(
    notes: String,
    payee: String,
    location: String,
    notesSuggestions: List<String>,
    payeeSuggestions: List<String>,
    locationSuggestions: List<String>,
    onNotesChanged: (String) -> Unit,
    onPayeeChanged: (String) -> Unit,
    onLocationChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // ── Note ──
    AutocompleteTextField(
        value = notes,
        onValueChange = onNotesChanged,
        suggestions = notesSuggestions,
        label = stringResource(R.string.add_transaction_notes_label),
        modifier = Modifier.fillMaxWidth(),
    )
    VerticalSpacer(SpacingSize.SM)

    // ── Beneficiario/Payee ──
    AutocompleteTextField(
        value = payee,
        onValueChange = onPayeeChanged,
        suggestions = payeeSuggestions,
        label = stringResource(R.string.add_transaction_payee_label),
        modifier = Modifier.fillMaxWidth(),
    )
    VerticalSpacer(SpacingSize.SM)

    // ── Luogo/Location ──
    AutocompleteTextField(
        value = location,
        onValueChange = onLocationChanged,
        suggestions = locationSuggestions,
        label = stringResource(R.string.add_transaction_location_label),
        modifier = Modifier.fillMaxWidth(),
    )
}
