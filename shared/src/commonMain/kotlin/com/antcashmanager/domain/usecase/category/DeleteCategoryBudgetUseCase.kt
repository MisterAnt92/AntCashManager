package com.antcashmanager.domain.usecase.category

import com.antcashmanager.domain.repository.CategoryBudgetRepository
import com.antcashmanager.domain.usecase.base.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** Removes the budget for a category identified by [Long] id. */
public class DeleteCategoryBudgetUseCase(
    private val repository: CategoryBudgetRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<Long, Unit>(dispatcher) {

    override suspend fun execute(params: Long) {
        repository.deleteBudget(params)
    }
}
