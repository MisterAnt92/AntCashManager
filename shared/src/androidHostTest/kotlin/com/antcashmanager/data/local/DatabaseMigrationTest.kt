package com.antcashmanager.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class DatabaseMigrationTest {

    @Test
    fun ensureDefaultProfileAndCleanOrphans_shouldInsertDefaultProfileAndRepairOrphans_whenTableExists() {
        val cursor: android.database.Cursor = mockk()
        every { cursor.moveToFirst() } returns true
        every { cursor.close() } returns Unit

        val db: SupportSQLiteDatabase = mockk(relaxed = true)
        every { db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='profiles'") } returns cursor

        ensureDefaultProfileAndCleanOrphans(db)

        verify {
            db.execSQL(match { it.contains("INSERT OR IGNORE INTO profiles") })
            db.execSQL("UPDATE transactions SET profile_id = 1 WHERE profile_id IS NULL OR profile_id NOT IN (SELECT id FROM profiles)")
            db.execSQL("UPDATE categories SET profile_id = 1 WHERE profile_id IS NULL OR profile_id NOT IN (SELECT id FROM profiles)")
        }
    }

    @Test
    fun ensureDefaultProfileAndCleanOrphans_shouldDoNothing_whenProfilesTableDoesNotExist() {
        val cursor: android.database.Cursor = mockk()
        every { cursor.moveToFirst() } returns false
        every { cursor.close() } returns Unit

        val db: SupportSQLiteDatabase = mockk(relaxed = true)
        every { db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='profiles'") } returns cursor

        ensureDefaultProfileAndCleanOrphans(db)

        verify(exactly = 0) {
            db.execSQL(match { it.contains("INSERT OR IGNORE INTO profiles") })
        }
    }

    @Test
    fun ensureDefaultProfileAndCleanOrphans_shouldIncludeNullGuard_inUpdateStatement() {
        // Regression test: NULL NOT IN (...) = UNKNOWN in SQL, so rows with NULL profile_id
        // were silently skipped before this fix, causing FK violations on migration.
        val cursor: android.database.Cursor = mockk()
        every { cursor.moveToFirst() } returns true
        every { cursor.close() } returns Unit

        val db: SupportSQLiteDatabase = mockk(relaxed = true)
        every { db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='profiles'") } returns cursor

        ensureDefaultProfileAndCleanOrphans(db)

        verify {
            db.execSQL(match { sql ->
                sql.contains("profile_id IS NULL OR profile_id NOT IN") &&
                    sql.contains("transactions")
            })
            db.execSQL(match { sql ->
                sql.contains("profile_id IS NULL OR profile_id NOT IN") &&
                    sql.contains("categories")
            })
        }
    }

    @Test
    fun migration_12_13_shouldRebuildProfilesBeforeTransactions_toAvoidFkDropConstraint() {
        // Regression test: Migration_12_13 previously rebuilt transactions (adding FK→profiles)
        // then dropped profiles — with FK ON that DROP would fail. Profiles must be rebuilt first.
        val executedSqls = mutableListOf<String>()

        val cursor: android.database.Cursor = mockk()
        every { cursor.moveToFirst() } returns true
        every { cursor.close() } returns Unit

        val db: SupportSQLiteDatabase = mockk(relaxed = true)
        every { db.query(any<String>()) } returns cursor
        every { db.execSQL(capture(executedSqls)) } returns Unit

        Migration_12_13().migrate(db)

        val dropProfilesIndex = executedSqls.indexOfFirst { it == "DROP TABLE `profiles`" }
        val createNewTransactionsIndex = executedSqls.indexOfFirst {
            it.contains("CREATE TABLE IF NOT EXISTS `_new_transactions`") &&
                it.contains("FOREIGN KEY(`profile_id`)")
        }
        assert(dropProfilesIndex >= 0) { "DROP TABLE profiles not found" }
        assert(createNewTransactionsIndex >= 0) { "_new_transactions with FK not found" }
        assert(dropProfilesIndex < createNewTransactionsIndex) {
            "profiles must be dropped BEFORE _new_transactions with FK is created " +
                "(drop at $dropProfilesIndex, fk-transactions at $createNewTransactionsIndex)"
        }
    }
}
