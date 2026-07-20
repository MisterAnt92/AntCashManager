package com.antcashmanager.android.ui.screen.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.category.DeleteCategoryUseCase
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.category.InsertCategoryUseCase
import com.antcashmanager.domain.usecase.category.UpdateCategoryUseCase
import com.antcashmanager.domain.usecase.transaction.SyncTransactionCategoriesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val insertCategoryUseCase: InsertCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val syncTransactionCategoriesUseCase: SyncTransactionCategoriesUseCase,
) : ViewModel() {

    constructor(categoryRepository: CategoryRepository, transactionRepository: TransactionRepository) : this(
        getCategoriesUseCase = GetCategoriesUseCase(categoryRepository),
        insertCategoryUseCase = InsertCategoryUseCase(categoryRepository),
        updateCategoryUseCase = UpdateCategoryUseCase(categoryRepository),
        deleteCategoryUseCase = DeleteCategoryUseCase(categoryRepository),
        syncTransactionCategoriesUseCase = SyncTransactionCategoriesUseCase(transactionRepository),
    )

    private val _state = MutableStateFlow(CategoriesState())
    val state: StateFlow<CategoriesState> = _state

    init {
        viewModelScope.launch {
            getCategoriesUseCase().collect { result ->
                result.onSuccess { cats ->
                    _state.update {
                        it.copy(
                            categories = cats,
                            expenseCategories = cats.filter { category -> category.type == "EXPENSE" },
                            incomeCategories = cats.filter { category -> category.type == "INCOME" },
                        )
                    }
                }.onFailure { error ->
                    if (error is kotlinx.coroutines.CancellationException) throw error
                    Logger.e(throwable = error, tag = "CategoriesViewModel") { "Error loading categories: ${error.message}" }
                }
            }
        }
    }

    fun addCategory(name: String, icon: String, color: Long, type: String = "EXPENSE") {
        Logger.d(tag = "CategoriesViewModel") { "Adding category: $name ($type)" }
        viewModelScope.launch {
            val result = insertCategoryUseCase(
                Category(name = name, icon = icon, color = color, type = type),
            )
            result.onFailure { error ->
                if (error is kotlinx.coroutines.CancellationException) throw error
                Logger.e(throwable = error, tag = "CategoriesViewModel") { "Failed to insert category: ${error.message}" }
            }
        }
    }

    fun updateCategory(category: Category) {
        Logger.d(tag = "CategoriesViewModel") { "Updating category: ${category.name}" }
        val oldName = _state.value.categories.find { it.id == category.id }?.name
        viewModelScope.launch {
            val result = updateCategoryUseCase(category)
            result.onSuccess {
                // Propaga nome/icona/colore aggiornati alle transazioni che referenziano
                // ancora il nome precedente, evitando categorie "orfane" nei Grafici.
                if (oldName != null) {
                    syncTransactionCategoriesUseCase(
                        SyncTransactionCategoriesUseCase.Params(oldName, category),
                    ).onFailure { error ->
                        if (error is kotlinx.coroutines.CancellationException) throw error
                        Logger.e(throwable = error, tag = "CategoriesViewModel") { "Failed to sync transactions for renamed category: ${error.message}" }
                    }
                }
            }.onFailure { error ->
                if (error is kotlinx.coroutines.CancellationException) throw error
                Logger.e(throwable = error, tag = "CategoriesViewModel") { "Failed to update category: ${error.message}" }
            }
        }
    }

    fun deleteCategory(category: Category) {
        Logger.d(tag = "CategoriesViewModel") { "Deleting category: ${category.name}" }
        viewModelScope.launch {
            val result = deleteCategoryUseCase(category)
            result.onFailure { error ->
                if (error is kotlinx.coroutines.CancellationException) throw error
                Logger.e(throwable = error, tag = "CategoriesViewModel") { "Failed to delete category: ${error.message}" }
            }
        }
    }
}
