package com.antcashmanager.data.repository

import com.antcashmanager.data.local.dao.CategoryDao
import com.antcashmanager.data.mapper.toDomain
import com.antcashmanager.data.mapper.toEntity
import com.antcashmanager.domain.manager.ProfileManager
import com.antcashmanager.domain.model.ALL_DEFAULT_CATEGORIES
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
            .map { entities ->
                val categories = entities.map { it.toDomain() }
                if (categories.isEmpty()) ALL_DEFAULT_CATEGORIES else categories
            }

    override suspend fun getCategoryById(id: Long): Category? =
        categoryDao.getCategoryById(id)?.toDomain()

    override suspend fun getCategoryByName(name: String): Category? =
        categoryDao.getCategoryByName(pid, name)?.toDomain()

    override suspend fun insertCategory(category: Category): Long {
        if (!category.isDefault) {
            val existingDefaults =
                categoryDao.getDefaultCategoriesForProfile(pid).associateBy {
                    "${it.type}_${it.name}"
                }
            ALL_DEFAULT_CATEGORIES.forEach { defaultCategory ->
                val key = "${defaultCategory.type}_${defaultCategory.name}"
                if (!existingDefaults.containsKey(key)) {
                    categoryDao.insertCategory(
                        defaultCategory.toEntity().copy(profileId = pid),
                    )
                }
            }
        }
        return categoryDao.insertCategory(category.toEntity().copy(profileId = pid))
    }

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
            .map { entities ->
                val categories = entities.map { it.toDomain() }
                if (categories.isEmpty()) {
                    ALL_DEFAULT_CATEGORIES.filter { it.type == type }
                } else {
                    categories
                }
            }

    override suspend fun getDefaultCategoryCount(): Int =
        categoryDao.getDefaultCategoryCount(pid)

    override suspend fun deleteAllCategoriesForProfile(profileId: Long) {
        categoryDao.deleteAllCategories(profileId)
    }

    override suspend fun deleteAllNonDefaultCategoriesForProfile(profileId: Long) {
        categoryDao.deleteAllNonDefaultCategories(profileId)
    }

    override suspend fun seedDefaultCategoriesForProfile(targetProfileId: Long) {
        ALL_DEFAULT_CATEGORIES.forEach { category ->
            categoryDao.insertCategory(category.toEntity().copy(profileId = targetProfileId))
        }
    }
}
