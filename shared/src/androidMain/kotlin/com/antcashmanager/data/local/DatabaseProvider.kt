package com.antcashmanager.data.local

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase

public object DatabaseProvider {
    private const val DATABASE_NAME = "antcashmanager.db"

    @Volatile
    private var database: AppDatabase? = null

    public fun getDatabase(context: Context): AppDatabase =
        database ?: synchronized(this) {
            val appContext = context.applicationContext

            Room
                .databaseBuilder(
                    appContext,
                    AppDatabase::class.java,
                    DATABASE_NAME,
                ).setJournalMode(androidx.room.RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                .addMigrations(
                    Migration_9_10(),
                    Migration_10_11(),
                    Migration_11_12(),
                    Migration_12_13(),
                    Migration_13_14(),
                    Migration_14_15(),
                ).addCallback(object : androidx.room.RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        ensureDefaultProfileAndCleanOrphans(db)
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        ensureDefaultProfileAndCleanOrphans(db)
                    }
                })
                .build()
                .also {
                    database = it
                }
        }
}
