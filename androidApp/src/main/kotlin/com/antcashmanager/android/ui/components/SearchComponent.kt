package com.antcashmanager.android.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchComponent(
    isVisible: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchSuggestions: List<String>,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.transactions_search_label),
    placeholder: String = stringResource(R.string.transactions_search_placeholder),
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Column {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { AppText(label) },
                placeholder = { AppText(placeholder) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.common_clear),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
            )

            // Search Suggestions
            if (searchSuggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    searchSuggestions.forEach { suggestion ->
                        SuggestionChip(
                            onClick = { onSearchQueryChange(suggestion) },
                            label = {
                                AppText(
                                    text = suggestion,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "SearchComponent - Light", showBackground = true)
@Composable
private fun SearchComponentPreviewLight() {
    AntCashManagerTheme(dynamicColor = false) {
        SearchComponent(
            isVisible = true,
            searchQuery = "gro",
            onSearchQueryChange = {},
            searchSuggestions = listOf("Groceries", "Grocery Market", "Group Dinner"),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(
    name = "SearchComponent - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SearchComponentPreviewDark() {
    SearchComponentPreviewLight()
}

@Preview(name = "SearchComponent - Accessibility", showBackground = true, fontScale = 1.5f)
@Composable
private fun SearchComponentPreviewAccessibility() {
    SearchComponentPreviewLight()
}
