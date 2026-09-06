package com.antcashmanager.domain.repository

import com.antcashmanager.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

public interface TransactionRepository {
    public fun getAllTransactions(): Flow<List<Transaction>>

    /**
     * Get transactions with pagination support.
     * Recommended for large datasets (1000+ transactions).
     * @param pageSize Number of items per page (default: 100)
     * @param pageIndex Page index starting from 0
     * @return Flow of transactions for the requested page
     */
    public fun getTransactionsPaginated(
        pageSize: Int = 100,
        pageIndex: Int = 0,
    ): Flow<List<Transaction>>

    /**
     * Get transactions filtered by category (database-level filtering).
     * CRITICAL: Pushing filtering to DB instead of client-side dramatically improves performance.
     */
    public fun getTransactionsByCategory(
        category: String,
        pageSize: Int = 100,
        pageIndex: Int = 0,
    ): Flow<List<Transaction>>

    /**
     * Search transactions by query text (title/payee/notes).
     * Database-level full-text equivalent for efficiency.
     */
    public fun searchTransactions(
        query: String,
        pageSize: Int = 100,
        pageIndex: Int = 0,
    ): Flow<List<Transaction>>

    public suspend fun getTransactionById(id: Long): Transaction?

    public suspend fun insertTransaction(transaction: Transaction): Long

    public suspend fun insertTransactions(transactions: List<Transaction>): List<Long>

    public suspend fun updateTransaction(transaction: Transaction): Unit

    public suspend fun updateTransactions(transactions: List<Transaction>): Unit

    public suspend fun deleteTransaction(transaction: Transaction): Unit

    public suspend fun deleteAllTransactions(): Unit

    public fun getTransactionsByDateRange(
        from: Long,
        to: Long,
    ): Flow<List<Transaction>>

    public fun getRecurringTransactions(): Flow<List<Transaction>>

    public suspend fun renameCategory(
        oldCategoryName: String,
        newCategoryName: String,
        icon: String,
        color: Long,
    ): Unit

    // Metodi per suggerimenti transazioni. [since] filtra alle transazioni con
    // timestamp >= since (default 0 = nessun filtro), usato per "dimenticare" i
    // suggerimenti precedenti a una cancellazione richiesta dall'utente.
    public fun getDistinctTitles(since: Long = 0L): Flow<List<String>>

    public fun getDistinctPayees(since: Long = 0L): Flow<List<String>>

    public fun getDistinctNotes(since: Long = 0L): Flow<List<String>>

    public fun getDistinctLocations(since: Long = 0L): Flow<List<String>>

    public fun getDistinctTags(since: Long = 0L): Flow<List<String>>

    // Unified suggestions in a single query (optimized)
    public suspend fun getSuggestions(since: Long = 0L): com.antcashmanager.domain.model.TransactionSuggestions
}
