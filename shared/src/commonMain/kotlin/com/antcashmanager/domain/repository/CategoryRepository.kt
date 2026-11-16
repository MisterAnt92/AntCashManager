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

    /** Deletes ALL categories (including defaults) belonging to [profileId]. */
    public suspend fun deleteAllCategoriesForProfile(profileId: Long)

    /** Deletes non-default categories belonging to [profileId] (used during backup restore). */
    public suspend fun deleteAllNonDefaultCategoriesForProfile(profileId: Long)

    /** Copies default categories from [sourceProfileId] into [targetProfileId]. Used when creating a new profile. */
    public suspend fun seedDefaultCategoriesForProfile(sourceProfileId: Long, targetProfileId: Long)
}
