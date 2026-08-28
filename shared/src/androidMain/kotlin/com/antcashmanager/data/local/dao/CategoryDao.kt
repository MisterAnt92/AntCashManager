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
    @Query("SELECT * FROM categories ORDER BY is_hidden ASC, sort_order ASC")
    public fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    public suspend fun getCategoryById(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    public suspend fun getCategoryByName(name: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    public suspend fun updateCategory(category: CategoryEntity)

    @Delete
    public suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories")
    public suspend fun deleteAllCategories()

    @Query("DELETE FROM categories WHERE is_default = 0")
    public suspend fun deleteAllNonDefaultCategories()

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY is_hidden ASC, sort_order ASC")
    public fun getCategoriesByType(type: String): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories WHERE is_default = 1")
    public suspend fun getDefaultCategoryCount(): Int
}
