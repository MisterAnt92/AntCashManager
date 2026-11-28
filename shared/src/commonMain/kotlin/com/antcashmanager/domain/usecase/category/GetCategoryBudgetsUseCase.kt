package com.antcashmanager.domain.usecase.category

import com.antcashmanager.domain.model.CategoryBudget
import com.antcashmanager.domain.repository.CategoryBudgetRepository
import com.antcashmanager.domain.usecase.base.NoParamsObservableUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

/** Emits the full list of category budgets as a reactive [Flow]. */
public class GetCategoryBudgetsUseCase(
    private val repository: CategoryBudgetRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsObservableUseCase<List<CategoryBudget>>(dispatcher) {

    override fun execute(params: Unit): Flow<List<CategoryBudget>> = repository.getBudgets()
}
