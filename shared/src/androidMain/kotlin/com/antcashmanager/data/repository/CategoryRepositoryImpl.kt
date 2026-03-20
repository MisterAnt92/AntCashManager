package com.antcashmanager.data.repository
import com.antcashmanager.data.local.dao.CategoryDao
import com.antcashmanager.data.mapper.toDomain
import com.antcashmanager.data.mapper.toEntity
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao,
) : CategoryRepository {
    override fun getAllCategories(): Flow<List<Category>> =
        categoryDao.getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    override suspend fun getCategoryById(id: Long): Category? =
        categoryDao.getCategoryById(id)?.toDomain()
    override suspend fun insertCategory(category: Category): Long =
        categoryDao.insertCategory(category.toEntity())
    override suspend fun updateCategory(category: Category) =
        categoryDao.updateCategory(category.toEntity())
    override suspend fun deleteCategory(category: Category) =
        categoryDao.deleteCategory(category.toEntity())
    override suspend fun deleteAllCategories() =
        categoryDao.deleteAllCategories()
}
