package com.antcashmanager.android.testutil

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionSuggestions
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Fake unico e completo di [TransactionRepository] per i test dei ViewModel.
 *
 * Tutte le operazioni leggono/scrivono un'unica sorgente di verità reattiva
 * ([transactions]), quindi si comportano in modo realistico senza bisogno di
 * varianti dedicate. Per testare un fallimento assegna [errorToThrow]; per
 * testare la cancellazione assegna [operationDelayMs]. Se un test ha bisogno
 * di un comportamento diverso su un singolo metodo, la classe è `open` per
 * poterlo sovrascrivere invece di duplicare l'intero fake.
 */
open class FakeTransactionRepository(
    initialTransactions: List<Transaction> = emptyList(),
) : TransactionRepository {

    val transactions = MutableStateFlow(initialTransactions)

    /** Se non-null, ogni operazione di scrittura lo lancia invece di eseguire l'operazione. */
    var errorToThrow: Throwable? = null

    /** Ritardo applicato prima di ogni operazione sospesa, utile per testare la cancellazione. */
    var operationDelayMs: Long = 0

    private var nextId = (initialTransactions.maxOfOrNull { it.id } ?: 0L) + 1

    override fun getAllTransactions(): Flow<List<Transaction>> = transactions

    override suspend fun getTransactionById(id: Long): Transaction? =
        transactions.value.find { it.id == id }

    override suspend fun insertTransaction(transaction: Transaction): Long {
        applyDelayAndMaybeThrow()
        val id = if (transaction.id != 0L) transaction.id else nextId++
        transactions.update { it + transaction.copy(id = id) }
        return id
    }

    override suspend fun insertTransactions(transactions: List<Transaction>): List<Long> {
        applyDelayAndMaybeThrow()
        val ids = transactions.map { if (it.id != 0L) it.id else nextId++ }
        val withIds = transactions.zip(ids).map { (t, id) -> t.copy(id = id) }
        this.transactions.update { it + withIds }
        return ids
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        applyDelayAndMaybeThrow()
        transactions.update { list -> list.map { if (it.id == transaction.id) transaction else it } }
    }

    override suspend fun updateTransactions(transactions: List<Transaction>) {
        applyDelayAndMaybeThrow()
        this.transactions.update { list ->
            list.map { existing ->
                transactions.find { it.id == existing.id } ?: existing
            }
        }
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        applyDelayAndMaybeThrow()
        transactions.update { list -> list.filterNot { it.id == transaction.id } }
    }

    override suspend fun deleteAllTransactions() {
        applyDelayAndMaybeThrow()
        transactions.value = emptyList()
    }

    override fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<Transaction>> =
        transactions.map { list -> list.filter { it.timestamp in from..to } }

    override fun getRecurringTransactions(): Flow<List<Transaction>> =
        transactions.map { list -> list.filter { it.isRecurring } }

    override suspend fun renameCategory(
        oldCategoryName: String,
        newCategoryName: String,
        icon: String,
        color: Long
    ) {
        transactions.update { list ->
            list.map { current ->
                if (current.category == oldCategoryName) {
                    current.copy(
                        category = newCategoryName,
                        categoryIcon = icon,
                        categoryColor = color
                    )
                } else {
                    current
                }
            }
        }
    }

    override fun getDistinctTitles(since: Long): Flow<List<String>> =
        distinctValuesOf(since) { it.title }

    override fun getDistinctPayees(since: Long): Flow<List<String>> =
        distinctValuesOf(since) { it.payee }

    override fun getDistinctNotes(since: Long): Flow<List<String>> =
        distinctValuesOf(since) { it.notes }

    override fun getDistinctLocations(since: Long): Flow<List<String>> =
        distinctValuesOf(since) { it.location }

    override fun getDistinctTags(since: Long): Flow<List<String>> =
        distinctValuesOf(since) { it.tags }

    override suspend fun getSuggestions(since: Long): TransactionSuggestions {
        applyDelayAndMaybeThrow()
        val filtered = transactions.value.filter { it.timestamp >= since }
        return TransactionSuggestions(
            titles = filtered.map { it.title }.filter { it.isNotBlank() }.distinct(),
            payees = filtered.map { it.payee }.filter { it.isNotBlank() }.distinct(),
            notes = filtered.map { it.notes }.filter { it.isNotBlank() }.distinct(),
            locations = filtered.map { it.location }.filter { it.isNotBlank() }.distinct(),
            tags = filtered.map { it.tags }.filter { it.isNotBlank() }.distinct()
        )
    }

    private fun distinctValuesOf(
        since: Long,
        selector: (Transaction) -> String
    ): Flow<List<String>> =
        transactions.map { list ->
            list.filter { it.timestamp >= since }.map(selector).filter { it.isNotBlank() }
                .distinct()
        }

    private suspend fun applyDelayAndMaybeThrow() {
        if (operationDelayMs > 0) delay(operationDelayMs)
        errorToThrow?.let { throw it }
    }
}
