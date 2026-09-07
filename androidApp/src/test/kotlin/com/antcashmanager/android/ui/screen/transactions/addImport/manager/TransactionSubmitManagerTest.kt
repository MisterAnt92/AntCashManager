package com.antcashmanager.android.ui.screen.transactions.addImport.manager

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.testutil.FakeTransactionRepository
import com.antcashmanager.android.ui.screen.transactions.addImport.AddTransactionConstant
import com.antcashmanager.android.ui.screen.transactions.addImport.AddTransactionState
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.usecase.transaction.InsertTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.UpdateTransactionUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test unitari per [TransactionSubmitManager].
 *
 * Copre:
 * - Validazione dello stato della transazione
 * - Costruzione del Transaction object
 * - Salvataggio nel database (insert vs update)
 * - Gestione degli errori
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TransactionSubmitManagerTest : BaseUnitTest() {
    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var insertUseCase: InsertTransactionUseCase
    private lateinit var updateUseCase: UpdateTransactionUseCase
    private lateinit var manager: TransactionSubmitManager

    private val mockCategory =
        Category(
            id = 1,
            name = "Food",
            icon = "🍔",
            color = 0xFFFF6B6B,
            type = "EXPENSE",
        )

    private val validState =
        AddTransactionState(
            selectedCategory = mockCategory,
            selectedType = TransactionType.EXPENSE,
            title = "Lunch",
            amount = "25.50",
            notes = "Test notes",
            payee = "Restaurant",
            location = "Downtown",
            tags = "food,lunch",
            timestamp = System.currentTimeMillis(),
            isRecurring = false,
            recurrenceInterval = "",
            selectedPaymentType = PaymentType.CASH,
            mealVoucherCount = "0",
            isModifying = false,
            categories = listOf(mockCategory),
        )

    @Before
    fun setup() {
        transactionRepository = FakeTransactionRepository(emptyList())
        insertUseCase = InsertTransactionUseCase(transactionRepository)
        updateUseCase = UpdateTransactionUseCase(transactionRepository)
        manager =
            TransactionSubmitManager(
                insertTransactionUseCase = insertUseCase,
                updateTransactionUseCase = updateUseCase,
            )
    }

    // ── Validation Tests ──

    @Test
    fun `validateTransactionState returns null for valid state`() {
        val error = manager.validateTransactionState(validState)
        assertNull("Validation should pass for valid state", error)
    }

    @Test
    fun `validateTransactionState returns error when category is null`() {
        val state = validState.copy(selectedCategory = null)
        val error = manager.validateTransactionState(state)
        assertEquals(AddTransactionConstant.ERROR_REQUIRED_CATEGORY_TYPE, error)
    }

    @Test
    fun `validateTransactionState returns error when type is null`() {
        val state = validState.copy(selectedType = null)
        val error = manager.validateTransactionState(state)
        assertEquals(AddTransactionConstant.ERROR_REQUIRED_CATEGORY_TYPE, error)
    }

    @Test
    fun `validateTransactionState returns error when title is blank`() {
        val state = validState.copy(title = "")
        val error = manager.validateTransactionState(state)
        assertEquals(AddTransactionConstant.ERROR_REQUIRED_TITLE_AMOUNT, error)
    }

    @Test
    fun `validateTransactionState returns error when title is only whitespace`() {
        val state = validState.copy(title = "   ")
        val error = manager.validateTransactionState(state)
        assertEquals(AddTransactionConstant.ERROR_REQUIRED_TITLE_AMOUNT, error)
    }

    @Test
    fun `validateTransactionState returns error when category not in list`() {
        val state = validState.copy(categories = emptyList())
        val error = manager.validateTransactionState(state)
        assertEquals("Categoria non più disponibile", error)
    }

    @Test
    fun `validateTransactionState returns error for MEAL_VOUCHERS with invalid count`() {
        val state =
            validState.copy(
                selectedPaymentType = PaymentType.MEAL_VOUCHERS,
                mealVoucherCount = "0",
            )
        val error = manager.validateTransactionState(state)
        assertEquals("Numero buoni pasto non valido", error)
    }

    @Test
    fun `validateTransactionState returns error for MEAL_VOUCHERS with non-numeric count`() {
        val state =
            validState.copy(
                selectedPaymentType = PaymentType.MEAL_VOUCHERS,
                mealVoucherCount = "abc",
            )
        val error = manager.validateTransactionState(state)
        assertEquals("Numero buoni pasto non valido", error)
    }

    @Test
    fun `validateTransactionState passes for MEAL_VOUCHERS with valid count`() {
        val state =
            validState.copy(
                selectedPaymentType = PaymentType.MEAL_VOUCHERS,
                mealVoucherCount = "5",
            )
        val error = manager.validateTransactionState(state)
        assertNull("Validation should pass for valid MEAL_VOUCHERS state", error)
    }

    @Test
    fun `validateTransactionState returns error when amount is blank`() {
        val state =
            validState.copy(
                selectedPaymentType = PaymentType.CASH,
                amount = "",
            )
        val error = manager.validateTransactionState(state)
        assertEquals(AddTransactionConstant.ERROR_REQUIRED_TITLE_AMOUNT, error)
    }

    @Test
    fun `validateTransactionState returns error when amount is invalid`() {
        val state = validState.copy(amount = "abc")
        val error = manager.validateTransactionState(state)
        assertEquals(AddTransactionConstant.ERROR_INVALID_AMOUNT, error)
    }

    @Test
    fun `validateTransactionState returns error when amount is zero`() {
        val state = validState.copy(amount = "0")
        val error = manager.validateTransactionState(state)
        assertEquals(AddTransactionConstant.ERROR_INVALID_AMOUNT, error)
    }

    @Test
    fun `validateTransactionState returns error when amount is negative`() {
        val state = validState.copy(amount = "-10.50")
        val error = manager.validateTransactionState(state)
        assertEquals(AddTransactionConstant.ERROR_INVALID_AMOUNT, error)
    }

    // ── Build Transaction Tests ──

    @Test
    fun `buildTransaction creates correct Transaction for EXPENSE`() {
        val transaction = manager.buildTransaction(validState, transactionId = null)

        assertEquals("Lunch", transaction.title)
        assertEquals("Food", transaction.category)
        assertEquals(TransactionType.EXPENSE, transaction.type)
        assertEquals(-25.50, transaction.amount, 0.01) // Negativo per le spese
        assertEquals(validState.timestamp, transaction.timestamp)
        assertEquals("Test notes", transaction.notes)
        assertEquals("Restaurant", transaction.payee)
        assertEquals("Downtown", transaction.location)
        assertEquals("food,lunch", transaction.tags)
        assertEquals(false, transaction.isRecurring)
        assertEquals(PaymentType.CASH, transaction.paymentType)
    }

    @Test
    fun `buildTransaction creates correct Transaction for INCOME`() {
        val incomeState =
            validState.copy(
                selectedType = TransactionType.INCOME,
                selectedCategory = mockCategory.copy(type = "INCOME"),
                amount = "1500.00",
            )
        val transaction = manager.buildTransaction(incomeState, transactionId = null)

        assertEquals("Lunch", transaction.title)
        assertEquals(TransactionType.INCOME, transaction.type)
        assertEquals(1500.00, transaction.amount, 0.01) // Positivo per le entrate
    }

    @Test
    fun `buildTransaction handles MEAL_VOUCHERS payment type`() {
        val mealVoucherState =
            validState.copy(
                selectedPaymentType = PaymentType.MEAL_VOUCHERS,
                mealVoucherCount = "5",
                amount = "50.00",
            )
        val transaction = manager.buildTransaction(mealVoucherState, transactionId = null)

        assertEquals(PaymentType.MEAL_VOUCHERS, transaction.paymentType)
        assertEquals(5, transaction.mealVoucherCount)
        assertEquals(-50.00, transaction.amount, 0.01)
    }

    @Test
    fun `buildTransaction uses transaction id when modifying`() {
        val modifyingState = validState.copy(isModifying = true)
        val transaction = manager.buildTransaction(modifyingState, transactionId = 123L)

        assertEquals(123L, transaction.id)
    }

    @Test
    fun `buildTransaction uses zero id when creating new`() {
        val transaction = manager.buildTransaction(validState, transactionId = null)

        assertEquals(0L, transaction.id)
    }

    @Test
    fun `buildTransaction throws exception when category is null`() {
        val state = validState.copy(selectedCategory = null)

        try {
            manager.buildTransaction(state, transactionId = null)
            assertTrue("Expected IllegalStateException", false)
        } catch (e: IllegalStateException) {
            assertEquals("selectedCategory must not be null", e.message)
        }
    }

    @Test
    fun `buildTransaction throws exception when type is null`() {
        val state = validState.copy(selectedType = null)

        try {
            manager.buildTransaction(state, transactionId = null)
            assertTrue("Expected IllegalStateException", false)
        } catch (e: IllegalStateException) {
            assertEquals("selectedType must not be null", e.message)
        }
    }

    // ── Save Transaction Tests ──

    @Test
    fun `saveTransaction inserts new transaction`() =
        runUnitTest {
            val transaction =
                Transaction(
                    id = 0,
                    title = "Test",
                    amount = 100.0,
                    category = "Food",
                    type = TransactionType.EXPENSE,
                    timestamp = System.currentTimeMillis(),
                )

            val result = manager.saveTransaction(transaction, isModifying = false)

            assertTrue("Save should succeed", result.isSuccess)
            val allTransactions = transactionRepository.getAllTransactions().first()
            assertEquals(1, allTransactions.size)
        }

    @Test
    fun `saveTransaction updates existing transaction`() =
        runUnitTest {
            val originalTransaction =
                Transaction(
                    id = 1,
                    title = "Original",
                    amount = 100.0,
                    category = "Food",
                    type = TransactionType.EXPENSE,
                    timestamp = System.currentTimeMillis(),
                )
            insertUseCase(originalTransaction).getOrNull() ?: 0L

            val updatedTransaction =
                originalTransaction.copy(
                    title = "Updated",
                    amount = 200.0,
                )
            val result = manager.saveTransaction(updatedTransaction, isModifying = true)

            assertTrue("Update should succeed", result.isSuccess)
            val saved = transactionRepository.getTransactionById(1L)
            assertNotNull("Saved transaction should not be null", saved)
            assertEquals("Updated", saved!!.title)
            assertEquals(200.0, saved.amount, 0.01)
        }

    // ── Complete Submit Flow Tests ──

    @Test
    fun `submitTransaction validates and saves successfully`() =
        runUnitTest {
            val result = manager.submitTransaction(validState, transactionId = null)

            assertTrue("Submit should succeed", result.isSuccess)
            val allTransactions = transactionRepository.getAllTransactions().first()
            assertEquals(1, allTransactions.size)
        }

    @Test
    fun `submitTransaction fails validation when title is empty`() =
        runUnitTest {
            val invalidState = validState.copy(title = "")

            val result = manager.submitTransaction(invalidState, transactionId = null)

            assertTrue("Submit should fail", result.isFailure)
            assertEquals(
                AddTransactionConstant.ERROR_REQUIRED_TITLE_AMOUNT,
                result.exceptionOrNull()?.message,
            )
        }

    @Test
    fun `submitTransaction respects amount sign based on type`() =
        runUnitTest {
            val expenseState =
                validState.copy(
                    selectedType = TransactionType.EXPENSE,
                    amount = "50.00",
                )

            manager.submitTransaction(expenseState, transactionId = null).getOrNull()

            val allTransactions = transactionRepository.getAllTransactions().first()
            val saved = allTransactions.first()
            assertEquals(-50.0, saved.amount, 0.01)
        }
}
