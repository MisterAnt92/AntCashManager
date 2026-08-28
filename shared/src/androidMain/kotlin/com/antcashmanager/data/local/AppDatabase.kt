package com.antcashmanager.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.antcashmanager.data.local.dao.CategoryDao
import com.antcashmanager.data.local.dao.TransactionDao
import com.antcashmanager.data.local.entity.CategoryEntity
import com.antcashmanager.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class],
    version = 10,
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
        // NOTE: 9→10 is handled by manual Migration_9_10 in DatabaseProvider
        // (remove AutoMigration here to avoid conflicts with the manual migration)
    ],
)
public abstract class AppDatabase : RoomDatabase() {
    public abstract fun transactionDao(): TransactionDao
    public abstract fun categoryDao(): CategoryDao
}
