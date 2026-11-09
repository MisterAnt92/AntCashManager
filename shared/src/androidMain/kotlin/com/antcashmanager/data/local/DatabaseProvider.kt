package com.antcashmanager.data.local

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.UUID

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
                ).addMigrations(Migration_9_10())
                .addCallback(object : androidx.room.RoomDatabase.Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        // Seed Default profile on first open (new install or after 10→11 migration)
                        val uuid = UUID.randomUUID().toString()
                        val timestamp = System.currentTimeMillis()
                        db.execSQL(
                            "INSERT OR IGNORE INTO profiles " +
                                "(id, name, uuid, created_at, is_active, color_argb, avatar_emoji) " +
                                "VALUES (1, 'Default', '$uuid', $timestamp, 1, 4284955319, '')",
                        )
                    }
                })
                .build()
                .also {
                    database = it
                }
        }
}
