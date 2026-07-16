package com.antcashmanager.domain.repository

import com.antcashmanager.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    suspend fun getTransactionById(id: Long): Transaction?
    suspend fun insertTransaction(transaction: Transaction): Long
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun deleteAllTransactions()
    fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<Transaction>>
    fun getRecurringTransactions(): Flow<List<Transaction>>
    suspend fun renameCategory(oldCategoryName: String, newCategoryName: String, icon: String, color: Long)

    // Metodi per suggerimenti transazioni
    fun getDistinctTitles(): Flow<List<String>>
    fun getDistinctPayees(): Flow<List<String>>
    fun getDistinctNotes(): Flow<List<String>>
    fun getDistinctLocations(): Flow<List<String>>
    fun getDistinctTags(): Flow<List<String>>
}
