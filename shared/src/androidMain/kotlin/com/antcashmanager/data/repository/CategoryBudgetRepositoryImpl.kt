package com.antcashmanager.data.repository

import com.antcashmanager.data.local.dao.CategoryBudgetDao
import com.antcashmanager.data.local.entity.toEntity
import com.antcashmanager.domain.model.CategoryBudget
import com.antcashmanager.domain.repository.CategoryBudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class CategoryBudgetRepositoryImpl(
    private val dao: CategoryBudgetDao,
) : CategoryBudgetRepository {

    override fun getBudgets(): Flow<List<CategoryBudget>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun setBudget(budget: CategoryBudget) {
        dao.upsert(budget.toEntity())
    }

    override suspend fun deleteBudget(categoryId: Long) {
        dao.deleteById(categoryId)
    }
}
