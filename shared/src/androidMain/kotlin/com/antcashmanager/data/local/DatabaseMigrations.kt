package com.antcashmanager.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.UUID

public fun ensureDefaultProfileAndCleanOrphans(db: SupportSQLiteDatabase) {
    val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='profiles'")
    val tableExists = cursor.moveToFirst()
    cursor.close()

    if (tableExists) {
        val uuid = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        db.execSQL(
            "INSERT OR IGNORE INTO profiles " +
                "(id, name, uuid, created_at, is_active, color_argb, avatar_emoji) " +
                "VALUES (1, 'Default', '$uuid', $timestamp, 1, 4284955319, '')",
        )
        if (hasColumn(db, "transactions", "profile_id")) {
            db.execSQL("UPDATE transactions SET profile_id = 1 WHERE profile_id IS NULL OR profile_id NOT IN (SELECT id FROM profiles)")
        }
        if (hasColumn(db, "categories", "profile_id")) {
            db.execSQL("UPDATE categories SET profile_id = 1 WHERE profile_id IS NULL OR profile_id NOT IN (SELECT id FROM profiles)")
        }
    }
}

private fun hasColumn(db: SupportSQLiteDatabase, tableName: String, columnName: String): Boolean {
    val cursor = db.query("PRAGMA table_info(`$tableName`)")
    val nameIndex = cursor.getColumnIndex("name")
    var exists = false
    if (nameIndex != -1) {
        while (cursor.moveToNext()) {
            if (cursor.getString(nameIndex) == columnName) {
                exists = true
                break
            }
        }
    }
    cursor.close()
    return exists
}

public class Migration_10_11 : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `profiles` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, `uuid` TEXT NOT NULL, " +
                "`created_at` INTEGER NOT NULL DEFAULT 0, " +
                "`is_active` INTEGER NOT NULL DEFAULT 0, " +
                "`color_argb` INTEGER NOT NULL DEFAULT 4284955319, " +
                "`avatar_emoji` TEXT NOT NULL DEFAULT '')",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_profiles_is_active` ON `profiles` (`is_active`)")

        ensureDefaultProfileAndCleanOrphans(db)

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `_new_transactions` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, " +
                "`amount` REAL NOT NULL, `category` TEXT NOT NULL, `type` TEXT NOT NULL, " +
                "`timestamp` INTEGER NOT NULL, `notes` TEXT NOT NULL DEFAULT '', " +
                "`payee` TEXT NOT NULL DEFAULT '', `location` TEXT NOT NULL DEFAULT '', " +
                "`is_recurring` INTEGER NOT NULL DEFAULT 0, `tags` TEXT NOT NULL DEFAULT '', " +
                "`recurrence_interval` TEXT NOT NULL DEFAULT '', " +
                "`payment_type` TEXT NOT NULL DEFAULT 'ELECTRONIC', " +
                "`meal_voucher_count` INTEGER NOT NULL DEFAULT 0, " +
                "`meal_voucher_difference` REAL NOT NULL DEFAULT 0.0, " +
                "`category_icon` TEXT NOT NULL DEFAULT '', " +
                "`category_color` INTEGER NOT NULL DEFAULT 9474862, " +
                "`profile_id` INTEGER NOT NULL DEFAULT 1)",
        )
        db.execSQL(
            "INSERT INTO `_new_transactions` " +
                "(`id`,`title`,`amount`,`category`,`type`,`timestamp`,`notes`,`payee`,`location`," +
                "`is_recurring`,`tags`,`recurrence_interval`,`payment_type`,`meal_voucher_count`," +
                "`meal_voucher_difference`,`category_icon`,`category_color`) " +
                "SELECT `id`,`title`,`amount`,`category`,`type`,`timestamp`,`notes`,`payee`,`location`," +
                "`is_recurring`,`tags`,`recurrence_interval`,`payment_type`,`meal_voucher_count`," +
                "`meal_voucher_difference`,`category_icon`,`category_color` FROM `transactions`",
        )
        db.execSQL("DROP TABLE `transactions`")
        db.execSQL("ALTER TABLE `_new_transactions` RENAME TO `transactions`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_timestamp` ON `transactions` (`timestamp`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_is_recurring` ON `transactions` (`is_recurring`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_category` ON `transactions` (`category`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_type` ON `transactions` (`type`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_profile_id` ON `transactions` (`profile_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_profile_id_timestamp` ON `transactions` (`profile_id`, `timestamp`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `_new_categories` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, " +
                "`icon` TEXT NOT NULL, `color` INTEGER NOT NULL, `type` TEXT NOT NULL DEFAULT 'EXPENSE', " +
                "`is_default` INTEGER NOT NULL DEFAULT 0, `sort_order` INTEGER NOT NULL DEFAULT 0, " +
                "`is_hidden` INTEGER NOT NULL DEFAULT 0, `profile_id` INTEGER NOT NULL DEFAULT 1)",
        )
        db.execSQL(
            "INSERT INTO `_new_categories` " +
                "(`id`,`name`,`icon`,`color`,`type`,`is_default`,`sort_order`,`is_hidden`) " +
                "SELECT `id`,`name`,`icon`,`color`,`type`,`is_default`,`sort_order`,`is_hidden` FROM `categories`",
        )
        db.execSQL("DROP TABLE `categories`")
        db.execSQL("ALTER TABLE `_new_categories` RENAME TO `categories`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_type` ON `categories` (`type`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_name` ON `categories` (`name`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_profile_id` ON `categories` (`profile_id`)")

        ensureDefaultProfileAndCleanOrphans(db)
    }
}

public class Migration_11_12 : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `profiles` ADD COLUMN `photo_path` TEXT DEFAULT ''")
        ensureDefaultProfileAndCleanOrphans(db)
    }
}

