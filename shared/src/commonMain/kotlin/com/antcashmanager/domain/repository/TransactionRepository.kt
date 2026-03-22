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
}
