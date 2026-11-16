package com.antcashmanager.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class DatabaseMigrationTest {

    private fun createTableInfoCursor(hasProfileId: Boolean): android.database.Cursor {
        val cursor: android.database.Cursor = mockk()
        every { cursor.getColumnIndex("name") } returns 0
        if (hasProfileId) {
            every { cursor.moveToNext() } returnsMany listOf(true, false)
            every { cursor.getString(0) } returns "profile_id"
        } else {
            every { cursor.moveToNext() } returnsMany listOf(true, false)
            every { cursor.getString(0) } returns "other_column"
        }
        every { cursor.close() } returns Unit
        return cursor
    }

    @Test
    fun ensureDefaultProfileAndCleanOrphans_shouldInsertDefaultProfileAndRepairOrphans_whenTableAndColumnExist() {
        val masterCursor: android.database.Cursor = mockk()
        every { masterCursor.moveToFirst() } returns true
        every { masterCursor.close() } returns Unit

        val db: SupportSQLiteDatabase = mockk(relaxed = true)
        every { db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='profiles'") } returns masterCursor
        every { db.query(match<String> { it.contains("PRAGMA table_info") }) } answers { createTableInfoCursor(true) }

        ensureDefaultProfileAndCleanOrphans(db)

        verify {
            db.execSQL(match { it.contains("INSERT OR IGNORE INTO profiles") })
            db.execSQL("UPDATE transactions SET profile_id = 1 WHERE profile_id IS NULL OR profile_id NOT IN (SELECT id FROM profiles)")
            db.execSQL("UPDATE categories SET profile_id = 1 WHERE profile_id IS NULL OR profile_id NOT IN (SELECT id FROM profiles)")
        }
    }

    @Test
    fun ensureDefaultProfileAndCleanOrphans_shouldSkipUpdate_whenProfileIdColumnDoesNotExist() {
        val masterCursor: android.database.Cursor = mockk()
        every { masterCursor.moveToFirst() } returns true
        every { masterCursor.close() } returns Unit

        val db: SupportSQLiteDatabase = mockk(relaxed = true)
        every { db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='profiles'") } returns masterCursor
        every { db.query(match<String> { it.contains("PRAGMA table_info") }) } answers { createTableInfoCursor(false) }

        ensureDefaultProfileAndCleanOrphans(db)

        verify {
            db.execSQL(match { it.contains("INSERT OR IGNORE INTO profiles") })
        }
        verify(exactly = 0) {
            db.execSQL(match { it.contains("UPDATE transactions SET profile_id") })
            db.execSQL(match { it.contains("UPDATE categories SET profile_id") })
        }
    }

    @Test
    fun ensureDefaultProfileAndCleanOrphans_shouldDoNothing_whenProfilesTableDoesNotExist() {
        val masterCursor: android.database.Cursor = mockk()
        every { masterCursor.moveToFirst() } returns false
        every { masterCursor.close() } returns Unit

        val db: SupportSQLiteDatabase = mockk(relaxed = true)
        every { db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='profiles'") } returns masterCursor

        ensureDefaultProfileAndCleanOrphans(db)

        verify(exactly = 0) {
            db.execSQL(match { it.contains("INSERT OR IGNORE INTO profiles") })
        }
    }

    @Test
    fun migration_10_11_shouldNotCrash_whenEnsureCalledBeforeProfileIdColumnExists() {
        // Regression test for: no such column: profile_id — the early ensureDefaultProfileAndCleanOrphans
        // call in Migration_10_11 fires before profile_id is added to transactions/categories.
        // With hasColumn guards in place that call must silently skip the UPDATE.
        val masterCursor: android.database.Cursor = mockk()
        every { masterCursor.moveToFirst() } returns true
        every { masterCursor.close() } returns Unit

        val db: SupportSQLiteDatabase = mockk(relaxed = true)
        every { db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='profiles'") } returns masterCursor

        // PRAGMA table_info is queried twice per ensureDefaultProfileAndCleanOrphans call
        // (once for transactions, once for categories). The first two calls come BEFORE the
        // rename, so profile_id doesn't exist yet; calls 3+ come AFTER the rename.
        var pragmaCallCount = 0
        every { db.query(match<String> { it.contains("PRAGMA table_info") }) } answers {
            pragmaCallCount++
            createTableInfoCursor(pragmaCallCount > 2)
        }

        // Must not throw SQLiteException: no such column: profile_id
        Migration_10_11().migrate(db)

        // The late ensureDefaultProfileAndCleanOrphans call (after rename) must run the UPDATE
        verify(atLeast = 1) {
            db.execSQL(match { it.contains("UPDATE transactions SET profile_id") })
        }
    }

    @Test
    fun migration_12_13_shouldDeduplicateProfilesBeforeUniqueIndex() {
        val executedSqls = mutableListOf<String>()

        val masterCursor: android.database.Cursor = mockk()
        every { masterCursor.moveToFirst() } returns true
        every { masterCursor.close() } returns Unit

        val db: SupportSQLiteDatabase = mockk(relaxed = true)
        every { db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='profiles'") } returns masterCursor
        every { db.query(match<String> { it.contains("PRAGMA table_info") }) } answers { createTableInfoCursor(true) }
        every { db.execSQL(capture(executedSqls)) } returns Unit

        Migration_12_13().migrate(db)

        val dedupIndex = executedSqls.indexOfFirst {
            it.contains("DELETE FROM") && it.contains("profiles") && it.contains("MIN")
        }
        val uniqueIndexIndex = executedSqls.indexOfFirst {
            it.contains("CREATE UNIQUE INDEX") && it.contains("profiles_uuid")
        }
        assert(dedupIndex >= 0) { "Dedup DELETE not found in executed SQLs" }
        assert(uniqueIndexIndex >= 0) { "UNIQUE INDEX on profiles_uuid not found" }
        assert(dedupIndex < uniqueIndexIndex) {
            "Dedup DELETE must precede CREATE UNIQUE INDEX (dedup=$dedupIndex, uniqueIdx=$uniqueIndexIndex)"
        }
    }

    @Test
    fun migration_12_13_shouldRebuildProfilesBeforeTransactions_toAvoidFkDropConstraint() {
        val executedSqls = mutableListOf<String>()

        val masterCursor: android.database.Cursor = mockk()
        every { masterCursor.moveToFirst() } returns true
        every { masterCursor.close() } returns Unit

        val db: SupportSQLiteDatabase = mockk(relaxed = true)
        every { db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='profiles'") } returns masterCursor
        every { db.query(match<String> { it.contains("PRAGMA table_info") }) } answers { createTableInfoCursor(true) }
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
