package com.antcashmanager.android.ui.screen.categories.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolunteerActivism
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.layout.HorizontalSpacer
import com.antcashmanager.android.R
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.ui.components.dialog.HelpButton
import com.antcashmanager.android.ui.components.state.AntEmptyState
import com.antcashmanager.android.navigation.LocalScreenHeaderConfigCallback
import com.antcashmanager.android.navigation.ScreenHeaderConfig
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.categories.CategoriesState
import com.antcashmanager.android.ui.screen.categories.CategoriesViewModel
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.util.translateCategory
import com.antcashmanager.domain.model.Category
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/** Map icon key names to Material Icons. */
val categoryIconMap: Map<String, ImageVector> = mapOf(
    "home" to Icons.Default.Home,
    "directions_car" to Icons.Default.DirectionsCar,
    "restaurant" to Icons.Default.Restaurant,
    "receipt_long" to Icons.AutoMirrored.Filled.ReceiptLong,
    "local_dining" to Icons.Default.LocalDining,
    "theater_comedy" to Icons.Default.TheaterComedy,
    "local_hospital" to Icons.Default.LocalHospital,
    "shopping_bag" to Icons.Default.ShoppingBag,
    "school" to Icons.Default.School,
    "more_horiz" to Icons.Default.MoreHoriz,
    "payments" to Icons.Default.Payments,
    "savings" to Icons.Default.Savings,
    "currency_exchange" to Icons.Default.CurrencyExchange,
    "trending_up" to Icons.AutoMirrored.Filled.TrendingUp,
    "work" to Icons.Default.Work,
    "redeem" to Icons.Default.Redeem,
    "flight" to Icons.Default.Flight,
    "fitness_center" to Icons.Default.FitnessCenter,
    "pets" to Icons.Default.Pets,
    "directions_bus" to Icons.Default.DirectionsBus,
    "lightbulb" to Icons.Default.Lightbulb,
    "celebration" to Icons.Default.Celebration,
    "subscriptions" to Icons.Default.Subscriptions,
    "fastfood" to Icons.Default.Fastfood,
    "star" to Icons.Default.Star,
    "volunteer_activism" to Icons.Default.VolunteerActivism,
    "directions_bike" to Icons.Default.DirectionsBike,
    "shopping_cart" to Icons.Default.ShoppingCart,
    "coffee" to Icons.Default.Coffee,
    "spa" to Icons.Default.Spa,
)

/**
 * Returns a localized content description for a given icon key.
 */
@Composable
fun getIconContentDescription(iconKey: String): String {
    val resId = when (iconKey) {
        "home" -> R.string.icon_home
        "directions_car" -> R.string.icon_directions_car
        "restaurant" -> R.string.icon_restaurant
        "receipt_long" -> R.string.icon_receipt_long
        "local_dining" -> R.string.icon_local_dining
        "theater_comedy" -> R.string.icon_theater_comedy
        "local_hospital" -> R.string.icon_local_hospital
        "shopping_bag" -> R.string.icon_shopping_bag
        "school" -> R.string.icon_school
        "more_horiz" -> R.string.icon_more_horiz
        "payments" -> R.string.icon_payments
        "savings" -> R.string.icon_savings
        "currency_exchange" -> R.string.icon_currency_exchange
        "trending_up" -> R.string.icon_trending_up
        "work" -> R.string.icon_work
        "redeem" -> R.string.icon_redeem
        "flight" -> R.string.icon_flight
        "fitness_center" -> R.string.icon_fitness_center
        "pets" -> R.string.icon_pets
        "directions_bus" -> R.string.icon_directions_bus
        "lightbulb" -> R.string.icon_lightbulb
        "celebration" -> R.string.icon_celebration
        "subscriptions" -> R.string.icon_subscriptions
        "fastfood" -> R.string.icon_fastfood
        "star" -> R.string.icon_star
        "volunteer_activism" -> R.string.icon_volunteer_activism
        "directions_bike" -> R.string.icon_directions_bike
        "shopping_cart" -> R.string.icon_shopping_cart
        "coffee" -> R.string.icon_coffee
        "spa" -> R.string.icon_spa
        else -> R.string.categories_icon_label
    }
    return stringResource(resId)
}

val categoryColors = listOf(
    0xFFE57373L, 0xFFFF8A65L, 0xFFFFB74DL, 0xFFFFD54FL,
    0xFFDCE775L, 0xFF81C784L, 0xFF4DB6ACL, 0xFF4FC3F7L,
    0xFF64B5F6L, 0xFF7986CBL, 0xFFBA68C8L, 0xFFF06292L,
    0xFF90A4AEL, 0xFFA1887FL,
)

@Composable
fun CategoriesScreen() {
    val viewModel: CategoriesViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    CategoriesContent(
        state = state,
        onAddCategory = viewModel::addCategory,
        onDeleteCategory = viewModel::deleteCategory,
        onSetCategoryHidden = viewModel::setCategoryHidden,
        onReorderCategories = viewModel::reorderCategories,
    )
}

