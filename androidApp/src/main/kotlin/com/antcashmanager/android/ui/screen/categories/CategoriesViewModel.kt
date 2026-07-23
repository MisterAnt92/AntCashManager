package com.antcashmanager.android.ui.screen.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.usecase.category.DeleteCategoryUseCase
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.category.InsertCategoryUseCase
import com.antcashmanager.domain.usecase.category.UpdateCategoryUseCase
import com.antcashmanager.domain.usecase.transaction.SyncTransactionCategoriesUseCase
import kotlinx.coroutines.CancellationException
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

    private val _state = MutableStateFlow(CategoriesState())
    val state: StateFlow<CategoriesState> = _state

    init {
        viewModelScope.launch {
            getCategoriesUseCase().collect { result ->
                result.onSuccess { cats ->
                    val expense = cats.filter { it.type == "EXPENSE" }.sortedBy { it.sortOrder }
                    val income = cats.filter { it.type == "INCOME" }.sortedBy { it.sortOrder }
                    _state.update {
                        it.copy(
                            categories = cats,
                            expenseCategories = expense.filterNot { category -> category.isHidden },
                            incomeCategories = income.filterNot { category -> category.isHidden },
                            hiddenExpenseCategories = expense.filter { category -> category.isHidden },
                            hiddenIncomeCategories = income.filter { category -> category.isHidden },
                        )
                    }
                }.onFailure { error ->
                    if (error is CancellationException) throw error
                    Logger.e(
                        throwable = error,
                        tag = "CategoriesViewModel"
                    ) { "Error loading categories: ${error.message}" }
                }
            }
        }
    }

    fun addCategory(name: String, icon: String, color: Long, type: String = "EXPENSE") {
        Logger.d(tag = "CategoriesViewModel") { "Adding category: $name ($type)" }
        // Accoda la nuova categoria in fondo all'ordine esistente per quel tipo, invece di
        // farla comparire in cima con sortOrder = 0 di default.
        val nextSortOrder = (
                _state.value.categories
                    .filter { it.type == type }
                    .maxOfOrNull { it.sortOrder }
                    ?: -1
                ) + 1
        viewModelScope.launch {
            val result = insertCategoryUseCase(
                Category(
                    name = name,
                    icon = icon,
                    color = color,
                    type = type,
                    sortOrder = nextSortOrder
                ),
            )
            result.onFailure { error ->
                if (error is CancellationException) throw error
                Logger.e(
                    throwable = error,
                    tag = "CategoriesViewModel"
                ) { "Failed to insert category: ${error.message}" }
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
                        if (error is CancellationException) throw error
                        Logger.e(
                            throwable = error,
                            tag = "CategoriesViewModel"
                        ) { "Failed to sync transactions for renamed category: ${error.message}" }
                    }
                }
            }.onFailure { error ->
                if (error is CancellationException) throw error
                Logger.e(
                    throwable = error,
                    tag = "CategoriesViewModel"
                ) { "Failed to update category: ${error.message}" }
            }
        }
    }

    /**
     * Nasconde/mostra di nuovo una categoria senza cancellarla. A differenza di [updateCategory],
     * non propaga nulla alle transazioni esistenti: nome/icona/colore non cambiano, quindi la
     * sincronizzazione sarebbe solo un aggiornamento ridondante su tutte le transazioni
     * corrispondenti.
     */
    fun setCategoryHidden(category: Category, hidden: Boolean) {
        Logger.d(tag = "CategoriesViewModel") { "Setting category '${category.name}' hidden=$hidden" }
        viewModelScope.launch {
            updateCategoryUseCase(category.copy(isHidden = hidden)).onFailure { error ->
                if (error is CancellationException) throw error
                Logger.e(
                    throwable = error,
                    tag = "CategoriesViewModel"
                ) { "Failed to update category visibility: ${error.message}" }
            }
        }
    }

    /**
     * Persiste il nuovo ordine di [reordered] assegnando `sortOrder` in base alla posizione
     * nella lista. Aggiorna solo le categorie il cui `sortOrder` è effettivamente cambiato.
     */
    fun reorderCategories(reordered: List<Category>) {
        Logger.d(tag = "CategoriesViewModel") { "Reordering ${reordered.size} categories" }
        viewModelScope.launch {
            reordered.forEachIndexed { index, category ->
                if (category.sortOrder != index) {
                    updateCategoryUseCase(category.copy(sortOrder = index)).onFailure { error ->
                        if (error is CancellationException) throw error
                        Logger.e(
                            throwable = error,
                            tag = "CategoriesViewModel"
                        ) { "Failed to persist reordered category '${category.name}': ${error.message}" }
                    }
                }
            }
        }
    }

    fun deleteCategory(category: Category) {
        Logger.d(tag = "CategoriesViewModel") { "Deleting category: ${category.name}" }
        viewModelScope.launch {
            val result = deleteCategoryUseCase(category)
            result.onFailure { error ->
                if (error is CancellationException) throw error
                Logger.e(
                    throwable = error,
                    tag = "CategoriesViewModel"
                ) { "Failed to delete category: ${error.message}" }
            }
        }
    }
}