public class Migration_12_13 : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        ensureDefaultProfileAndCleanOrphans(db)

        // Rebuild profiles FIRST so that DROP TABLE profiles happens before transactions
        // acquires its FK constraint — avoids a potential "FOREIGN KEY constraint failed"
        // when FK pragma is ON on the writer connection.
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `_new_profiles` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, " +
                "`uuid` TEXT NOT NULL, `created_at` INTEGER NOT NULL DEFAULT 0, " +
                "`is_active` INTEGER NOT NULL DEFAULT 0, `color_argb` INTEGER NOT NULL DEFAULT 4284955319, " +
                "`avatar_emoji` TEXT NOT NULL DEFAULT '', `photo_path` TEXT DEFAULT '')",
        )
        db.execSQL(
            "INSERT INTO `_new_profiles` " +
                "(`id`,`name`,`uuid`,`created_at`,`is_active`,`color_argb`,`avatar_emoji`,`photo_path`) " +
                "SELECT `id`,`name`,`uuid`,`created_at`,`is_active`,`color_argb`,`avatar_emoji`,`photo_path` FROM `profiles`",
        )
        db.execSQL("DROP TABLE `profiles`")
        db.execSQL("ALTER TABLE `_new_profiles` RENAME TO `profiles`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_profiles_is_active` ON `profiles` (`is_active`)")
        // Defensive dedup: remove any profiles with a duplicate uuid keeping the lowest id.
        // Normally impossible (uuid is random), but guards against corrupt backup-restore paths
        // that could otherwise crash the following CREATE UNIQUE INDEX with UNIQUE constraint failed.
        db.execSQL(
            "DELETE FROM `profiles` WHERE `id` NOT IN " +
                "(SELECT MIN(`id`) FROM `profiles` GROUP BY `uuid`)",
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_profiles_uuid` ON `profiles` (`uuid`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `_new_transactions` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, " +
                "`amount` REAL NOT NULL, `category` TEXT NOT NULL, `type` TEXT NOT NULL, " +
                "`timestamp` INTEGER NOT NULL, `notes` TEXT NOT NULL DEFAULT '', " +
                "`payee` TEXT NOT NULL DEFAULT '', `location` TEXT NOT NULL DEFAULT '', " +
                "`is_recurring` INTEGER NOT NULL DEFAULT 0, `tags` TEXT NOT NULL DEFAULT '', " +
                "`recurrence_interval` TEXT NOT NULL DEFAULT '', " +
                "`payment_type` TEXT NOT NULL DEFAULT 'ELECTRONIC', " +
                "`meal_voucher_count` INTEGER NOT NULL DEFAULT 0, " +
                "`meal_voucher_difference` REAL NOT NULL DEFAULT 0.0, " +
                "`category_icon` TEXT NOT NULL DEFAULT '', " +
                "`category_color` INTEGER NOT NULL DEFAULT 9474862, " +
                "`profile_id` INTEGER NOT NULL DEFAULT 1, " +
                "FOREIGN KEY(`profile_id`) REFERENCES `profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
        )
        db.execSQL(
            "INSERT INTO `_new_transactions` " +
                "(`id`,`title`,`amount`,`category`,`type`,`timestamp`,`notes`,`payee`,`location`," +
                "`is_recurring`,`tags`,`recurrence_interval`,`payment_type`,`meal_voucher_count`," +
                "`meal_voucher_difference`,`category_icon`,`category_color`,`profile_id`) " +
                "SELECT `id`,`title`,`amount`,`category`,`type`,`timestamp`,`notes`,`payee`,`location`," +
                "`is_recurring`,`tags`,`recurrence_interval`,`payment_type`,`meal_voucher_count`," +
                "`meal_voucher_difference`,`category_icon`,`category_color`,`profile_id` FROM `transactions`",
        )
        db.execSQL("DROP TABLE `transactions`")
        db.execSQL("ALTER TABLE `_new_transactions` RENAME TO `transactions`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_timestamp` ON `transactions` (`timestamp`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_is_recurring` ON `transactions` (`is_recurring`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_type` ON `transactions` (`type`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_profile_id_timestamp` ON `transactions` (`profile_id`, `timestamp`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_profile_id_category` ON `transactions` (`profile_id`, `category`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `_new_categories` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, " +
                "`icon` TEXT NOT NULL, `color` INTEGER NOT NULL, `type` TEXT NOT NULL DEFAULT 'EXPENSE', " +
                "`is_default` INTEGER NOT NULL DEFAULT 0, `sort_order` INTEGER NOT NULL DEFAULT 0, " +
                "`is_hidden` INTEGER NOT NULL DEFAULT 0, `profile_id` INTEGER NOT NULL DEFAULT 1, " +
                "FOREIGN KEY(`profile_id`) REFERENCES `profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
        )
        db.execSQL(
            "INSERT INTO `_new_categories` " +
                "(`id`,`name`,`icon`,`color`,`type`,`is_default`,`sort_order`,`is_hidden`,`profile_id`) " +
                "SELECT `id`,`name`,`icon`,`color`,`type`,`is_default`,`sort_order`,`is_hidden`,`profile_id` FROM `categories`",
        )
        db.execSQL("DROP TABLE `categories`")
        db.execSQL("ALTER TABLE `_new_categories` RENAME TO `categories`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_type` ON `categories` (`type`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_profile_id_name` ON `categories` (`profile_id`, `name`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_profile_id_type` ON `categories` (`profile_id`, `type`)")

        ensureDefaultProfileAndCleanOrphans(db)
    }
}

