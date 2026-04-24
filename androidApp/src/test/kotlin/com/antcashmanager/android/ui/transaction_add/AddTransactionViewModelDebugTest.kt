package com.antcashmanager.android.ui.transaction_add

import com.antcashmanager.android.ui.screen.transactionAdd.AddTransactionViewModel
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * Test debug per capire il problema con i test del ViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelDebugTest {

    private lateinit var mockTransactionRepository: TransactionRepository
    private lateinit var mockCategoryRepository: CategoryRepository
    private val testDispatcher = StandardTestDispatcher()

    private val mockCategories = listOf(
        Category(1, "Food", "🍔", 0xFFFF6B6B, "EXPENSE"),
        Category(2, "Salary", "💰", 0xFF51CF66, "INCOME")
    )

    private val mockTransaction = Transaction(
        id = 1L,
        title = "Test Transaction",
        amount = 100.0,
        category = "Food",
        type = TransactionType.EXPENSE,
        timestamp = System.currentTimeMillis(),
        notes = "Test notes",
        payee = "Test payee",
        location = "Test location",
        tags = "test,tag",
        isRecurring = false,
        recurrenceInterval = ""
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockTransactionRepository = object : TransactionRepository {
            override fun getAllTransactions() = flowOf(emptyList<Transaction>())
            override suspend fun getTransactionById(id: Long) =
                if (id == 1L) mockTransaction else null

            override suspend fun insertTransaction(transaction: Transaction) = 1L
            override suspend fun updateTransaction(transaction: Transaction) {}
            override suspend fun deleteTransaction(transaction: Transaction) {}
            override suspend fun deleteAllTransactions() {}
            override fun getTransactionsByDateRange(from: Long, to: Long) =
                flowOf(emptyList<Transaction>())

            override fun getRecurringTransactions() = flowOf(emptyList<Transaction>())

            override suspend fun updateCategoryData(categoryName: String, icon: String, color: Long) {
                // No-op for test
            }

            override fun getDistinctTitles() = flowOf(emptyList<String>())
            override fun getDistinctPayees() = flowOf(emptyList<String>())
            override fun getDistinctNotes() = flowOf(emptyList<String>())
            override fun getDistinctLocations() = flowOf(emptyList<String>())
            override fun getDistinctTags() = flowOf(emptyList<String>())
        }

        mockCategoryRepository = object : CategoryRepository {
            override fun getAllCategories() = flowOf(mockCategories)
            override suspend fun getCategoryById(id: Long) = mockCategories.find { it.id == id }
            override suspend fun insertCategory(category: Category) = 1L
            override suspend fun updateCategory(category: Category) {}
            override suspend fun deleteCategory(category: Category) {}
            override suspend fun deleteAllCategories() {}
            override fun getCategoriesByType(type: String) =
                flowOf(mockCategories.filter { it.type == type })

            override suspend fun getDefaultCategoryCount() = mockCategories.size

            override suspend fun getCategoryByName(name: String): Category? =
                mockCategories.find { it.name == name }
        }
    }

    @Test
    fun `debug test - check initial state`() = runTest(testDispatcher) {
        val viewModel = AddTransactionViewModel(mockTransactionRepository, mockCategoryRepository)

        // Avanza il dispatcher per permettere l'inizializzazione
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.first()
        println("Debug: Initial state loading: ${state.isLoading}")
        println("Debug: Initial state categories count: ${state.categories.size}")
        println("Debug: Initial state isModifying: ${state.isModifying}")
        println("Debug: Initial state currentStep: ${state.currentStep}")

        // Per ora verifichiamo solo che il ViewModel si sia creato
        assertNotNull("ViewModel should be created", viewModel)
    }

    @Test
    fun `debug test - check modifying mode`() = runTest(testDispatcher) {
        val viewModel = AddTransactionViewModel(
            mockTransactionRepository,
            mockCategoryRepository,
            transactionId = 1L
        )

        // Avanza il dispatcher per permettere l'inizializzazione
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.first()
        println("Debug: Edit mode state loading: ${state.isLoading}")
        println("Debug: Edit mode state categories count: ${state.categories.size}")
        println("Debug: Edit mode state isModifying: ${state.isModifying}")
        println("Debug: Edit mode state currentStep: ${state.currentStep}")
        println("Debug: Edit mode state selectedCategory: ${state.selectedCategory}")

        // Per ora verifichiamo solo che il ViewModel si sia creato
        assertNotNull("ViewModel should be created", viewModel)
    }
}
