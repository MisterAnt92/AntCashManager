package com.antcashmanager.domain.repository

import com.antcashmanager.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>

    /**
     * Get transactions with pagination support.
     * Recommended for large datasets (1000+ transactions).
     * @param pageSize Number of items per page (default: 100)
     * @param pageIndex Page index starting from 0
     * @return Flow of transactions for the requested page
     */
    fun getTransactionsPaginated(pageSize: Int = 100, pageIndex: Int = 0): Flow<List<Transaction>>

    /**
     * Get transactions filtered by category (database-level filtering).
     * CRITICAL: Pushing filtering to DB instead of client-side dramatically improves performance.
     */
    fun getTransactionsByCategory(category: String, pageSize: Int = 100, pageIndex: Int = 0): Flow<List<Transaction>>

    /**
     * Search transactions by query text (title/payee/notes).
     * Database-level full-text equivalent for efficiency.
     */
    fun searchTransactions(query: String, pageSize: Int = 100, pageIndex: Int = 0): Flow<List<Transaction>>

    suspend fun getTransactionById(id: Long): Transaction?
    suspend fun insertTransaction(transaction: Transaction): Long
    suspend fun insertTransactions(transactions: List<Transaction>): List<Long>
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun updateTransactions(transactions: List<Transaction>)
    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun deleteAllTransactions()
    fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<Transaction>>
    fun getRecurringTransactions(): Flow<List<Transaction>>
    suspend fun renameCategory(
        oldCategoryName: String,
        newCategoryName: String,
        icon: String,
        color: Long
    )

    // Metodi per suggerimenti transazioni. [since] filtra alle transazioni con
    // timestamp >= since (default 0 = nessun filtro), usato per "dimenticare" i
    // suggerimenti precedenti a una cancellazione richiesta dall'utente.
    fun getDistinctTitles(since: Long = 0L): Flow<List<String>>
    fun getDistinctPayees(since: Long = 0L): Flow<List<String>>
    fun getDistinctNotes(since: Long = 0L): Flow<List<String>>
    fun getDistinctLocations(since: Long = 0L): Flow<List<String>>
    fun getDistinctTags(since: Long = 0L): Flow<List<String>>

    // Unified suggestions in a single query (optimized)
    suspend fun getSuggestions(since: Long = 0L): com.antcashmanager.domain.model.TransactionSuggestions
}
