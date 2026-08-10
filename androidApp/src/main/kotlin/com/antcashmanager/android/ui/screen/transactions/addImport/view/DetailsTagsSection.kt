package com.antcashmanager.android.ui.screen.transactions.addImport.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.text.AppText

/**
 * Composable per la gestione dei tag nelle transazioni.
 *
 * Responsabilità:
 * - Renderizzare il campo di input per i tag
 * - Gestire l'aggiunta/rimozione di tag
 * - Mostrare i tag correnti
 * - Suggerire tag basati sulla ricerca
 *
 * Note:
 * - I tag sono gestiti come stringa comma-separated
 * - L'input filtrato per suggerimenti case-insensitive
 * - Limite di 5 suggerimenti mostrati
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DetailsTagsSection(
    tags: String,
    onTagsChange: (String) -> Unit,
    suggestions: List<String>,
    modifier: Modifier = Modifier,
) {
    var tagInput by remember { mutableStateOf("") }
    val currentTags = remember(tags) {
        tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        AppText(
            text = stringResource(R.string.add_transaction_tags_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Tag Input
        OutlinedTextField(
            value = tagInput,
            onValueChange = { tagInput = it },
            label = { AppText(stringResource(R.string.add_transaction_tags_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            trailingIcon = {
                if (tagInput.isNotBlank()) {
                    IconButton(onClick = {
                        if (!currentTags.contains(tagInput.trim())) {
                            val newTags = (currentTags + tagInput.trim()).joinToString(", ")
                            onTagsChange(newTags)
                        }
                        tagInput = ""
                    }) {
                        androidx.compose.material3.Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Tag"
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true
        )

        if (currentTags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                currentTags.forEach { tag ->
                    InputChip(
                        selected = true,
                        onClick = { },
                        label = { AppText(tag) },
                        trailingIcon = {
                            androidx.compose.material3.Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        val newTags = currentTags
                                            .filter { it != tag }
                                            .joinToString(", ")
                                        onTagsChange(newTags)
                                    }
                            )
                        },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = null
                    )
                }
            }
        }

        // Suggestions
        val filteredSuggestions = suggestions.filter {
            it.contains(tagInput, ignoreCase = true) && !currentTags.contains(it)
        }.take(5)

        if (tagInput.isNotBlank() && filteredSuggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            AppText(
                text = stringResource(R.string.add_transaction_tags_suggestions),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                filteredSuggestions.forEach { suggestion ->
                    AssistChip(
                        onClick = {
                            val newTags = (currentTags + suggestion).joinToString(", ")
                            onTagsChange(newTags)
                            tagInput = ""
                        },
                        label = {
                            AppText(
                                suggestion,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}
