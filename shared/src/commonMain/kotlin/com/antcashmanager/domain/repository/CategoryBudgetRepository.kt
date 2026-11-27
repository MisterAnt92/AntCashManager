package com.antcashmanager.domain.repository

import com.antcashmanager.domain.model.CategoryBudget
import kotlinx.coroutines.flow.Flow

public interface CategoryBudgetRepository {
    public fun getBudgets(): Flow<List<CategoryBudget>>

    public suspend fun setBudget(budget: CategoryBudget)

    public suspend fun deleteBudget(categoryId: Long)
}
