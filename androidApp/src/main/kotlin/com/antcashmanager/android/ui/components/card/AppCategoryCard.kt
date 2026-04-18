package com.antcashmanager.android.ui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import com.antcashmanager.android.ui.screen.categories.categoryIconMap
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
        // Icona della categoria
        val iconVector = categoryIconMap[category.icon]
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(category.color))
                .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (iconVector != null) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            } else {
                AppText(
                    text = category.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nome della categoria
        AppText(
            text = category.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp),
            maxLines = 1,
        )

        // Indicatore di selezione
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
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
                icon = "restaurant",
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
                icon = "payments",
                color = 0xFF51CF66,
                type = "INCOME"
            ),
            isSelected = true,
            onClick = {}
        )
    }
}
