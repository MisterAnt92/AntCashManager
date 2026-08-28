package com.antcashmanager.domain.repository

import com.antcashmanager.domain.model.Category
import kotlinx.coroutines.flow.Flow

public interface CategoryRepository {
    public fun getAllCategories(): Flow<List<Category>>
    public suspend fun getCategoryById(id: Long): Category?
    public suspend fun getCategoryByName(name: String): Category?
    public suspend fun insertCategory(category: Category): Long
    public suspend fun updateCategory(category: Category): Unit
    public suspend fun deleteCategory(category: Category): Unit
    public suspend fun deleteAllCategories(): Unit
    public fun getCategoriesByType(type: String): Flow<List<Category>>
    public suspend fun getDefaultCategoryCount(): Int
}
