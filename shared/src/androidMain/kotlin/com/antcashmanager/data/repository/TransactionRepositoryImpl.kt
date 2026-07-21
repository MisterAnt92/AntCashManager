package com.antcashmanager.data.repository

import com.antcashmanager.data.local.dao.TransactionDao
import com.antcashmanager.data.mapper.toDomain
import com.antcashmanager.data.mapper.toEntity
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.security.LocalDataCipher
import com.antcashmanager.domain.service.NoOpWidgetUpdateNotifier
import com.antcashmanager.domain.service.WidgetUpdateNotifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val localDataCipher: LocalDataCipher,
    private val widgetUpdateNotifier: WidgetUpdateNotifier = NoOpWidgetUpdateNotifier,
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions().map { entities ->
            entities.map { decryptEntity(it).toDomain() }
        }

    override suspend fun getTransactionById(id: Long): Transaction? =
        transactionDao.getTransactionById(id)?.let { decryptEntity(it).toDomain() }

    override suspend fun insertTransaction(transaction: Transaction): Long =
        transactionDao.insertTransaction(encryptEntity(transaction.toEntity()))
            .also { widgetUpdateNotifier.notifyTransactionsChanged() }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(encryptEntity(transaction.toEntity()))
        widgetUpdateNotifier.notifyTransactionsChanged()
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(encryptEntity(transaction.toEntity()))
        widgetUpdateNotifier.notifyTransactionsChanged()
    }

    override suspend fun deleteAllTransactions() {
        transactionDao.deleteAllTransactions()
        widgetUpdateNotifier.notifyTransactionsChanged()
    }

    override fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateRange(from, to).map { entities ->
            entities.map { decryptEntity(it).toDomain() }
        }

    override fun getRecurringTransactions(): Flow<List<Transaction>> =
        transactionDao.getRecurringTransactions().map { entities ->
            entities.map { decryptEntity(it).toDomain() }
        }

    override suspend fun renameCategory(oldCategoryName: String, newCategoryName: String, icon: String, color: Long) =
        transactionDao.renameCategory(oldCategoryName, newCategoryName, icon, color)

    // Implementazione metodi per suggerimenti
    override fun getDistinctTitles(since: Long): Flow<List<String>> =
        transactionDao.getDistinctTitles(since).map { values ->
            values.map(localDataCipher::decryptString).distinct()
        }

    override fun getDistinctPayees(since: Long): Flow<List<String>> =
        transactionDao.getDistinctPayees(since).map { values ->
            values.map(localDataCipher::decryptString).distinct()
        }

    override fun getDistinctNotes(since: Long): Flow<List<String>> =
        transactionDao.getDistinctNotes(since).map { values ->
            values.map(localDataCipher::decryptString).distinct()
        }

    override fun getDistinctLocations(since: Long): Flow<List<String>> =
        transactionDao.getDistinctLocations(since).map { values ->
            values.map(localDataCipher::decryptString).distinct()
        }

    override fun getDistinctTags(since: Long): Flow<List<String>> =
        transactionDao.getDistinctTags(since).map { values ->
            values.map(localDataCipher::decryptString).distinct()
        }

    private fun encryptEntity(entity: com.antcashmanager.data.local.entity.TransactionEntity) =
        entity.copy(
            title = localDataCipher.encryptString(entity.title),
            notes = localDataCipher.encryptString(entity.notes),
            payee = localDataCipher.encryptString(entity.payee),
            location = localDataCipher.encryptString(entity.location),
            tags = localDataCipher.encryptString(entity.tags),
        )

    private fun decryptEntity(entity: com.antcashmanager.data.local.entity.TransactionEntity) =
        entity.copy(
            title = localDataCipher.decryptString(entity.title),
            notes = localDataCipher.decryptString(entity.notes),
            payee = localDataCipher.decryptString(entity.payee),
            location = localDataCipher.decryptString(entity.location),
            tags = localDataCipher.decryptString(entity.tags),
        )
}
