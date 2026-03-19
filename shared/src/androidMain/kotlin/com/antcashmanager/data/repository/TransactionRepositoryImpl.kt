package com.antcashmanager.data.repository

import com.antcashmanager.data.local.dao.TransactionDao
import com.antcashmanager.data.mapper.toDomain
import com.antcashmanager.data.mapper.toEntity
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getTransactionById(id: Long): Transaction? =
        transactionDao.getTransactionById(id)?.toDomain()

    override suspend fun insertTransaction(transaction: Transaction): Long =
        transactionDao.insertTransaction(transaction.toEntity())

    override suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.updateTransaction(transaction.toEntity())

    override suspend fun deleteTransaction(transaction: Transaction) =
        transactionDao.deleteTransaction(transaction.toEntity())

    override suspend fun deleteAllTransactions() =
        transactionDao.deleteAllTransactions()

    override fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateRange(from, to).map { entities ->
            entities.map { it.toDomain() }
        }
}