@Composable
internal fun CategoriesContent(
    state: CategoriesState,
    onAddCategory: (String, String, Long, String) -> Unit = { _, _, _, _ -> },
    onDeleteCategory: (Category) -> Unit = {},
    onSetCategoryHidden: (Category, Boolean) -> Unit = { _, _ -> },
    onReorderCategories: (List<Category>) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val analyticsManager: AnalyticsManager = koinInject()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showReorderDialog by remember { mutableStateOf(false) }
    var hiddenSectionExpanded by remember { mutableStateOf(false) }

    // Help dialog
    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }

    val tabs = listOf(
        stringResource(R.string.categories_tab_expense),
        stringResource(R.string.categories_tab_income),
    )
    val currentCategories =
        if (selectedTab == 0) state.expenseCategories else state.incomeCategories
    val currentHiddenCategories =
        if (selectedTab == 0) state.hiddenExpenseCategories else state.hiddenIncomeCategories
    val currentType = if (selectedTab == 0) "EXPENSE" else "INCOME"

    // Configure screen header with actions
    val headerConfigCallback = LocalScreenHeaderConfigCallback.current
    val categoriesTitle = stringResource(R.string.common_categories)
    LaunchedEffect(currentCategories.size, selectedTab) {
        headerConfigCallback?.invoke(
            ScreenHeaderConfig(
                title = categoriesTitle,
                hasOrderOption = currentCategories.size > 1,
                onOrderClick = {
                    if (currentCategories.size > 1) {
                        analyticsManager.logEvent("categories_reorder_opened")
                        showReorderDialog = true
                    }
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        HelpButton(
                            onHelpClick = {
                                analyticsManager.logEvent("categories_help_opened")
                                showHelpDialog = true
                            },
                        )
                    }
                }
            )
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.categories_add_content_desc)
                )
            }
        },
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("categories_screen"),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    top = 0.dp,
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = innerPadding.calculateBottomPadding(),
                ),
        ) {

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { AppText(title, fontWeight = FontWeight.SemiBold) },
                    )
                }
            }

            VerticalSpacer(SpacingSize.SM)

            if (currentCategories.isEmpty() && currentHiddenCategories.isEmpty()) {
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
                            onClick = {
                                val params = android.os.Bundle().apply {
                                    putString("category_name", category.name)
                                    putInt("index", currentCategories.indexOf(category))
                                }
                                analyticsManager.logEvent("categories_list_item_clicked", params)
                            },
                            onDelete = {
                                if (!category.isDefault) {
                                    categoryToDelete = category
                                }
                            },
                            onToggleHidden = {
                                analyticsManager.logEvent("category_hidden")
                                onSetCategoryHidden(category, true)
                            },
                        )
                    }
                    if (currentHiddenCategories.isNotEmpty()) {
                        item(key = "hidden_section_header") {
                            HiddenCategoriesSectionHeader(
                                count = currentHiddenCategories.size,
                                expanded = hiddenSectionExpanded,
                                onExpandedChange = { hiddenSectionExpanded = it },
                            )
                        }
                        if (hiddenSectionExpanded) {
                            items(
                                currentHiddenCategories,
                                key = { "hidden_${it.id}" }) { category ->
                                CategoryItem(
                                    category = category,
                                    onDelete = {
                                        if (!category.isDefault) {
                                            categoryToDelete = category
                                        }
                                    },
                                    onToggleHidden = {
                                        analyticsManager.logEvent("category_shown")
                                        onSetCategoryHidden(category, false)
                                    },
                                )
                            }
                        }
                    }
                    item { VerticalSpacer(SpacingSize.XXXL) }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCategoryDialog(
            currentType = currentType,
            onConfirm = { name, icon, color ->
                analyticsManager.logEvent("category_created")
                onAddCategory(name, icon, color, currentType)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false },
        )
    }

    if (showReorderDialog) {
        CategoriesReorderDialog(
            categories = currentCategories,
            onDismiss = { showReorderDialog = false },
            onConfirm = { reordered ->
                analyticsManager.logEvent("categories_reordered")
                onReorderCategories(reordered)
                showReorderDialog = false
            },
        )
    }

    categoryToDelete?.let { category ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { AppText(stringResource(R.string.categories_delete_title)) },
            text = {
                AppText(
                    stringResource(
                        R.string.categories_delete_message,
                        translateCategory(category.name)
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        analyticsManager.logEvent("category_deleted")
                        onDeleteCategory(category)
                        categoryToDelete = null
                    },
                ) {
                    AppText(
                        stringResource(R.string.dialog_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    AppText(stringResource(R.string.common_cancel))
                }
            },
        )
    }
}

@Composable
private fun HiddenCategoriesSectionHeader(
    count: Int,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(300),
        label = "hidden_categories_chevron_rotation",
    )
    val expandCollapseLabel = if (expanded) {
        stringResource(R.string.common_collapse)
    } else {
        stringResource(R.string.common_expand)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = expandCollapseLabel) { onExpandedChange(!expanded) }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppText(
            text = stringResource(R.string.categories_hidden_section_title, count),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = expandCollapseLabel,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .graphicsLayer(rotationZ = rotationAngle)
                .size(24.dp),
        )
    }
}