public class Migration_13_14 : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        ensureDefaultProfileAndCleanOrphans(db)

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `_new_profiles` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, " +
                "`uuid` TEXT NOT NULL, `created_at` INTEGER NOT NULL DEFAULT 0, " +
                "`is_active` INTEGER NOT NULL DEFAULT 0, `color_argb` INTEGER NOT NULL DEFAULT 4284955319, " +
                "`avatar_emoji` TEXT NOT NULL DEFAULT '', `photo_path` TEXT DEFAULT '', " +
                "`share_transactions` INTEGER NOT NULL DEFAULT 0)",
        )
        db.execSQL(
            "INSERT INTO `_new_profiles` " +
                "(`id`,`name`,`uuid`,`created_at`,`is_active`,`color_argb`,`avatar_emoji`,`photo_path`) " +
                "SELECT `id`,`name`,`uuid`,`created_at`,`is_active`,`color_argb`,`avatar_emoji`,`photo_path` FROM `profiles`",
        )
        db.execSQL("DROP TABLE `profiles`")
        db.execSQL("ALTER TABLE `_new_profiles` RENAME TO `profiles`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_profiles_is_active` ON `profiles` (`is_active`)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_profiles_uuid` ON `profiles` (`uuid`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_profiles_share_transactions` ON `profiles` (`share_transactions`)")

        ensureDefaultProfileAndCleanOrphans(db)
    }
}

