package com.antcashmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.antcashmanager.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface CategoryDao {
    @Query("SELECT * FROM categories WHERE profile_id = :profileId ORDER BY is_hidden ASC, sort_order ASC")
    public fun getAllCategories(profileId: Long): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    public suspend fun getCategoryById(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories WHERE profile_id = :profileId AND name = :name LIMIT 1")
    public suspend fun getCategoryByName(profileId: Long, name: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    public suspend fun updateCategory(category: CategoryEntity)

    @Delete
    public suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE profile_id = :profileId")
    public suspend fun deleteAllCategories(profileId: Long)

    @Query("DELETE FROM categories WHERE profile_id = :profileId AND is_default = 0")
    public suspend fun deleteAllNonDefaultCategories(profileId: Long)

    @Query(
        "SELECT * FROM categories WHERE profile_id = :profileId AND type = :type " +
            "ORDER BY is_hidden ASC, sort_order ASC",
    )
    public fun getCategoriesByType(profileId: Long, type: String): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories WHERE profile_id = :profileId AND is_default = 1")
    public suspend fun getDefaultCategoryCount(profileId: Long): Int

    /** Returns all default categories of any profile — used by CreateProfileUseCase for seeding. */
    @Query("SELECT * FROM categories WHERE profile_id = :sourceProfileId AND is_default = 1")
    public suspend fun getDefaultCategoriesForProfile(sourceProfileId: Long): List<CategoryEntity>
}
