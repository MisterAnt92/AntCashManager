package com.antcashmanager.data.repository

import com.antcashmanager.data.local.dao.TransactionDao
import com.antcashmanager.data.mapper.toDomain
import com.antcashmanager.data.mapper.toEntity
import com.antcashmanager.domain.manager.ProfileManager
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.security.LocalDataCipher
import com.antcashmanager.domain.service.NoOpWidgetUpdateNotifier
import com.antcashmanager.domain.service.WidgetUpdateNotifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

public class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val localDataCipher: LocalDataCipher,
    private val profileManager: ProfileManager,
    private val widgetUpdateNotifier: WidgetUpdateNotifier = NoOpWidgetUpdateNotifier,
) : TransactionRepository {

    private val pid get() = profileManager.getActiveProfileIdSync()

    override fun getAllTransactions(): Flow<List<Transaction>> =
        profileManager.activeProfileId
            .flatMapLatest { profileId ->
                transactionDao.getAllTransactions(profileId)
            }.flowOn(Dispatchers.Default)
            .map { entities -> entities.map { decryptEntity(it).toDomain() } }

    override fun getTransactionsPaginated(
        pageSize: Int,
        pageIndex: Int,
    ): Flow<List<Transaction>> =
        profileManager.activeProfileId
            .flatMapLatest { profileId ->
                transactionDao.getTransactionsPaginated(
                    profileId = profileId,
                    limit = pageSize,
                    offset = pageIndex * pageSize,
                )
            }.flowOn(Dispatchers.Default)
            .map { entities -> entities.map { decryptEntity(it).toDomain() } }

    override fun getTransactionsByCategory(
        category: String,
        pageSize: Int,
        pageIndex: Int,
    ): Flow<List<Transaction>> =
        profileManager.activeProfileId
            .flatMapLatest { profileId ->
                transactionDao.getTransactionsByCategory(
                    profileId = profileId,
                    category = category,
                    limit = pageSize,
                    offset = pageIndex * pageSize,
                )
            }.flowOn(Dispatchers.Default)
            .map { entities -> entities.map { decryptEntity(it).toDomain() } }

    override fun searchTransactions(
        query: String,
        pageSize: Int,
        pageIndex: Int,
    ): Flow<List<Transaction>> {
        if (!localDataCipher.isEncryptionEnabled()) {
            return profileManager.activeProfileId
                .flatMapLatest { profileId ->
                    transactionDao.searchTransactions(
                        profileId = profileId,
                        query = "%$query%",
                        limit = pageSize,
                        offset = pageIndex * pageSize,
                    )
                }.flowOn(Dispatchers.Default)
                .map { entities -> entities.map { decryptEntity(it).toDomain() } }
        }

        // Encryption enabled: load all for active profile, decrypt, filter in memory
        return profileManager.activeProfileId
            .flatMapLatest { profileId ->
                transactionDao.getAllTransactions(profileId)
            }.flowOn(Dispatchers.Default)
            .map { entities ->
                entities
                    .map { decryptEntity(it).toDomain() }
                    .filter { transaction ->
                        val lowerQuery = query.lowercase()
                        transaction.title.lowercase().contains(lowerQuery) ||
                            transaction.payee.lowercase().contains(lowerQuery) ||
                            transaction.notes.lowercase().contains(lowerQuery)
                    }.drop(pageIndex * pageSize)
                    .take(pageSize)
            }
    }

    override suspend fun getTransactionById(id: Long): Transaction? =
        transactionDao.getTransactionById(id)?.let { decryptEntity(it).toDomain() }

    override suspend fun insertTransaction(transaction: Transaction): Long =
        transactionDao
            .insertTransaction(encryptEntity(transaction.toEntity().copy(profileId = pid)))
            .also { widgetUpdateNotifier.notifyTransactionsChanged() }

    override suspend fun insertTransactions(transactions: List<Transaction>): List<Long> {
        val profileId = pid
        return transactionDao
            .insertTransactions(
                transactions
                    .asSequence()
                    .map { encryptEntity(it.toEntity().copy(profileId = profileId)) }
                    .toList(),
            ).also { widgetUpdateNotifier.notifyTransactionsChanged() }
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(encryptEntity(transaction.toEntity().copy(profileId = pid)))
        widgetUpdateNotifier.notifyTransactionsChanged()
    }

    override suspend fun updateTransactions(transactions: List<Transaction>) {
        val profileId = pid
        transactionDao.updateTransactions(
            transactions
                .asSequence()
                .map { encryptEntity(it.toEntity().copy(profileId = profileId)) }
                .toList(),
        )
        widgetUpdateNotifier.notifyTransactionsChanged()
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(encryptEntity(transaction.toEntity()))
        widgetUpdateNotifier.notifyTransactionsChanged()
    }

    override suspend fun deleteAllTransactions() {
        transactionDao.deleteAllTransactions(pid)
        widgetUpdateNotifier.notifyTransactionsChanged()
    }

    override suspend fun deleteAllTransactionsForProfile(profileId: Long) {
        transactionDao.deleteAllTransactions(profileId)
        widgetUpdateNotifier.notifyTransactionsChanged()
    }

    override fun getTransactionsByDateRange(
        from: Long,
        to: Long,
    ): Flow<List<Transaction>> =
        profileManager.activeProfileId
            .flatMapLatest { profileId ->
                transactionDao.getTransactionsByDateRange(profileId, from, to)
            }.flowOn(Dispatchers.Default)
            .map { entities -> entities.map { decryptEntity(it).toDomain() } }

    override fun getRecurringTransactions(): Flow<List<Transaction>> =
        profileManager.activeProfileId
            .flatMapLatest { profileId ->
                transactionDao.getRecurringTransactions(profileId)
            }.flowOn(Dispatchers.Default)
            .map { entities -> entities.map { decryptEntity(it).toDomain() } }

    override suspend fun renameCategory(
        oldCategoryName: String,
        newCategoryName: String,
        icon: String,
        color: Long,
    ): Unit = transactionDao.renameCategory(pid, oldCategoryName, newCategoryName, icon, color)

    override fun getDistinctTitles(since: Long): Flow<List<String>> {
        if (!localDataCipher.isEncryptionEnabled()) {
            return profileManager.activeProfileId
                .flatMapLatest { profileId ->
                    transactionDao.getDistinctTitles(profileId, since)
                }.flowOn(Dispatchers.Default)
                .map { values -> values.map(localDataCipher::decryptString).distinct() }
        }
        return profileManager.activeProfileId
            .flatMapLatest { profileId -> transactionDao.getAllTransactions(profileId) }
            .flowOn(Dispatchers.Default)
            .map { entities ->
                entities.asSequence()
                    .map { localDataCipher.decryptString(it.title) }
                    .filter { it.isNotEmpty() }.distinct().take(20).toList()
            }
    }

    override fun getDistinctPayees(since: Long): Flow<List<String>> {
        if (!localDataCipher.isEncryptionEnabled()) {
            return profileManager.activeProfileId
                .flatMapLatest { profileId ->
                    transactionDao.getDistinctPayees(profileId, since)
                }.flowOn(Dispatchers.Default)
                .map { values -> values.map(localDataCipher::decryptString).distinct() }
        }
        return profileManager.activeProfileId
            .flatMapLatest { profileId -> transactionDao.getAllTransactions(profileId) }
            .flowOn(Dispatchers.Default)
            .map { entities ->
                entities.asSequence()
                    .map { localDataCipher.decryptString(it.payee) }
                    .filter { it.isNotEmpty() }.distinct().take(20).toList()
            }
    }

    override fun getDistinctNotes(since: Long): Flow<List<String>> {
        if (!localDataCipher.isEncryptionEnabled()) {
            return profileManager.activeProfileId
                .flatMapLatest { profileId ->
                    transactionDao.getDistinctNotes(profileId, since)
                }.flowOn(Dispatchers.Default)
                .map { values -> values.map(localDataCipher::decryptString).distinct() }
        }
        return profileManager.activeProfileId
            .flatMapLatest { profileId -> transactionDao.getAllTransactions(profileId) }
            .flowOn(Dispatchers.Default)
            .map { entities ->
                entities.asSequence()
                    .map { localDataCipher.decryptString(it.notes) }
                    .filter { it.isNotEmpty() }.distinct().take(20).toList()
            }
    }

    override fun getDistinctLocations(since: Long): Flow<List<String>> {
        if (!localDataCipher.isEncryptionEnabled()) {
            return profileManager.activeProfileId
                .flatMapLatest { profileId ->
                    transactionDao.getDistinctLocations(profileId, since)
                }.flowOn(Dispatchers.Default)
                .map { values -> values.map(localDataCipher::decryptString).distinct() }
        }
        return profileManager.activeProfileId
            .flatMapLatest { profileId -> transactionDao.getAllTransactions(profileId) }
            .flowOn(Dispatchers.Default)
            .map { entities ->
                entities.asSequence()
                    .map { localDataCipher.decryptString(it.location) }
                    .filter { it.isNotEmpty() }.distinct().take(20).toList()
            }
    }

    override fun getDistinctTags(since: Long): Flow<List<String>> {
        if (!localDataCipher.isEncryptionEnabled()) {
            return profileManager.activeProfileId
                .flatMapLatest { profileId ->
                    transactionDao.getDistinctTags(profileId, since)
                }.flowOn(Dispatchers.Default)
                .map { values -> values.map(localDataCipher::decryptString).distinct() }
        }
        return profileManager.activeProfileId
            .flatMapLatest { profileId -> transactionDao.getAllTransactions(profileId) }
            .flowOn(Dispatchers.Default)
            .map { entities ->
                entities.asSequence()
                    .map { localDataCipher.decryptString(it.tags) }
                    .filter { it.isNotEmpty() }.distinct().take(20).toList()
            }
    }

    override suspend fun getSuggestions(since: Long): com.antcashmanager.domain.model.TransactionSuggestions {
        val rows = transactionDao.getSuggestions(pid, since)
        val decryptedRows = rows.mapNotNull { row ->
            val title = localDataCipher.decryptString(row.title)
            val payee = localDataCipher.decryptString(row.payee)
            val notes = localDataCipher.decryptString(row.notes)
            val location = localDataCipher.decryptString(row.location)
            val tags = localDataCipher.decryptString(row.tags)
            if (title.isEmpty() && payee.isEmpty() && notes.isEmpty() &&
                location.isEmpty() && tags.isEmpty()
            ) {
                null
            } else {
                DecryptedSuggestionRow(title, payee, notes, location, tags)
            }
        }

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
            tags = tagSet.toList(),
        )
    }

    private data class DecryptedSuggestionRow(
        val title: String,
        val payee: String,
        val notes: String,
        val location: String,
        val tags: String,
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