public class Migration_14_15 : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        ensureDefaultProfileAndCleanOrphans(db)

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `_new_transactions` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, " +
                "`amount` REAL NOT NULL, `category` TEXT NOT NULL, `type` TEXT NOT NULL, " +
                "`timestamp` INTEGER NOT NULL, `notes` TEXT NOT NULL DEFAULT '', " +
                "`payee` TEXT NOT NULL DEFAULT '', `location` TEXT NOT NULL DEFAULT '', " +
                "`is_recurring` INTEGER NOT NULL DEFAULT 0, `tags` TEXT NOT NULL DEFAULT '', " +
                "`recurrence_interval` TEXT NOT NULL DEFAULT '', " +
                "`payment_type` TEXT NOT NULL DEFAULT 'ELECTRONIC', " +
                "`meal_voucher_count` INTEGER NOT NULL DEFAULT 0, " +
                "`meal_voucher_difference` REAL NOT NULL DEFAULT 0.0, " +
                "`category_icon` TEXT NOT NULL DEFAULT '', " +
                "`category_color` INTEGER NOT NULL DEFAULT 9474862, " +
                "`profile_id` INTEGER NOT NULL DEFAULT 1, " +
                "FOREIGN KEY(`profile_id`) REFERENCES `profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
        )
        db.execSQL(
            "INSERT INTO `_new_transactions` " +
                "(`id`,`title`,`amount`,`category`,`type`,`timestamp`,`notes`,`payee`,`location`," +
                "`is_recurring`,`tags`,`recurrence_interval`,`payment_type`,`meal_voucher_count`," +
                "`meal_voucher_difference`,`category_icon`,`category_color`,`profile_id`) " +
                "SELECT `id`,`title`,`amount`,`category`,`type`,`timestamp`,`notes`,`payee`,`location`," +
                "`is_recurring`,`tags`,`recurrence_interval`,`payment_type`,`meal_voucher_count`," +
                "`meal_voucher_difference`,`category_icon`,`category_color`,`profile_id` FROM `transactions`",
        )
        db.execSQL("DROP TABLE `transactions`")
        db.execSQL("ALTER TABLE `_new_transactions` RENAME TO `transactions`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_profile_id_timestamp` ON `transactions` (`profile_id`, `timestamp`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_profile_id_category` ON `transactions` (`profile_id`, `category`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_profile_id_category_timestamp` ON `transactions` (`profile_id`, `category`, `timestamp`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_profile_id_is_recurring_timestamp` ON `transactions` (`profile_id`, `is_recurring`, `timestamp`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `_new_categories` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, " +
                "`icon` TEXT NOT NULL, `color` INTEGER NOT NULL, `type` TEXT NOT NULL DEFAULT 'EXPENSE', " +
                "`is_default` INTEGER NOT NULL DEFAULT 0, `sort_order` INTEGER NOT NULL DEFAULT 0, " +
                "`is_hidden` INTEGER NOT NULL DEFAULT 0, `profile_id` INTEGER NOT NULL DEFAULT 1, " +
                "FOREIGN KEY(`profile_id`) REFERENCES `profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
        )
        db.execSQL(
            "INSERT INTO `_new_categories` " +
                "(`id`,`name`,`icon`,`color`,`type`,`is_default`,`sort_order`,`is_hidden`,`profile_id`) " +
                "SELECT `id`,`name`,`icon`,`color`,`type`,`is_default`,`sort_order`,`is_hidden`,`profile_id` FROM `categories`",
        )
        db.execSQL("DROP TABLE `categories`")
        db.execSQL("ALTER TABLE `_new_categories` RENAME TO `categories`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_profile_id_name` ON `categories` (`profile_id`, `name`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_profile_id_type` ON `categories` (`profile_id`, `type`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_profile_id_is_hidden_sort_order` ON `categories` (`profile_id`, `is_hidden`, `sort_order`)")

        ensureDefaultProfileAndCleanOrphans(db)
    }
}

public class Migration_15_16 : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `category_budgets` " +
                "(`categoryId` INTEGER NOT NULL, `monthlyLimit` REAL NOT NULL, " +
                "PRIMARY KEY(`categoryId`))",
        )
    }
}
