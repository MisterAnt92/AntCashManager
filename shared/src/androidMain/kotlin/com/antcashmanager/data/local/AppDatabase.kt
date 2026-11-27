package com.antcashmanager.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.antcashmanager.data.local.dao.CategoryBudgetDao
import com.antcashmanager.data.local.dao.CategoryDao
import com.antcashmanager.data.local.dao.ProfileDao
import com.antcashmanager.data.local.dao.TransactionDao
import com.antcashmanager.data.local.entity.CategoryBudgetEntity
import com.antcashmanager.data.local.entity.CategoryEntity
import com.antcashmanager.data.local.entity.ProfileEntity
import com.antcashmanager.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class, ProfileEntity::class, CategoryBudgetEntity::class],
    version = 16,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
        // NOTE: 9→10, 10→11, 11→12, 12→13, 13→14, 14→15 are handled by manual migrations in DatabaseProvider
        // (remove AutoMigration here to avoid conflicts with the manual migrations and ensure default profile seeding)
    ],
)
public abstract class AppDatabase : RoomDatabase() {
    public abstract fun transactionDao(): TransactionDao

    public abstract fun categoryDao(): CategoryDao

    public abstract fun profileDao(): ProfileDao

    public abstract fun categoryBudgetDao(): CategoryBudgetDao
}
