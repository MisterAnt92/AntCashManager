package com.antcashmanager.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 9 to 10.
 * This migration also fixes an issue where version 9 might have been created without
 * the 'meal_voucher_difference' column due to an incomplete manual migration.
 */
public class Migration_9_10 : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Check if meal_voucher_difference column exists in transactions table
        val cursor = database.query("PRAGMA table_info(transactions)")
        var columnExists = false
        val nameIndex = cursor.getColumnIndex("name")
        if (nameIndex != -1) {
            while (cursor.moveToNext()) {
                if (cursor.getString(nameIndex) == "meal_voucher_difference") {
                    columnExists = true
                    break
                }
            }
        }
        cursor.close()

        // Add the column if it doesn't exist
        if (!columnExists) {
            database.execSQL("ALTER TABLE `transactions` ADD COLUMN `meal_voucher_difference` REAL NOT NULL DEFAULT 0.0")
        }

        // The auto-migration mechanism will handle any other potential changes,
        // but this manual migration ensures the integrity of the critical missing field.
    }
}
