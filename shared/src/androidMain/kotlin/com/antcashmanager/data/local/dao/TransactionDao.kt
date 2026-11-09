package com.antcashmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.antcashmanager.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE profile_id = :profileId ORDER BY timestamp DESC")
    public fun getAllTransactions(profileId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions WHERE profile_id = :profileId")
    public suspend fun getCount(profileId: Long): Int

    @Query(
        "SELECT * FROM transactions WHERE profile_id = :profileId " +
            "ORDER BY timestamp DESC LIMIT :limit OFFSET :offset",
    )
    public fun getTransactionsPaginated(
        profileId: Long,
        limit: Int,
        offset: Int,
    ): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions WHERE profile_id = :profileId AND category = :category " +
            "ORDER BY timestamp DESC LIMIT :limit OFFSET :offset",
    )
    public fun getTransactionsByCategory(
        profileId: Long,
        category: String,
        limit: Int = 100,
        offset: Int = 0,
    ): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions WHERE profile_id = :profileId AND category = :category " +
            "AND timestamp BETWEEN :from AND :to ORDER BY timestamp DESC LIMIT :limit OFFSET :offset",
    )
    public fun getTransactionsByCategoryAndDateRange(
        profileId: Long,
        category: String,
        from: Long,
        to: Long,
        limit: Int = 100,
        offset: Int = 0,
    ): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions WHERE profile_id = :profileId " +
            "AND (title LIKE :query OR payee LIKE :query OR notes LIKE :query) " +
            "ORDER BY timestamp DESC LIMIT :limit OFFSET :offset",
    )
    public fun searchTransactions(
        profileId: Long,
        query: String,
        limit: Int = 100,
        offset: Int = 0,
    ): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    public suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long>

    @Update
    public suspend fun updateTransaction(transaction: TransactionEntity)

    @Update
    public suspend fun updateTransactions(transactions: List<TransactionEntity>)

    @Delete
    public suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE profile_id = :profileId")
    public suspend fun deleteAllTransactions(profileId: Long)

    @Query(
        "SELECT * FROM transactions WHERE profile_id = :profileId " +
            "AND timestamp BETWEEN :from AND :to ORDER BY timestamp DESC",
    )
    public fun getTransactionsByDateRange(
        profileId: Long,
        from: Long,
        to: Long,
    ): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions WHERE profile_id = :profileId AND is_recurring = 1 " +
            "ORDER BY timestamp DESC",
    )
    public fun getRecurringTransactions(profileId: Long): Flow<List<TransactionEntity>>

    @Transaction
    @Query(
        "UPDATE transactions SET category = :newCategoryName, category_icon = :icon, category_color = :color " +
            "WHERE profile_id = :profileId AND category = :oldCategoryName",
    )
    public suspend fun renameCategory(
        profileId: Long,
        oldCategoryName: String,
        newCategoryName: String,
        icon: String,
        color: Long,
    )

    @Query(
        "SELECT DISTINCT title FROM transactions " +
            "WHERE profile_id = :profileId AND title != '' AND timestamp >= :since " +
            "ORDER BY timestamp DESC LIMIT 20",
    )
    public fun getDistinctTitles(profileId: Long, since: Long): Flow<List<String>>

    @Query(
        "SELECT DISTINCT payee FROM transactions " +
            "WHERE profile_id = :profileId AND payee != '' AND timestamp >= :since " +
            "ORDER BY timestamp DESC LIMIT 20",
    )
    public fun getDistinctPayees(profileId: Long, since: Long): Flow<List<String>>

    @Query(
        "SELECT DISTINCT notes FROM transactions " +
            "WHERE profile_id = :profileId AND notes != '' AND timestamp >= :since " +
            "ORDER BY timestamp DESC LIMIT 20",
    )
    public fun getDistinctNotes(profileId: Long, since: Long): Flow<List<String>>

    @Query(
        "SELECT DISTINCT location FROM transactions " +
            "WHERE profile_id = :profileId AND location != '' AND timestamp >= :since " +
            "ORDER BY timestamp DESC LIMIT 20",
    )
    public fun getDistinctLocations(profileId: Long, since: Long): Flow<List<String>>

    @Query(
        "SELECT DISTINCT tags FROM transactions " +
            "WHERE profile_id = :profileId AND tags != '' AND timestamp >= :since " +
            "ORDER BY timestamp DESC LIMIT 20",
    )
    public fun getDistinctTags(profileId: Long, since: Long): Flow<List<String>>

    @Query(
        """
        SELECT DISTINCT title, payee, notes, location, tags
        FROM transactions
        WHERE profile_id = :profileId AND timestamp >= :since
            AND (title != '' OR payee != '' OR notes != '' OR location != '' OR tags != '')
        ORDER BY timestamp DESC
        LIMIT 100
    """,
    )
    public suspend fun getSuggestions(profileId: Long, since: Long): List<SuggestionRow>
}

public data class SuggestionRow(
    val title: String,
    val payee: String,
    val notes: String,
    val location: String,
    val tags: String,
)
