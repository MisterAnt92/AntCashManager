package com.antcashmanager.domain.usecase.category

import com.antcashmanager.domain.model.CategoryBudget
import com.antcashmanager.domain.repository.CategoryBudgetRepository
import com.antcashmanager.domain.usecase.base.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** Persists or updates a [CategoryBudget]. */
public class SetCategoryBudgetUseCase(
    private val repository: CategoryBudgetRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<CategoryBudget, Unit>(dispatcher) {

    override suspend fun execute(params: CategoryBudget) {
        repository.setBudget(params)
    }
}
