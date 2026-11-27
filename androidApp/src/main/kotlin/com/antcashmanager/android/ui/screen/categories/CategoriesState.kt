package com.antcashmanager.android.ui.screen.categories

import com.antcashmanager.architecture.ErrorState
import com.antcashmanager.architecture.UIState
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.CategoryBudget

/**
 * Stato UI per la schermata delle categorie.
 *
 * [expenseCategories]/[incomeCategories] contengono solo le categorie visibili, ordinate per
 * [Category.sortOrder].
 *
 * UDF Pattern:
 * - state: CategoriesState (this)
 * - events: CategoryEvent (sealed class)
 * - viewModel: CategoriesViewModel (onEvent handler)
 */
data class CategoriesState(
    val categories: List<Category> = emptyList(),
    val expenseCategories: List<Category> = emptyList(),
    val incomeCategories: List<Category> = emptyList(),
    val defaultExpenseCategories: List<Category> = emptyList(),
    val customExpenseCategories: List<Category> = emptyList(),
    val defaultIncomeCategories: List<Category> = emptyList(),
    val customIncomeCategories: List<Category> = emptyList(),
    val selectedTabIndex: Int = 0,
    val errorState: ErrorState = ErrorState(),
    /** Budget keyed by categoryId. */
    val budgets: Map<Long, CategoryBudget> = emptyMap(),
    /** Category id currently being edited in BudgetEditDialog, null = dialog closed. */
    val editingBudgetCategoryId: Long? = null,
    /** Absolute expense total (positive) per category for the current period. */
    val categoryExpenseTotals: Map<Long, Double> = emptyMap(),
) : UIState()
