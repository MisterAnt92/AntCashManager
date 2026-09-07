package com.antcashmanager.android.ui.screen.transactions.addImport.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.text.AppText

/**
 * Composable per la sezione "Ricorrenza" nella form di aggiunta/modifica transazione.
 *
 * Responsabilità:
 * - Toggle ricorrenza (checkbox)
 * - Selezione intervallo di ricorrenza (dropdown)
 * - Visualizzazione condizionale dell'intervallo
 *
 * Visibilità:
 * - Sempre visibile
 * - Dettagli intervallo mostrati solo se isRecurring == true
 *
 * Note:
 * - La validazione richiede che recurrenceInterval sia non-vuoto se isRecurring
 * - Il dropdown è visibile solo quando isRecurring è true
 */
@Composable
internal fun DetailsRecurrenceSection(
    isRecurring: Boolean,
    recurrenceInterval: String,
    onRecurringChanged: (Boolean) -> Unit,
    onIntervalChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // ── Toggle Ricorrenza ──
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isRecurring) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ).clickable { onRecurringChanged(!isRecurring) }
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
            if (isRecurring) {
                AppText(
                    text = stringResource(R.string.recurrence_interval_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Checkbox(
            checked = isRecurring,
            onCheckedChange = onRecurringChanged,
        )
    }

    // ── Dropdown Intervallo (mostrare solo se ricorrente) ──
    if (isRecurring) {
        VerticalSpacer(SpacingSize.SM)
        RecurrenceIntervalDropdown(
            selectedInterval = recurrenceInterval,
            onIntervalChange = onIntervalChanged,
        )
    }
}
