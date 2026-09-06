package com.antcashmanager.data.local

import android.content.Context
import androidx.room.Room

public object DatabaseProvider {
    private const val DATABASE_NAME = "antcashmanager.db"
    @Volatile
    private var database: AppDatabase? = null

    public fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            val appContext = context.applicationContext

            Room.databaseBuilder(
                appContext,
                AppDatabase::class.java,
                DATABASE_NAME,
            )
                .addMigrations(Migration_9_10())
                .build().also {
                    database = it
                }
        }
    }
}