@Composable
private fun CategoryItem(
    category: Category,
    onDelete: () -> Unit,
    onToggleHidden: () -> Unit,
    onClick: (() -> Unit)? = null,
) {
    val icon = categoryIconMap[category.icon]
    val translatedName = translateCategory(category.name)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable { onClick() } else it },
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
                    AppText(
                        text = translatedName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            HorizontalSpacer(SpacingSize.MD)
            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = translatedName,
                    style = MaterialTheme.typography.titleMedium,
                )
                if (category.isDefault) {
                    AppText(
                        text = stringResource(R.string.categories_default_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            IconButton(onClick = onToggleHidden) {
                Icon(
                    imageVector = if (category.isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (category.isHidden) {
                        stringResource(R.string.categories_show)
                    } else {
                        stringResource(R.string.categories_hide)
                    },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
    var selectedIcon by remember { mutableStateOf("category") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { AppText(stringResource(R.string.categories_add)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                // Category Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { AppText(stringResource(R.string.categories_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                VerticalSpacer(SpacingSize.MD)

                // Icon Selection
                AppText(
                    text = stringResource(R.string.categories_icon_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                VerticalSpacer(SpacingSize.XS)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    categoryIconMap.forEach { (iconKey, iconVector) ->
                        val isSelected = iconKey == selectedIcon
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                                .then(
                                    if (isSelected) {
                                        Modifier.border(
                                            2.dp,
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        )
                                    } else {
                                        Modifier
                                    },
                                )
                                .selectable(
                                    selected = isSelected,
                                    onClick = { selectedIcon = iconKey },
                                    role = Role.RadioButton,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = getIconContentDescription(iconKey),
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
                VerticalSpacer(SpacingSize.MD)

                // Color Selection
                AppText(
                    text = stringResource(R.string.categories_color_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                VerticalSpacer(SpacingSize.XS)
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
                                        Modifier.border(
                                            3.dp,
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        )
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
                onClick = {
                    if (name.isNotBlank()) onConfirm(
                        name.trim(),
                        selectedIcon,
                        selectedColor
                    )
                },
                enabled = name.isNotBlank(),
            ) {
                AppText(stringResource(R.string.dialog_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { AppText(stringResource(R.string.common_cancel)) }
        },
    )
}

// ── Previews ──

@Preview(showBackground = true, name = "CategoriesScreen - With Tabs")
@Preview(showBackground = true, name = "CategoriesScreen - 7 inch", widthDp = 600, heightDp = 960)
@Preview(showBackground = true, name = "CategoriesScreen - 10 inch", widthDp = 840, heightDp = 1280)
@Composable
private fun CategoriesContentPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        CategoriesContent(
            state = CategoriesState(
                expenseCategories = listOf(
                    Category(
                        id = 1,
                        name = "Casa",
                        icon = "home",
                        color = 0xFF4FC3F7,
                        type = "EXPENSE",
                        isDefault = true
                    ),
                    Category(
                        id = 2,
                        name = "Cibo",
                        icon = "restaurant",
                        color = 0xFFE57373,
                        type = "EXPENSE",
                        isDefault = true
                    ),
                    Category(
                        id = 3,
                        name = "Shopping",
                        icon = "shopping_bag",
                        color = 0xFFDCE775,
                        type = "EXPENSE"
                    ),
                ),
                incomeCategories = listOf(
                    Category(
                        id = 10,
                        name = "Stipendio",
                        icon = "payments",
                        color = 0xFF81C784,
                        type = "INCOME",
                        isDefault = true
                    ),
                    Category(
                        id = 11,
                        name = "Freelance",
                        icon = "work",
                        color = 0xFFA1887F,
                        type = "INCOME",
                        isDefault = true
                    ),
                ),
            ),
            modifier = Modifier,
        )
    }
}

@Preview(showBackground = true, name = "CategoriesScreen - Empty")
@Composable
private fun CategoriesContentEmptyPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        CategoriesContent(
            state = CategoriesState(
                expenseCategories = emptyList(),
                incomeCategories = emptyList(),
            ),
            modifier = Modifier,
        )
    }
}

@Preview(showBackground = true, name = "CategoriesScreen - Dark")
@Composable
private fun CategoriesContentDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        CategoriesContent(
            state = CategoriesState(
                expenseCategories = listOf(
                    Category(
                        id = 1,
                        name = "Trasporti",
                        icon = "directions_car",
                        color = 0xFF64B5F6,
                        type = "EXPENSE",
                        isDefault = true
                    ),
                ),
                incomeCategories = listOf(
                    Category(
                        id = 10,
                        name = "Stipendio",
                        icon = "payments",
                        color = 0xFF81C784,
                        type = "INCOME",
                        isDefault = true
                    ),
                ),
            ),
            modifier = Modifier,
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
