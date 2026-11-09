package com.antcashmanager.data.repository

import com.antcashmanager.data.local.dao.CategoryDao
import com.antcashmanager.data.mapper.toDomain
import com.antcashmanager.data.mapper.toEntity
import com.antcashmanager.domain.manager.ProfileManager
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

public class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao,
    private val profileManager: ProfileManager,
) : CategoryRepository {

    private val pid get() = profileManager.getActiveProfileIdSync()

    override fun getAllCategories(): Flow<List<Category>> =
        profileManager.activeProfileId
            .flatMapLatest { profileId -> categoryDao.getAllCategories(profileId) }
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun getCategoryById(id: Long): Category? =
        categoryDao.getCategoryById(id)?.toDomain()

    override suspend fun getCategoryByName(name: String): Category? =
        categoryDao.getCategoryByName(pid, name)?.toDomain()

    override suspend fun insertCategory(category: Category): Long =
        categoryDao.insertCategory(category.toEntity().copy(profileId = pid))

    override suspend fun updateCategory(category: Category): Unit =
        categoryDao.updateCategory(category.toEntity())

    override suspend fun deleteCategory(category: Category) {
        if (category.isDefault) return
        categoryDao.deleteCategory(category.toEntity())
    }

    override suspend fun deleteAllCategories(): Unit =
        categoryDao.deleteAllNonDefaultCategories(pid)

    override fun getCategoriesByType(type: String): Flow<List<Category>> =
        profileManager.activeProfileId
            .flatMapLatest { profileId -> categoryDao.getCategoriesByType(profileId, type) }
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun getDefaultCategoryCount(): Int =
        categoryDao.getDefaultCategoryCount(pid)

    override suspend fun deleteAllCategoriesForProfile(profileId: Long) {
        categoryDao.deleteAllCategories(profileId)
    }

    override suspend fun deleteAllNonDefaultCategoriesForProfile(profileId: Long) {
        categoryDao.deleteAllNonDefaultCategories(profileId)
    }

    override suspend fun seedDefaultCategoriesForProfile(
        sourceProfileId: Long,
        targetProfileId: Long,
    ) {
        val defaults = categoryDao.getDefaultCategoriesForProfile(sourceProfileId)
        defaults.forEach { entity ->
            categoryDao.insertCategory(entity.copy(id = 0, profileId = targetProfileId))
        }
    }
}
