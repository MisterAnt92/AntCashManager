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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.touchlab.kermit.Logger
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.AntEmptyState
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository

/** Map icon key names to Material Icons. */
val categoryIconMap: Map<String, ImageVector> = mapOf(
    "home" to Icons.Default.Home,
    "directions_car" to Icons.Default.DirectionsCar,
    "restaurant" to Icons.Default.Restaurant,
    "receipt_long" to Icons.Default.ReceiptLong,
    "local_dining" to Icons.Default.LocalDining,
    "theater_comedy" to Icons.Default.TheaterComedy,
    "local_hospital" to Icons.Default.LocalHospital,
    "shopping_bag" to Icons.Default.ShoppingBag,
    "school" to Icons.Default.School,
    "more_horiz" to Icons.Default.MoreHoriz,
    "payments" to Icons.Default.Payments,
    "savings" to Icons.Default.Savings,
    "currency_exchange" to Icons.Default.CurrencyExchange,
    "trending_up" to Icons.Default.TrendingUp,
    "work" to Icons.Default.Work,
)

val categoryColors = listOf(
    0xFFE57373L, 0xFFFF8A65L, 0xFFFFB74DL, 0xFFFFD54FL,
    0xFFDCE775L, 0xFF81C784L, 0xFF4DB6ACL, 0xFF4FC3F7L,
    0xFF64B5F6L, 0xFF7986CBL, 0xFFBA68C8L, 0xFFF06292L,
    0xFF90A4AEL, 0xFFA1887FL,
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
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()

    CategoriesContent(
        expenseCategories = expenseCategories,
        incomeCategories = incomeCategories,
        onAddCategory = { name, icon, color, type -> viewModel.addCategory(name, icon, color, type) },
        onDeleteCategory = { viewModel.deleteCategory(it) },
    )
}

@Composable
internal fun CategoriesContent(
    expenseCategories: List<Category>,
    incomeCategories: List<Category>,
    onAddCategory: (String, String, Long, String) -> Unit = { _, _, _, _ -> },
    onDeleteCategory: (Category) -> Unit = {},
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    val tabs = listOf(
        stringResource(R.string.categories_tab_expense),
        stringResource(R.string.categories_tab_income),
    )
    val currentCategories = if (selectedTab == 0) expenseCategories else incomeCategories
    val currentType = if (selectedTab == 0) "EXPENSE" else "INCOME"

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
            Spacer(modifier = Modifier.height(12.dp))

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.SemiBold) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (currentCategories.isEmpty()) {
                AntEmptyState(
                    mascotRes = R.drawable.ic_ant_mascot,
                    title = stringResource(R.string.categories_empty),
                    subtitle = stringResource(R.string.categories_empty_subtitle),
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(currentCategories, key = { it.id }) { category ->
                        CategoryItem(
                            category = category,
                            onDelete = {
                                if (!category.isDefault) {
                                    categoryToDelete = category
                                }
                            },
                        )
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCategoryDialog(
            currentType = currentType,
            onConfirm = { name, icon, color ->
                onAddCategory(name, icon, color, currentType)
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
    val icon = categoryIconMap[category.icon]

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
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                } else {
                    Text(
                        text = category.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                if (category.isDefault) {
                    Text(
                        text = stringResource(R.string.categories_default_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (!category.isDefault) {
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddCategoryDialog(
    currentType: String,
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

@Preview(showBackground = true, name = "CategoriesScreen - With Tabs")
@Composable
private fun CategoriesContentPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        CategoriesContent(
            expenseCategories = listOf(
                Category(id = 1, name = "Casa", icon = "home", color = 0xFF4FC3F7, type = "EXPENSE", isDefault = true),
                Category(id = 2, name = "Cibo", icon = "restaurant", color = 0xFFE57373, type = "EXPENSE", isDefault = true),
                Category(id = 3, name = "Shopping", icon = "shopping_bag", color = 0xFFDCE775, type = "EXPENSE"),
            ),
            incomeCategories = listOf(
                Category(id = 10, name = "Stipendio", icon = "payments", color = 0xFF81C784, type = "INCOME", isDefault = true),
                Category(id = 11, name = "Freelance", icon = "work", color = 0xFFA1887F, type = "INCOME", isDefault = true),
            ),
        )
    }
}

@Preview(showBackground = true, name = "CategoriesScreen - Empty")
@Composable
private fun CategoriesContentEmptyPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        CategoriesContent(
            expenseCategories = emptyList(),
            incomeCategories = emptyList(),
        )
    }
}

@Preview(showBackground = true, name = "CategoriesScreen - Dark")
@Composable
private fun CategoriesContentDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        CategoriesContent(
            expenseCategories = listOf(
                Category(id = 1, name = "Trasporti", icon = "directions_car", color = 0xFF64B5F6, type = "EXPENSE", isDefault = true),
            ),
            incomeCategories = listOf(
                Category(id = 10, name = "Stipendio", icon = "payments", color = 0xFF81C784, type = "INCOME", isDefault = true),
            ),
        )
    }
}

@Preview(showBackground = true, name = "AddCategoryDialog")
@Composable
private fun AddCategoryDialogPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        AddCategoryDialog(
            currentType = "EXPENSE",
            onConfirm = { _, _, _ -> },
            onDismiss = {},
        )
    }
}
