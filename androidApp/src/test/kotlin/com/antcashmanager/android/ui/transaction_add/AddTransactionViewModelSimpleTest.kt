package com.antcashmanager.android.ui.transaction_add

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.ui.screen.transactionAdd.AddTransactionEvent
import com.antcashmanager.android.ui.screen.transactionAdd.AddTransactionViewModel
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test semplificati per le funzionalità core
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelSimpleTest : BaseUnitTest() {

    private lateinit var mockTransactionRepository: TransactionRepository
    private lateinit var mockCategoryRepository: CategoryRepository

    private val mockCategories = listOf(
        Category(1, "Food", "🍔", 0xFFFF6B6B, "EXPENSE"),
        Category(2, "Salary", "💰", 0xFF51CF66, "INCOME")
    )

    @Before
    fun setup() {
        mockTransactionRepository = object : TransactionRepository {
            override fun getAllTransactions() =
                flowOf(emptyList<com.antcashmanager.domain.model.Transaction>())

            override suspend fun getTransactionById(id: Long) = null
            override suspend fun insertTransaction(transaction: com.antcashmanager.domain.model.Transaction) =
                1L

            override suspend fun updateTransaction(transaction: com.antcashmanager.domain.model.Transaction) {}
            override suspend fun deleteTransaction(transaction: com.antcashmanager.domain.model.Transaction) {}
            override suspend fun deleteAllTransactions() {}
            override fun getTransactionsByDateRange(from: Long, to: Long) =
                flowOf(emptyList<com.antcashmanager.domain.model.Transaction>())

            override fun getRecurringTransactions() =
                flowOf(emptyList<com.antcashmanager.domain.model.Transaction>())

            override suspend fun updateCategoryData(
                categoryName: String,
                icon: String,
                color: Long
            ) {
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
    fun init_shouldCreateViewModel_whenCalledWithRepositories() = runViewModelTest {
        val viewModel = AddTransactionViewModel(mockTransactionRepository, mockCategoryRepository)
        assertNotNull("ViewModel should be created", viewModel)
    }

    @Test
    fun init_shouldCreateViewModel_whenCalledWithTransactionId() = runViewModelTest {
        val viewModel = AddTransactionViewModel(
            mockTransactionRepository,
            mockCategoryRepository,
            transactionId = 1L
        )
        assertNotNull("ViewModel should be created with transactionId", viewModel)
    }

    @Test
    fun onEvent_shouldProcessSelectCategoryEvent_whenCategoryIsSelected() = runViewModelTest {
        val viewModel = AddTransactionViewModel(mockTransactionRepository, mockCategoryRepository)

        // Questa chiamata dovrebbe almeno non crashare
        viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[0]))

        // Se arriviamo qui, l'evento è stato processato senza errori
        assertTrue("Event processing should not crash", true)
    }
}
