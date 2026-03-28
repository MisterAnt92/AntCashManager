package com.antcashmanager.android.ui.screen.home.categories

import com.antcashmanager.domain.model.Category

/**
 * Stato UI per la schermata delle categorie.
 */
data class CategoriesState(
    val categories: List<Category> = emptyList(),
    val expenseCategories: List<Category> = emptyList(),
    val incomeCategories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

