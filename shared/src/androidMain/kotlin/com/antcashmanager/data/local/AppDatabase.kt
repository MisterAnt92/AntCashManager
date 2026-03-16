package com.antcashmanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.antcashmanager.data.local.dao.TransactionDao
import com.antcashmanager.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}
