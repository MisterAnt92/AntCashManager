package com.antcashmanager.android.ui.components.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties

/**
 * TextField con autocomplete che mostra suggerimenti basati sul testo inserito.
 *
 * @param value Valore corrente del campo
 * @param onValueChange Callback quando il valore cambia
 * @param suggestions Lista di suggerimenti disponibili
 * @param label Label del campo
 * @param modifier Modifier del componente
 */
@Composable
fun AutocompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>,
    label: String,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var hasFocus by remember { mutableStateOf(false) }

    // Filtra i suggerimenti in base al testo inserito
    val filteredSuggestions = remember(value, suggestions) {
        if (value.isBlank()) {
            suggestions.take(5) // Mostra i primi 5 quando vuoto
        } else {
            suggestions.filter {
                it.contains(value, ignoreCase = true)
            }.take(5)
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = filteredSuggestions.isNotEmpty()
            },
            label = { Text(label) },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    hasFocus = focusState.isFocused
                    expanded = hasFocus && filteredSuggestions.isNotEmpty()
                },
        )

        // Dropdown con suggerimenti
        AnimatedVisibility(visible = expanded && hasFocus) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
                    .padding(vertical = 4.dp)
            ) {
                items(filteredSuggestions) { suggestion ->
                    Text(
                        text = suggestion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onValueChange(suggestion)
                                expanded = false
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

