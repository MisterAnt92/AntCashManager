package com.antcashmanager.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration_8_9 : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create indices on transactions table
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_timestamp` ON `transactions` (`timestamp`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_is_recurring` ON `transactions` (`is_recurring`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_category` ON `transactions` (`category`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_type` ON `transactions` (`type`)")

        // Create indices on categories table
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_type` ON `categories` (`type`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_name` ON `categories` (`name`)")
    }
}
