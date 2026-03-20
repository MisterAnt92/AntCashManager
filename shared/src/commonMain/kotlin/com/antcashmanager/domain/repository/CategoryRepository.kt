package com.antcashmanager.domain.repository
import com.antcashmanager.domain.model.Category
import kotlinx.coroutines.flow.Flow
interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: Long): Category?
    suspend fun insertCategory(category: Category): Long
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(category: Category)
    suspend fun deleteAllCategories()
    fun getCategoriesByType(type: String): Flow<List<Category>>
    suspend fun getDefaultCategoryCount(): Int
}
