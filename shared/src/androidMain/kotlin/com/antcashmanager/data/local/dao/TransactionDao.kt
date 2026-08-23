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
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    public fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions")
    public suspend fun getCount(): Int

    /**
     * Fetch transactions with pagination.
     * @param limit Number of transactions per page (recommended: 50-200)
     * @param offset Offset from start (0 for first page)
     * @return Flow of paginated transaction results
     */
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    public fun getTransactionsPaginated(limit: Int, offset: Int): Flow<List<TransactionEntity>>

    /**
     * Filter transactions by category with pagination.
     * Pushes filtering to database level for efficiency.
     */
    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    public fun getTransactionsByCategory(category: String, limit: Int = 100, offset: Int = 0): Flow<List<TransactionEntity>>

    /**
     * Filter transactions by date range and category with pagination.
     * Optimized for charts and reports.
     */
    @Query("SELECT * FROM transactions WHERE category = :category AND timestamp BETWEEN :from AND :to ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    public fun getTransactionsByCategoryAndDateRange(
        category: String,
        from: Long,
        to: Long,
        limit: Int = 100,
        offset: Int = 0
    ): Flow<List<TransactionEntity>>

    /**
     * Search transactions by title/payee/notes with pagination.
     * Case-insensitive search.
     */
    @Query("SELECT * FROM transactions WHERE (title LIKE :query OR payee LIKE :query OR notes LIKE :query) ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    public fun searchTransactions(query: String, limit: Int = 100, offset: Int = 0): Flow<List<TransactionEntity>>

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

    @Query("DELETE FROM transactions")
    public suspend fun deleteAllTransactions()

    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    public fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE is_recurring = 1 ORDER BY timestamp DESC")
    public fun getRecurringTransactions(): Flow<List<TransactionEntity>>

    @Transaction
    @Query(
        "UPDATE transactions SET category = :newCategoryName, category_icon = :icon, category_color = :color " +
                "WHERE category = :oldCategoryName",
    )
    public suspend fun renameCategory(
        oldCategoryName: String,
        newCategoryName: String,
        icon: String,
        color: Long
    )

    // Query per suggerimenti transazioni. :since filtra le transazioni con timestamp >= since.
    @Query("SELECT DISTINCT title FROM transactions WHERE title != '' AND timestamp >= :since ORDER BY timestamp DESC LIMIT 20")
    public fun getDistinctTitles(since: Long): Flow<List<String>>

    @Query("SELECT DISTINCT payee FROM transactions WHERE payee != '' AND timestamp >= :since ORDER BY timestamp DESC LIMIT 20")
    public fun getDistinctPayees(since: Long): Flow<List<String>>

    @Query("SELECT DISTINCT notes FROM transactions WHERE notes != '' AND timestamp >= :since ORDER BY timestamp DESC LIMIT 20")
    public fun getDistinctNotes(since: Long): Flow<List<String>>

    @Query("SELECT DISTINCT location FROM transactions WHERE location != '' AND timestamp >= :since ORDER BY timestamp DESC LIMIT 20")
    public fun getDistinctLocations(since: Long): Flow<List<String>>

    @Query("SELECT DISTINCT tags FROM transactions WHERE tags != '' AND timestamp >= :since ORDER BY timestamp DESC LIMIT 20")
    public fun getDistinctTags(since: Long): Flow<List<String>>

    // Unified suggestions query: fetch all distinct fields in single query
    @Query(
        """
        SELECT DISTINCT title, payee, notes, location, tags
        FROM transactions
        WHERE timestamp >= :since AND (title != '' OR payee != '' OR notes != '' OR location != '' OR tags != '')
        ORDER BY timestamp DESC
        LIMIT 100
    """
    )
    public suspend fun getSuggestions(since: Long): List<SuggestionRow>
}

public data class SuggestionRow(
    val title: String,
    val payee: String,
    val notes: String,
    val location: String,
    val tags: String
)
