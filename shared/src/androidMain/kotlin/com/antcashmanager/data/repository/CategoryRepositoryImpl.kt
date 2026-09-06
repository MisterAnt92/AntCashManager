package com.antcashmanager.data.repository

import com.antcashmanager.data.local.dao.CategoryDao
import com.antcashmanager.data.mapper.toDomain
import com.antcashmanager.data.mapper.toEntity
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao,
) : CategoryRepository {
    override fun getAllCategories(): Flow<List<Category>> =
        categoryDao.getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getCategoryById(id: Long): Category? = categoryDao.getCategoryById(id)?.toDomain()

    override suspend fun getCategoryByName(name: String): Category? = categoryDao.getCategoryByName(name)?.toDomain()

    override suspend fun insertCategory(category: Category): Long = categoryDao.insertCategory(category.toEntity())

    override suspend fun updateCategory(category: Category): Unit = categoryDao.updateCategory(category.toEntity())

    override suspend fun deleteCategory(category: Category) {
        if (category.isDefault) return // Protegge le categorie predefinite dalla cancellazione
        categoryDao.deleteCategory(category.toEntity())
    }

    override suspend fun deleteAllCategories(): Unit = categoryDao.deleteAllNonDefaultCategories()

    override fun getCategoriesByType(type: String): Flow<List<Category>> =
        categoryDao.getCategoriesByType(type).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getDefaultCategoryCount(): Int = categoryDao.getDefaultCategoryCount()
}
