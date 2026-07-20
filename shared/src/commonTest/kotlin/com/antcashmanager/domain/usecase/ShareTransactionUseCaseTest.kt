package com.antcashmanager.domain.usecase

import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertTrue

class ShareTransactionUseCaseTest {
    private val useCase = ShareTransactionUseCase()

    @Test
    fun formatTransactionForShare_shouldIncludeTransactionTitle() {
        val transaction = Transaction(
            id = 1,
            title = "Test Transaction",
            amount = 100.0,
            category = "Test",
            type = TransactionType.INCOME,
            timestamp = System.currentTimeMillis(),
        )
        val result = useCase(ShareTransactionUseCase.Params(transaction))
        assertTrue(result.contains("Test Transaction"))
    }

    @Test
    fun formatTransactionForShare_shouldShowPlusSignForIncome() {
        val transaction = Transaction(
            id = 1,
            title = "Salary",
            amount = 2500.0,
            category = "Work",
            type = TransactionType.INCOME,
            timestamp = System.currentTimeMillis(),
        )
        val result = useCase(ShareTransactionUseCase.Params(transaction))
        assertTrue(result.contains("+"))
    }

    @Test
    fun formatTransactionForShare_shouldIncludeAllData_whenComplete() {
        val transaction = Transaction(
            id = 1,
            title = "Complete",
            amount = 250.50,
            category = "Test",
            type = TransactionType.EXPENSE,
            timestamp = System.currentTimeMillis(),
            notes = "Notes",
            payee = "Payee",
            location = "Location",
            isRecurring = true,
            recurrenceInterval = "monthly",
            tags = "tag1",
        )
        val result = useCase(ShareTransactionUseCase.Params(transaction))
        assertTrue(result.contains("Complete"))
        assertTrue(result.contains("AntCashManager"))
    }
}
