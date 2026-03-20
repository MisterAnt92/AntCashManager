package com.antcashmanager.android.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.touchlab.kermit.Logger
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository

val categoryColors = listOf(
    0xFFE57373L, // Red
    0xFFFF8A65L, // Deep Orange
    0xFFFFB74DL, // Orange
    0xFFFFD54FL, // Amber
    0xFFDCE775L, // Lime
    0xFF81C784L, // Green
    0xFF4DB6ACL, // Teal
    0xFF4FC3F7L, // Light Blue
    0xFF64B5F6L, // Blue
    0xFF7986CBL, // Indigo
    0xFFBA68C8L, // Purple
    0xFFF06292L, // Pink
    0xFF90A4AEL, // Blue Grey
    0xFFA1887FL, // Brown
)

@Composable
fun CategoriesScreen(categoryRepository: CategoryRepository) {
    Logger.d("CategoriesScreen") { "Displaying CategoriesScreen" }
    val viewModel: CategoriesViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                CategoriesViewModel(categoryRepository) as T
        },
    )
    val categories by viewModel.categories.collectAsState()

    CategoriesContent(
        categories = categories,
        onAddCategory = { name, icon, color -> viewModel.addCategory(name, icon, color) },
        onDeleteCategory = { viewModel.deleteCategory(it) },
    )
}

@Composable
internal fun CategoriesContent(
    categories: List<Category>,
    onAddCategory: (String, String, Long) -> Unit = { _, _, _ -> },
    onDeleteCategory: (Category) -> Unit = {},
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.categories_add_content_desc))
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.categories_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (categories.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.categories_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.categories_empty_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories, key = { it.id }) { category ->
                        CategoryItem(
                            category = category,
                            onDelete = { categoryToDelete = category },
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCategoryDialog(
            onConfirm = { name, icon, color ->
                onAddCategory(name, icon, color)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false },
        )
    }

    categoryToDelete?.let { category ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text(stringResource(R.string.categories_delete_title)) },
            text = {
                Text(stringResource(R.string.categories_delete_message, category.name))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCategory(category)
                        categoryToDelete = null
                    },
                ) {
                    Text(stringResource(R.string.dialog_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        )
    }
}

@Composable
private fun CategoryItem(
    category: Category,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(category.color)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = category.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.dialog_delete),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddCategoryDialog(
    onConfirm: (String, String, Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableLongStateOf(categoryColors.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.categories_add)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.categories_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.categories_color_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    categoryColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .then(
                                    if (color == selectedColor) {
                                        Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    } else {
                                        Modifier
                                    },
                                )
                                .clickable { selectedColor = color },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (color == selectedColor) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = stringResource(R.string.categories_selected),
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), "category", selectedColor) },
                enabled = name.isNotBlank(),
            ) {
                Text(stringResource(R.string.dialog_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) }
        },
    )
}

// ── Previews ──

@Preview(showBackground = true, name = "CategoriesScreen - With Data")
@Composable
private fun CategoriesContentPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        CategoriesContent(
            categories = listOf(
                Category(id = 1, name = "Food", icon = "category", color = 0xFFE57373),
                Category(id = 2, name = "Transport", icon = "category", color = 0xFF4FC3F7),
                Category(id = 3, name = "Entertainment", icon = "category", color = 0xFFBA68C8),
                Category(id = 4, name = "Work", icon = "category", color = 0xFF81C784),
            ),
        )
    }
}

@Preview(showBackground = true, name = "CategoriesScreen - Empty")
@Composable
private fun CategoriesContentEmptyPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        CategoriesContent(categories = emptyList())
    }
}

@Preview(showBackground = true, name = "CategoriesScreen - Dark")
@Composable
private fun CategoriesContentDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        CategoriesContent(
            categories = listOf(
                Category(id = 1, name = "Food", icon = "category", color = 0xFFE57373),
                Category(id = 2, name = "Salary", icon = "category", color = 0xFF81C784),
            ),
        )
    }
}

@Preview(showBackground = true, name = "AddCategoryDialog")
@Composable
private fun AddCategoryDialogPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        AddCategoryDialog(
            onConfirm = { _, _, _ -> },
            onDismiss = {},
        )
    }
}

