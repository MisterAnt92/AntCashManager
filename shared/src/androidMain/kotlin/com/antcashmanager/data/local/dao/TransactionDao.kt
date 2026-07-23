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
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getCount(): Int

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long>

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransactions(transactions: List<TransactionEntity>)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE is_recurring = 1 ORDER BY timestamp DESC")
    fun getRecurringTransactions(): Flow<List<TransactionEntity>>

    @Transaction
    @Query(
        "UPDATE transactions SET category = :newCategoryName, category_icon = :icon, category_color = :color " +
                "WHERE category = :oldCategoryName",
    )
    suspend fun renameCategory(
        oldCategoryName: String,
        newCategoryName: String,
        icon: String,
        color: Long
    )

    // Query per suggerimenti transazioni. :since filtra le transazioni con timestamp >= since.
    @Query("SELECT DISTINCT title FROM transactions WHERE title != '' AND timestamp >= :since ORDER BY timestamp DESC LIMIT 20")
    fun getDistinctTitles(since: Long): Flow<List<String>>

    @Query("SELECT DISTINCT payee FROM transactions WHERE payee != '' AND timestamp >= :since ORDER BY timestamp DESC LIMIT 20")
    fun getDistinctPayees(since: Long): Flow<List<String>>

    @Query("SELECT DISTINCT notes FROM transactions WHERE notes != '' AND timestamp >= :since ORDER BY timestamp DESC LIMIT 20")
    fun getDistinctNotes(since: Long): Flow<List<String>>

    @Query("SELECT DISTINCT location FROM transactions WHERE location != '' AND timestamp >= :since ORDER BY timestamp DESC LIMIT 20")
    fun getDistinctLocations(since: Long): Flow<List<String>>

    @Query("SELECT DISTINCT tags FROM transactions WHERE tags != '' AND timestamp >= :since ORDER BY timestamp DESC LIMIT 20")
    fun getDistinctTags(since: Long): Flow<List<String>>

    // Unified suggestions query: fetch all distinct fields in single query
    @Query("""
        SELECT DISTINCT title, payee, notes, location, tags
        FROM transactions
        WHERE timestamp >= :since AND (title != '' OR payee != '' OR notes != '' OR location != '' OR tags != '')
        ORDER BY timestamp DESC
        LIMIT 100
    """)
    suspend fun getSuggestions(since: Long): List<SuggestionRow>
}

data class SuggestionRow(
    val title: String,
    val payee: String,
    val notes: String,
    val location: String,
    val tags: String
)
