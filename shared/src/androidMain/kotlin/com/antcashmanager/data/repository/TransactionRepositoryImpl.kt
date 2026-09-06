package com.antcashmanager.data.repository

import com.antcashmanager.data.local.dao.TransactionDao
import com.antcashmanager.data.mapper.toDomain
import com.antcashmanager.data.mapper.toEntity
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.security.LocalDataCipher
import com.antcashmanager.domain.service.NoOpWidgetUpdateNotifier
import com.antcashmanager.domain.service.WidgetUpdateNotifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

public class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val localDataCipher: LocalDataCipher,
    private val widgetUpdateNotifier: WidgetUpdateNotifier = NoOpWidgetUpdateNotifier,
) : TransactionRepository {
    // NOTE: DecryptionLRUCache was removed (never used via .get() method).
    // Cache clearing on update was the only usage, which is now handled by individual methods.

    override fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions()
            .flowOn(Dispatchers.Default)
            .map { entities ->
                entities.map { decryptEntity(it).toDomain() }
            }

    override fun getTransactionsPaginated(pageSize: Int, pageIndex: Int): Flow<List<Transaction>> =
        transactionDao.getTransactionsPaginated(
            limit = pageSize,
            offset = pageIndex * pageSize
        )
            .flowOn(Dispatchers.Default)
            .map { entities ->
                entities.map { decryptEntity(it).toDomain() }
            }

    override fun getTransactionsByCategory(category: String, pageSize: Int, pageIndex: Int): Flow<List<Transaction>> =
        transactionDao.getTransactionsByCategory(
            category = category,
            limit = pageSize,
            offset = pageIndex * pageSize
        )
            .flowOn(Dispatchers.Default)
            .map { entities ->
                entities.map { decryptEntity(it).toDomain() }
            }

    override fun searchTransactions(query: String, pageSize: Int, pageIndex: Int): Flow<List<Transaction>> {
        // When encryption is ON, LIKE queries on ciphertext fail (random IV per row).
        // Branch: if encrypted, load all transactions and filter in memory; if not, use fast DAO path.
        if (!localDataCipher.isEncryptionEnabled()) {
            // Fast path: use DAO LIKE query
            return transactionDao.searchTransactions(
                query = "%$query%",  // LIKE wildcard pattern
                limit = pageSize,
                offset = pageIndex * pageSize
            )
                .flowOn(Dispatchers.Default)
                .map { entities ->
                    entities.map { decryptEntity(it).toDomain() }
                }
        }

        // Slow path: encryption enabled — load all, decrypt, filter in memory
        return transactionDao.getAllTransactions()
            .flowOn(Dispatchers.Default)
            .map { entities ->
                entities
                    .map { decryptEntity(it).toDomain() }
                    .filter { transaction ->
                        val lowerQuery = query.lowercase()
                        transaction.title.lowercase().contains(lowerQuery) ||
                                transaction.payee.lowercase().contains(lowerQuery) ||
                                transaction.notes.lowercase().contains(lowerQuery)
                    }
                    .drop(pageIndex * pageSize)
                    .take(pageSize)
            }
    }

    override suspend fun getTransactionById(id: Long): Transaction? =
        transactionDao.getTransactionById(id)?.let { decryptEntity(it).toDomain() }

    override suspend fun insertTransaction(transaction: Transaction): Long =
        transactionDao.insertTransaction(encryptEntity(transaction.toEntity()))
            .also { widgetUpdateNotifier.notifyTransactionsChanged() }

    override suspend fun insertTransactions(transactions: List<Transaction>): List<Long> =
        transactionDao.insertTransactions(
            transactions.asSequence()
                .map { encryptEntity(it.toEntity()) }
                .toList()
        )
            .also { widgetUpdateNotifier.notifyTransactionsChanged() }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(encryptEntity(transaction.toEntity()))
        widgetUpdateNotifier.notifyTransactionsChanged()
    }

    override suspend fun updateTransactions(transactions: List<Transaction>) {
        transactionDao.updateTransactions(
            transactions.asSequence()
                .map { encryptEntity(it.toEntity()) }
                .toList()
        )
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
        transactionDao.getTransactionsByDateRange(from, to)
            .flowOn(Dispatchers.Default)
            .map { entities ->
                entities.map { decryptEntity(it).toDomain() }
            }

    override fun getRecurringTransactions(): Flow<List<Transaction>> =
        transactionDao.getRecurringTransactions()
            .flowOn(Dispatchers.Default)
            .map { entities ->
                entities.map { decryptEntity(it).toDomain() }
            }

    override suspend fun renameCategory(
        oldCategoryName: String,
        newCategoryName: String,
        icon: String,
        color: Long
    ): Unit =
        transactionDao.renameCategory(oldCategoryName, newCategoryName, icon, color)

    // Implementazione metodi per suggerimenti
    // NOTE: When encryption is ON, DISTINCT on ciphertext is ineffective (random IV per row).
    // We branch: if encrypted, decrypt then deduplicate; if not, use fast DAO path.

    override fun getDistinctTitles(since: Long): Flow<List<String>> {
        if (!localDataCipher.isEncryptionEnabled()) {
            return transactionDao.getDistinctTitles(since)
                .flowOn(Dispatchers.Default)
                .map { values -> values.map(localDataCipher::decryptString).distinct() }
        }
        // Encrypted: load all, decrypt, dedupe
        return transactionDao.getAllTransactions()
            .flowOn(Dispatchers.Default)
            .map { entities ->
                entities
                    .asSequence()
                    .map { localDataCipher.decryptString(it.title) }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .take(20)
                    .toList()
            }
    }

    override fun getDistinctPayees(since: Long): Flow<List<String>> {
        if (!localDataCipher.isEncryptionEnabled()) {
            return transactionDao.getDistinctPayees(since)
                .flowOn(Dispatchers.Default)
                .map { values -> values.map(localDataCipher::decryptString).distinct() }
        }
        // Encrypted: load all, decrypt, dedupe
        return transactionDao.getAllTransactions()
            .flowOn(Dispatchers.Default)
            .map { entities ->
                entities
                    .asSequence()
                    .map { localDataCipher.decryptString(it.payee) }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .take(20)
                    .toList()
            }
    }

    override fun getDistinctNotes(since: Long): Flow<List<String>> {
        if (!localDataCipher.isEncryptionEnabled()) {
            return transactionDao.getDistinctNotes(since)
                .flowOn(Dispatchers.Default)
                .map { values -> values.map(localDataCipher::decryptString).distinct() }
        }
        // Encrypted: load all, decrypt, dedupe
        return transactionDao.getAllTransactions()
            .flowOn(Dispatchers.Default)
            .map { entities ->
                entities
                    .asSequence()
                    .map { localDataCipher.decryptString(it.notes) }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .take(20)
                    .toList()
            }
    }

    override fun getDistinctLocations(since: Long): Flow<List<String>> {
        if (!localDataCipher.isEncryptionEnabled()) {
            return transactionDao.getDistinctLocations(since)
                .flowOn(Dispatchers.Default)
                .map { values -> values.map(localDataCipher::decryptString).distinct() }
        }
        // Encrypted: load all, decrypt, dedupe
        return transactionDao.getAllTransactions()
            .flowOn(Dispatchers.Default)
            .map { entities ->
                entities
                    .asSequence()
                    .map { localDataCipher.decryptString(it.location) }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .take(20)
                    .toList()
            }
    }

    override fun getDistinctTags(since: Long): Flow<List<String>> {
        if (!localDataCipher.isEncryptionEnabled()) {
            return transactionDao.getDistinctTags(since)
                .map { values -> values.map(localDataCipher::decryptString).distinct() }
        }
        // Encrypted: load all, decrypt, dedupe
        return transactionDao.getAllTransactions()
            .flowOn(Dispatchers.Default)
            .map { entities ->
                entities
                    .asSequence()
                    .map { localDataCipher.decryptString(it.tags) }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .take(20)
                    .toList()
            }
    }

    override suspend fun getSuggestions(since: Long): com.antcashmanager.domain.model.TransactionSuggestions {
        val rows = transactionDao.getSuggestions(since)

        // Optimize: Decrypt each row only once, then extract all fields
        // BEFORE: 5 decrypt operations per row = 5N total
        // AFTER: 1 decrypt per field per row = 5N total (but in single pass)
        val decryptedRows = rows.mapNotNull { row ->
            val title = localDataCipher.decryptString(row.title)
            val payee = localDataCipher.decryptString(row.payee)
            val notes = localDataCipher.decryptString(row.notes)
            val location = localDataCipher.decryptString(row.location)
            val tags = localDataCipher.decryptString(row.tags)

            // Return only if at least one field is non-empty
            if (title.isEmpty() && payee.isEmpty() && notes.isEmpty() &&
                location.isEmpty() && tags.isEmpty()) {
                null
            } else {
                DecryptedSuggestionRow(title, payee, notes, location, tags)
            }
        }

        // Convert to distinct sets - using Set for better performance than List.distinct()
        val titleSet = mutableSetOf<String>()
        val payeeSet = mutableSetOf<String>()
        val noteSet = mutableSetOf<String>()
        val locationSet = mutableSetOf<String>()
        val tagSet = mutableSetOf<String>()

        decryptedRows.forEach { row ->
            if (row.title.isNotEmpty()) titleSet.add(row.title)
            if (row.payee.isNotEmpty()) payeeSet.add(row.payee)
            if (row.notes.isNotEmpty()) noteSet.add(row.notes)
            if (row.location.isNotEmpty()) locationSet.add(row.location)
            if (row.tags.isNotEmpty()) tagSet.add(row.tags)
        }

        return com.antcashmanager.domain.model.TransactionSuggestions(
            titles = titleSet.toList(),
            payees = payeeSet.toList(),
            notes = noteSet.toList(),
            locations = locationSet.toList(),
            tags = tagSet.toList()
        )
    }

    /**
     * Helper data class to store decrypted values temporarily
     * Used in getSuggestions() to minimize decrypt operations
     */
    private data class DecryptedSuggestionRow(
        val title: String,
        val payee: String,
        val notes: String,
        val location: String,
        val tags: String
    )

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
