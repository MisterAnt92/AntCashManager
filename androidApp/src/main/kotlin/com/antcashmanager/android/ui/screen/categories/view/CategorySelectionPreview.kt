@file:OptIn(ExperimentalMaterial3Api::class)

package com.antcashmanager.android.ui.screen.categories.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.AppCategoryListItem
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.domain.model.Category

@Preview(name = "Category List Preview", showBackground = true)
@Composable
fun CategoryListPreview() {
    val categories = listOf(
        Category(1, "Food & Dining", "🍔", 0xFFFF6B6B, "EXPENSE"),
        Category(2, "Transportation", "🚗", 0xFFFFA500, "EXPENSE"),
        Category(3, "Entertainment & Fun", "🎮", 0xFF9C27B0, "EXPENSE"),
        Category(4, "Salary & Wages", "💰", 0xFF51CF66, "INCOME"),
        Category(5, "Gifts & Bonuses", "🎁", 0xFFE91E63, "INCOME"),
        Category(6, "Business Income", "💼", 0xFF2196F3, "INCOME")
    )

    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    AntCashManagerTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Select Category - New UI") }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.categories_selection_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                categories.forEach { category ->
                    AppCategoryListItem(
                        category = category,
                        isSelected = selectedCategory?.id == category.id,
                        onClick = {
                            selectedCategory =
                                if (selectedCategory?.id == category.id) null else category
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}
