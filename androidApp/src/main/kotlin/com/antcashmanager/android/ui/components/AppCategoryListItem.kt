package com.antcashmanager.android.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.categories.view.categoryIconMap
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.domain.model.Category

/**
 * Elemento categoria in lista orizzontale con icona e nome su singola riga.
 * Migliore per il wizard di selezione categoria nel transaction add.
 *
 * @param category Categoria da mostrare
 * @param isSelected Se la categoria è selezionata
 * @param onClick Callback al click
 * @param modifier Modifier personalizzato
 */
@Composable
fun AppCategoryListItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    subtitle: String? = null,
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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Icona della categoria con sfondo colorato circolare
        val iconVector = categoryIconMap[category.icon]
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(category.color)),
            contentAlignment = Alignment.Center,
        ) {
            if (iconVector != null) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            } else {
                AppText(
                    text = category.name.take(1).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Nome categoria + tipo (Entrata/Uscita) per disambiguare nomi simili
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            AppText(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1,
            )

            if (!subtitle.isNullOrBlank()) {
                AppText(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }

        // Indicatore di selezione
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "AppCategoryListItem - Non-Selected")
@Composable
private fun AppCategoryListItemPreviewNonSelected() {
    AntCashManagerTheme {
        AppCategoryListItem(
            category = Category(
                id = 1,
                name = "Food & Dining",
                icon = "restaurant",
                color = 0xFFFF6B6B,
                type = "EXPENSE"
            ),
            isSelected = false,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "AppCategoryListItem - Selected")
@Composable
private fun AppCategoryListItemPreviewSelected() {
    AntCashManagerTheme {
        AppCategoryListItem(
            category = Category(
                id = 2,
                name = "Salary & Wages",
                icon = "payments",
                color = 0xFF51CF66,
                type = "INCOME"
            ),
            isSelected = true,
            onClick = {}
        )
    }
}

@Preview(
    showBackground = true,
    name = "AppCategoryListItem - Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun AppCategoryListItemPreviewDark() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        AppCategoryListItem(
            category = Category(
                id = 3,
                name = "Transport",
                icon = "directions_car",
                color = 0xFF4DABF7,
                type = "EXPENSE",
            ),
            isSelected = true,
            onClick = {},
            subtitle = "Expense",
        )
    }
}

@Preview(name = "AppCategoryListItem - Accessibility", showBackground = true, fontScale = 1.5f)
@Composable
private fun AppCategoryListItemPreviewAccessibility() {
    AppCategoryListItemPreviewNonSelected()
}
