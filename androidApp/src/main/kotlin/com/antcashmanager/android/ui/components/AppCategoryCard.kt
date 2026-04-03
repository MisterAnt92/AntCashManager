package com.antcashmanager.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.domain.model.Category

/**
 * Categoria selezionabile per il wizard di aggiunta transazione.
 * Mostra icona grande, nome categoria (2 righe max), e check se selezionata.
 *
 * @param category Categoria da mostrare
 * @param isSelected Se la categoria è selezionata
 * @param onClick Callback al click
 * @param modifier Modifier personalizzato
 */
@Composable
fun AppCategoryCard(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Icona della categoria - più grande e prominente
        Text(
            text = category.icon,
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier
                .size(56.dp)
                .padding(4.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Nome della categoria - testo con migliore leggibilità
        AppText(
            text = category.name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp),
            maxLines = 2,
        )

        // Indicatore di selezione
        if (isSelected) {
            Spacer(modifier = Modifier.height(8.dp))
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview(showBackground = true, name = "AppCategoryCard - Non-Selected")
@Composable
private fun AppCategoryCardPreviewNonSelected() {
    AntCashManagerTheme {
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
    }
}

@Preview(showBackground = true, name = "AppCategoryCard - Selected")
@Composable
private fun AppCategoryCardPreviewSelected() {
    AntCashManagerTheme {
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

