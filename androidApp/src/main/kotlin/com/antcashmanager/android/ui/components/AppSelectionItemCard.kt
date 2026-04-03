package com.antcashmanager.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

/**
 * Componente per visualizzare categoria/tipo selezionati con icona, label e chevron se modificabile.
 * Label sempre su single line.
 *
 * @param label Label del campo
 * @param value Valore selezionato (su single line)
 * @param icon Icona da mostrare (emoji o simbolo)
 * @param isEditable Se true, mostra il chevron e rende il campo cliccabile
 * @param onClick Callback quando il campo è cliccato
 * @param modifier Modifier personalizzato
 */
@Composable
fun AppSelectionItemCard(
    label: String,
    value: String,
    icon: String? = null,
    isEditable: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .apply {
                if (isEditable && onClick != null) {
                    clickable(onClick = onClick)
                }
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Icon if provided
        if (icon != null) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineSmall,
            )
        }

        // Label and Value Column
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // Chevron if editable
        if (isEditable) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Preview(showBackground = true, name = "AppSelectionItemCard - Non-Editable")
@Composable
private fun AppSelectionItemCardPreviewNonEditable() {
    AntCashManagerTheme {
        AppSelectionItemCard(
            label = "Category",
            value = "Food",
            icon = "🍔",
            isEditable = false,
        )
    }
}

@Preview(showBackground = true, name = "AppSelectionItemCard - Editable")
@Composable
private fun AppSelectionItemCardPreviewEditable() {
    AntCashManagerTheme {
        AppSelectionItemCard(
            label = "Category",
            value = "Food",
            icon = "🍔",
            isEditable = true,
            onClick = {},
        )
    }
}

@Preview(showBackground = true, name = "AppSelectionItemCard - Without Icon")
@Composable
private fun AppSelectionItemCardPreviewNoIcon() {
    AntCashManagerTheme {
        AppSelectionItemCard(
            label = "Type",
            value = "Expense",
            isEditable = true,
            onClick = {},
        )
    }
}

