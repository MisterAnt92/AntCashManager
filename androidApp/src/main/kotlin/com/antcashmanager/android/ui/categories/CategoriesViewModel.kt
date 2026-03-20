package com.antcashmanager.android.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.usecase.category.DeleteCategoryUseCase
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.category.InsertCategoryUseCase
import com.antcashmanager.domain.usecase.category.UpdateCategoryUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val getCategoriesUseCase = GetCategoriesUseCase(categoryRepository)
    private val insertCategoryUseCase = InsertCategoryUseCase(categoryRepository)
    private val updateCategoryUseCase = UpdateCategoryUseCase(categoryRepository)
    private val deleteCategoryUseCase = DeleteCategoryUseCase(categoryRepository)

    val categories = getCategoriesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val expenseCategories = categoryRepository.getCategoriesByType("EXPENSE")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val incomeCategories = categoryRepository.getCategoriesByType("INCOME")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun addCategory(name: String, icon: String, color: Long, type: String = "EXPENSE") {
        Logger.d("CategoriesViewModel") { "Adding category: $name ($type)" }
        viewModelScope.launch {
            insertCategoryUseCase(
                Category(name = name, icon = icon, color = color, type = type),
            )
        }
    }

    fun updateCategory(category: Category) {
        Logger.d("CategoriesViewModel") { "Updating category: ${category.name}" }
        viewModelScope.launch {
            updateCategoryUseCase(category)
        }
    }

    fun deleteCategory(category: Category) {
        Logger.d("CategoriesViewModel") { "Deleting category: ${category.name}" }
        viewModelScope.launch {
            deleteCategoryUseCase(category)
        }
    }
}
