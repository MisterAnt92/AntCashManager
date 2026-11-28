package com.antcashmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.antcashmanager.domain.model.CategoryBudget

@Entity(tableName = "category_budgets")
public data class CategoryBudgetEntity(
    @PrimaryKey val categoryId: Long,
    val monthlyLimit: Double,
) {
    public fun toDomain(): CategoryBudget = CategoryBudget(categoryId, monthlyLimit)
}

public fun CategoryBudget.toEntity(): CategoryBudgetEntity =
    CategoryBudgetEntity(categoryId, monthlyLimit)
