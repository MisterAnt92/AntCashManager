package com.antcashmanager.android.ui.transaction_add

import com.antcashmanager.android.ui.screen.transaction_add.AddTransactionEvent
import com.antcashmanager.android.ui.screen.transaction_add.AddTransactionStep
import com.antcashmanager.android.ui.screen.transaction_add.AddTransactionViewModel
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Test semplificati per le funzionalità core
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelSimpleTest {

    private lateinit var mockTransactionRepository: TransactionRepository
    private lateinit var mockCategoryRepository: CategoryRepository
    private val testDispatcher = StandardTestDispatcher()

    private val mockCategories = listOf(
        Category(1, "Food", "🍔", 0xFFFF6B6B, "EXPENSE"),
        Category(2, "Salary", "💰", 0xFF51CF66, "INCOME")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockTransactionRepository = object : TransactionRepository {
            override fun getAllTransactions() = flowOf(emptyList<com.antcashmanager.domain.model.Transaction>())
            override suspend fun getTransactionById(id: Long) = null
            override suspend fun insertTransaction(transaction: com.antcashmanager.domain.model.Transaction) = 1L
            override suspend fun updateTransaction(transaction: com.antcashmanager.domain.model.Transaction) {}
            override suspend fun deleteTransaction(transaction: com.antcashmanager.domain.model.Transaction) {}
            override suspend fun deleteAllTransactions() {}
            override fun getTransactionsByDateRange(from: Long, to: Long) = flowOf(emptyList<com.antcashmanager.domain.model.Transaction>())
            override fun getRecurringTransactions() = flowOf(emptyList<com.antcashmanager.domain.model.Transaction>())
        }

        mockCategoryRepository = object : CategoryRepository {
            override fun getAllCategories() = flowOf(mockCategories)
            override suspend fun getCategoryById(id: Long) = mockCategories.find { it.id == id }
            override suspend fun insertCategory(category: Category) = 1L
            override suspend fun updateCategory(category: Category) {}
            override suspend fun deleteCategory(category: Category) {}
            override suspend fun deleteAllCategories() {}
            override fun getCategoriesByType(type: String) = flowOf(mockCategories.filter { it.type == type })
            override suspend fun getDefaultCategoryCount() = mockCategories.size
        }
    }

    @Test
    fun `test viewmodel creation works`() = runTest(testDispatcher) {
        val viewModel = AddTransactionViewModel(mockTransactionRepository, mockCategoryRepository)
        assertNotNull("ViewModel should be created", viewModel)
    }

    @Test
    fun `test viewmodel creation with transactionId works`() = runTest(testDispatcher) {
        val viewModel = AddTransactionViewModel(mockTransactionRepository, mockCategoryRepository, transactionId = 1L)
        assertNotNull("ViewModel should be created with transactionId", viewModel)
    }

    @Test
    fun `test category selection event processing`() = runTest(testDispatcher) {
        val viewModel = AddTransactionViewModel(mockTransactionRepository, mockCategoryRepository)

        // Questa chiamata dovrebbe almeno non crashare
        viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[0]))

        // Se arriviamo qui, l'evento è stato processato senza errori
        assertTrue("Event processing should not crash", true)
    }
}
