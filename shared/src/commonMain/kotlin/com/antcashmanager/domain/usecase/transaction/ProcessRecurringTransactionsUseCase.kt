package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.RecurrenceInterval
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.base.NoParamsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock

/**
 * Processes recurring transactions by checking each one and generating
 * new transaction instances if the interval has elapsed since the last
 * occurrence.
 */
class ProcessRecurringTransactionsUseCase(
    private val transactionRepository: TransactionRepository,
    private val clock: Clock = Clock.System,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsUseCase<Unit>(dispatcher) {

    override suspend fun execute(params: Unit) {
        val recurring = transactionRepository.getRecurringTransactions().first()
        val now = clock.now().toEpochMilliseconds()

        val toInsert = mutableListOf<com.antcashmanager.domain.model.Transaction>()
        val toUpdate = mutableListOf<com.antcashmanager.domain.model.Transaction>()

        for (transaction in recurring) {
            val interval = try {
                RecurrenceInterval.valueOf(transaction.recurrenceInterval)
            } catch (_: Exception) {
                continue
            }

            val intervalMs = interval.toMillis()
            val elapsed = now - transaction.timestamp

            if (elapsed >= intervalMs) {
                val periods = (elapsed / intervalMs).toInt()
                for (i in 1..periods) {
                    val newTimestamp = transaction.timestamp + intervalMs * i
                    // Avoid inserting duplicates for already-generated future dates
                    if (newTimestamp <= now) {
                        toInsert.add(
                            transaction.copy(
                                id = 0,
                                timestamp = newTimestamp,
                                isRecurring = false,
                                recurrenceInterval = "",
                            )
                        )
                    }
                }

                // Update the recurring template's timestamp to the latest period
                val latestTimestamp = transaction.timestamp + intervalMs * periods
                toUpdate.add(transaction.copy(timestamp = latestTimestamp))
            }
        }

        // Batch insert all new recurring instances
        if (toInsert.isNotEmpty()) {
            transactionRepository.insertTransactions(toInsert)
        }

        // Batch update all recurring templates
        if (toUpdate.isNotEmpty()) {
            transactionRepository.updateTransactions(toUpdate)
        }
    }

    private fun RecurrenceInterval.toMillis(): Long = when (this) {
        RecurrenceInterval.DAILY -> 24L * 60 * 60 * 1000
        RecurrenceInterval.WEEKLY -> 7L * 24 * 60 * 60 * 1000
        RecurrenceInterval.MONTHLY -> 30L * 24 * 60 * 60 * 1000
        RecurrenceInterval.YEARLY -> 365L * 24 * 60 * 60 * 1000
    }
}

