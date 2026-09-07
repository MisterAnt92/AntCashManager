package com.antcashmanager.android.ui.screen.categories

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.ui.base.BaseViewModel
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
    private val analyticsManager: AnalyticsManager,
) : BaseViewModel<CategoryEvent>() {
    private val _state = MutableStateFlow(CategoriesState())
    val state: StateFlow<CategoriesState> = _state

    init {
        viewModelScope.launch {
            getCategoriesUseCase().collect { result ->
                result
                    .onSuccess { cats ->
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
                        // Track category type distribution
                        analyticsManager.logEvent(
                            "category_type_distribution",
                            Bundle().apply {
                                putInt("expense_count", expense.size)
                                putInt("income_count", income.size)
                            },
                        )
                    }.onFailure { error ->
                        if (error is CancellationException) throw error
                        logError("Error loading categories: ${error.message}", error)
                    }
            }
        }
    }

    override fun onEvent(event: CategoryEvent) {
        logDebug("Event: $event")
        when (event) {
            is CategoryEvent.AddCategory -> addCategory(event.name, event.icon, event.color, event.type)
            is CategoryEvent.UpdateCategory -> updateCategory(event.category)
            is CategoryEvent.DeleteCategory -> deleteCategory(event.category)
            is CategoryEvent.SetCategoryHidden -> setCategoryHidden(event.category, event.hidden)
            is CategoryEvent.ReorderCategories -> reorderCategories(event.reordered)
            is CategoryEvent.RetryLastOperation -> logInfo("Retry requested")
        }
    }

    private fun addCategory(
        name: String,
        icon: String,
        color: Long,
        type: String = "EXPENSE",
    ) {
        logDebug("Adding category: $name ($type)")
        // Track category creation
        analyticsManager.logEvent(
            "category_crud_operation",
            Bundle().apply {
                putString("operation", "create")
                putString("type", type)
            },
        )
        // Accoda la nuova categoria in fondo all'ordine esistente per quel tipo, invece di
        // farla comparire in cima con sortOrder = 0 di default.
        val nextSortOrder =
            (
                _state.value.categories
                    .filter { it.type == type }
                    .maxOfOrNull { it.sortOrder }
                    ?: -1
            ) + 1
        viewModelScope.launch {
            insertCategoryUseCase(
                Category(
                    name = name,
                    icon = icon,
                    color = color,
                    type = type,
                    sortOrder = nextSortOrder,
                ),
            ).handleError { errorState ->
                _state.update { it.copy(errorState = errorState) }
            }
        }
    }

    private fun updateCategory(category: Category) {
        logDebug("Updating category: ${category.name}")
        // Track category update
        analyticsManager.logEvent(
            "category_crud_operation",
            Bundle().apply {
                putString("operation", "update")
                putString("type", category.type)
            },
        )
        val oldName =
            _state.value.categories
                .find { it.id == category.id }
                ?.name
        viewModelScope.launch {
            updateCategoryUseCase(category)
                .handleError { errorState ->
                    _state.update { it.copy(errorState = errorState) }
                }?.also {
                    // Propaga nome/icona/colore aggiornati alle transazioni che referenziano
                    // ancora il nome precedente, evitando categorie "orfane" nei Grafici.
                    if (oldName != null) {
                        syncTransactionCategoriesUseCase(
                            SyncTransactionCategoriesUseCase.Params(oldName, category),
                        ).handleError { errorState ->
                            _state.update { it.copy(errorState = errorState) }
                        }
                    }
                }
        }
    }

    /**
     * Nasconde/mostra di nuovo una categoria senza cancellarla. A differenza di [updateCategory],
     * non propaga nulla alle transazioni esistenti: nome/icona/colore non cambiano, quindi la
     * sincronizzazione sarebbe solo un aggiornamento ridondante su tutte le transazioni
     * corrispondenti.
     */
    private fun setCategoryHidden(
        category: Category,
        hidden: Boolean,
    ) {
        logDebug("Setting category '${category.name}' hidden=$hidden")
        viewModelScope.launch {
            updateCategoryUseCase(category.copy(isHidden = hidden)).handleError { errorState ->
                _state.update { it.copy(errorState = errorState) }
            }
        }
    }

    /**
     * Persiste il nuovo ordine di [reordered] assegnando `sortOrder` in base alla posizione
     * nella lista. Aggiorna solo le categorie il cui `sortOrder` è effettivamente cambiato.
     */
    private fun reorderCategories(reordered: List<Category>) {
        logDebug("Reordering ${reordered.size} categories")
        // Track category reorder
        analyticsManager.logEvent(
            "category_crud_operation",
            Bundle().apply {
                putString("operation", "reorder")
            },
        )
        viewModelScope.launch {
            reordered.forEachIndexed { index, category ->
                if (category.sortOrder != index) {
                    updateCategoryUseCase(category.copy(sortOrder = index)).handleError { errorState ->
                        _state.update { it.copy(errorState = errorState) }
                    }
                }
            }
        }
    }

    private fun deleteCategory(category: Category) {
        logDebug("Deleting category: ${category.name}")
        // Track category delete
        analyticsManager.logEvent(
            "category_crud_operation",
            Bundle().apply {
                putString("operation", "delete")
                putString("type", category.type)
            },
        )
        viewModelScope.launch {
            deleteCategoryUseCase(category).handleError { errorState ->
                _state.update { it.copy(errorState = errorState) }
            }
        }
    }
}
