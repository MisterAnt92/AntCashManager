package com.antcashmanager.android.ui.screen.categories.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.domain.model.Category

@Composable
internal fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.tertiary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.icon,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                Column {
                    AppText(
                        text = category.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    AppText(
                        text = stringResource(
                            if (category.type == "EXPENSE")
                                R.string.categories_tab_expense
                            else
                                R.string.categories_tab_income
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (category.type != "DEFAULT") {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.dialog_delete),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun HelpDialog(onDismiss: () -> Unit) {
    val helpFeatures = listOf(
        com.antcashmanager.android.ui.components.SimpleHelpFeature(
            title = "Gestione Categorie",
            description = "Crea e personalizza categorie per organizzare le tue transazioni",
            icon = Icons.Default.Add,
        ),
        com.antcashmanager.android.ui.components.SimpleHelpFeature(
            title = "Entrate e Uscite",
            description = "Categorizza transazioni come entrate o uscite",
            icon = Icons.Default.List,
        ),
        com.antcashmanager.android.ui.components.SimpleHelpFeature(
            title = "Elimina Categorie",
            description = "Rimuovi categorie personalizzate quando non più necessarie",
            icon = Icons.Default.Delete,
        ),
    )

    com.antcashmanager.android.ui.components.HelpDialogContent(
        isVisible = true,
        title = "Guida Categorie",
        description = "Gestisci le tue categorie di spesa e entrata!",
        features = helpFeatures,
        onDismiss = onDismiss,
    )
}

