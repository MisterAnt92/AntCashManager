package com.antcashmanager.android.ui.screen.categories

import com.antcashmanager.android.ui.base.ErrorState
import com.antcashmanager.domain.model.Category

/**
 * Stato UI per la schermata delle categorie.
 *
 * [expenseCategories]/[incomeCategories] contengono solo le categorie visibili, ordinate per
 * [Category.sortOrder]. Le categorie nascoste sono esposte separatamente in
 * [hiddenExpenseCategories]/[hiddenIncomeCategories] per la sezione dedicata "categorie
 * nascoste" nella UI.
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
    val hiddenExpenseCategories: List<Category> = emptyList(),
    val hiddenIncomeCategories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val errorState: ErrorState = ErrorState(),
)
