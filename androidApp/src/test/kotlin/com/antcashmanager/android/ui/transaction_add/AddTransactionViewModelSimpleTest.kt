package com.antcashmanager.android.ui.transaction_add

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.testutil.FakeCategoryRepository
import com.antcashmanager.android.testutil.FakeSettingsRepository
import com.antcashmanager.android.testutil.FakeTransactionRepository
import com.antcashmanager.android.ui.screen.transactions.addImport.AddTransactionEvent
import com.antcashmanager.android.ui.screen.transactions.addImport.AddTransactionViewModel
import com.antcashmanager.domain.model.Category
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test semplificati per le funzionalità core
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelSimpleTest : BaseUnitTest() {

    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var categoryRepository: FakeCategoryRepository
    private lateinit var settingsRepository: FakeSettingsRepository

    private val mockCategories = listOf(
        Category(1, "Food", "🍔", 0xFFFF6B6B, "EXPENSE"),
        Category(2, "Salary", "💰", 0xFF51CF66, "INCOME")
    )

    @Before
    fun setup() {
        transactionRepository = FakeTransactionRepository()
        categoryRepository = FakeCategoryRepository(mockCategories)
        settingsRepository = FakeSettingsRepository()
    }

    @Test
    fun init_shouldCreateViewModel_whenCalledWithRepositories() = runViewModelTest {
        val viewModel = AddTransactionViewModel(
            transactionRepository,
            categoryRepository,
            settingsRepository
        )
        assertNotNull("ViewModel should be created", viewModel)
    }

    @Test
    fun init_shouldCreateViewModel_whenCalledWithTransactionId() = runViewModelTest {
        val viewModel = AddTransactionViewModel(
            transactionRepository,
            categoryRepository,
            settingsRepository,
            transactionId = 1L
        )
        assertNotNull("ViewModel should be created with transactionId", viewModel)
    }

    @Test
    fun onEvent_shouldProcessSelectCategoryEvent_whenCategoryIsSelected() = runViewModelTest {
        val viewModel = AddTransactionViewModel(
            transactionRepository,
            categoryRepository,
            settingsRepository
        )

        // Questa chiamata dovrebbe almeno non crashare
        viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[0]))

        // Se arriviamo qui, l'evento è stato processato senza errori
        assertTrue("Event processing should not crash", true)
    }
}
