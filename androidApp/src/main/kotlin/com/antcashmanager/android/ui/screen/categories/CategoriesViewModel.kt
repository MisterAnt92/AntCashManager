package com.antcashmanager.android.ui.screen.categories

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.analytics.AnalyticsConstants.Events
import com.antcashmanager.android.analytics.AnalyticsConstants.Params
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.ui.base.AndroidBaseViewModel
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.CategoryBudget
import com.antcashmanager.domain.usecase.category.DeleteCategoryBudgetUseCase
import com.antcashmanager.domain.usecase.category.DeleteCategoryUseCase
import com.antcashmanager.domain.usecase.category.GetCategoryBudgetsUseCase
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.category.InsertCategoryUseCase
import com.antcashmanager.domain.usecase.category.RestoreDefaultCategoriesUseCase
import com.antcashmanager.domain.usecase.category.SetCategoryBudgetUseCase
import com.antcashmanager.domain.usecase.category.UpdateCategoryUseCase
import com.antcashmanager.domain.usecase.transaction.SyncTransactionCategoriesUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val insertCategoryUseCase: InsertCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val restoreDefaultCategoriesUseCase: RestoreDefaultCategoriesUseCase,
    private val syncTransactionCategoriesUseCase: SyncTransactionCategoriesUseCase,
    private val analyticsManager: AnalyticsManager,
    private val getCategoryBudgetsUseCase: GetCategoryBudgetsUseCase,
    private val setCategoryBudgetUseCase: SetCategoryBudgetUseCase,
    private val deleteCategoryBudgetUseCase: DeleteCategoryBudgetUseCase,
) : AndroidBaseViewModel<CategoryEvent, CategoriesState>(CategoriesState()) {

    init {
        viewModelScope.launch {
            combine(
                getCategoriesUseCase(),
                getCategoryBudgetsUseCase(),
            ) { catsResult, budgetsResult -> catsResult to budgetsResult }
                .collect { (catsResult, budgetsResult) ->
                    var updated = _state.value
                    catsResult
                        .onSuccess { cats ->
                            val (expense, income) = cats.partition { it.type == "EXPENSE" }
                            val sortedExpense = expense.sortedBy { it.sortOrder }
                            val sortedIncome = income.sortedBy { it.sortOrder }
                            val (defaultExp, customExp) = sortedExpense.partition { it.isDefault }
                            val (defaultInc, customInc) = sortedIncome.partition { it.isDefault }
                            updated = updated.copy(
                                categories = cats,
                                expenseCategories = sortedExpense.filterNot { it.isHidden },
                                incomeCategories = sortedIncome.filterNot { it.isHidden },
                                defaultExpenseCategories = defaultExp,
                                customExpenseCategories = customExp,
                                defaultIncomeCategories = defaultInc,
                                customIncomeCategories = customInc,
                            )
                            analyticsManager.logEvent(
                                "category_type_distribution",
                                Bundle().apply {
                                    putInt("expense_count", sortedExpense.size)
                                    putInt("income_count", sortedIncome.size)
                                },
                            )
                        }.onFailure { error ->
                            if (error is CancellationException) throw error
                            logError("Error loading categories: ${error.message}", error)
                        }
                    budgetsResult
                        .onSuccess { budgets ->
                            updated = updated.copy(budgets = budgets.associateBy { it.categoryId })
                        }.onFailure { error ->
                            if (error is CancellationException) throw error
                            logError("Error loading budgets: ${error.message}", error)
                        }
                    _state.value = updated
                }
        }
    }

    override fun handleEvent(event: CategoryEvent) {
        logDebug("Event: $event")
        when (event) {
            is CategoryEvent.AddCategoryEvent -> addCategory(event.name, event.icon, event.color, event.type)
            is CategoryEvent.UpdateCategoryEvent -> updateCategory(event.category)
            is CategoryEvent.DeleteCategoryEvent -> deleteCategory(event.category)
            is CategoryEvent.SetCategoryHiddenEvent -> setCategoryHidden(event.category, event.hidden)
            is CategoryEvent.ReorderCategoriesEvent -> reorderCategories(event.reordered)
            is CategoryEvent.RestoreDefaultCategoriesEvent -> restoreDefaultCategories()
            is CategoryEvent.LogItemClickedEvent -> logCategoryItemClicked(event.categoryName, event.index)
            is CategoryEvent.LogReorderOpenedEvent -> analyticsManager.logEvent(Events.CATEGORIES_REORDER_OPENED)
            is CategoryEvent.SelectTabEvent -> _state.update { it.copy(selectedTabIndex = event.index) }
            is CategoryEvent.OpenBudgetDialogEvent -> _state.update { it.copy(editingBudgetCategoryId = event.categoryId) }
            is CategoryEvent.SetBudgetEvent -> setBudget(event.categoryId, event.monthlyLimit)
            is CategoryEvent.DeleteBudgetEvent -> deleteBudget(event.categoryId)
        }
    }

    private fun logCategoryItemClicked(categoryName: String, index: Int) {
        analyticsManager.logEvent(
            Events.CATEGORIES_LIST_ITEM_CLICKED,
            Bundle().apply {
                putString(Params.CATEGORY_NAME, categoryName)
                putInt(Params.INDEX, index)
            },
        )
    }

    private fun addCategory(
        name: String,
        icon: String,
        color: Long,
        type: String = "EXPENSE",
    ) {
        logDebug("Adding category: $name ($type)")
        analyticsManager.logEvent(
            Events.CATEGORY_CRUD_OPERATION,
            Bundle().apply {
                putString(Params.OPERATION, Params.VALUE_CREATE)
                putString(Params.TYPE, type)
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
        analyticsManager.logEvent(
            Events.CATEGORY_CRUD_OPERATION,
            Bundle().apply {
                putString(Params.OPERATION, Params.VALUE_UPDATE)
                putString(Params.TYPE, category.type)
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

    private fun restoreDefaultCategories() {
        logInfo("Restoring default categories")
        viewModelScope.launch {
            restoreDefaultCategoriesUseCase().handleError { errorState ->
                _state.update { it.copy(errorState = errorState) }
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
        if (category.isDefault) {
            logWarn("Attempted to hide default category '${category.name}' - ignored")
            return
        }
        analyticsManager.logEvent(if (hidden) Events.CATEGORY_HIDDEN else Events.CATEGORY_SHOWN)
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
        analyticsManager.logEvent(
            Events.CATEGORY_CRUD_OPERATION,
            Bundle().apply { putString(Params.OPERATION, "reorder") },
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

    private fun setBudget(categoryId: Long, monthlyLimit: Double) {
        if (monthlyLimit <= 0.0) return
        logDebug("Setting budget for category $categoryId: $monthlyLimit")
        viewModelScope.launch {
            setCategoryBudgetUseCase(CategoryBudget(categoryId, monthlyLimit)).handleError { errorState ->
                _state.update { it.copy(errorState = errorState) }
            }
            _state.update { it.copy(editingBudgetCategoryId = null) }
        }
    }

    private fun deleteBudget(categoryId: Long) {
        logDebug("Deleting budget for category $categoryId")
        viewModelScope.launch {
            deleteCategoryBudgetUseCase(categoryId).handleError { errorState ->
                _state.update { it.copy(errorState = errorState) }
            }
        }
    }

    private fun deleteCategory(category: Category) {
        if (category.isDefault) {
            logWarn("Attempted to delete default category '${category.name}' - deletion ignored")
            return
        }
        logDebug("Deleting category: ${category.name}")
        analyticsManager.logEvent(
            Events.CATEGORY_CRUD_OPERATION,
            Bundle().apply {
                putString(Params.OPERATION, "delete")
                putString(Params.TYPE, category.type)
            },
        )
        viewModelScope.launch {
            deleteCategoryUseCase(category).handleError { errorState ->
                _state.update { it.copy(errorState = errorState) }
            }
        }
    }
}
