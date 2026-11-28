package com.antcashmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.antcashmanager.data.local.entity.CategoryBudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface CategoryBudgetDao {
    @Query("SELECT * FROM category_budgets")
    public fun getAll(): Flow<List<CategoryBudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun upsert(entity: CategoryBudgetEntity)

    @Query("DELETE FROM category_budgets WHERE categoryId = :categoryId")
    public suspend fun deleteById(categoryId: Long)
}
