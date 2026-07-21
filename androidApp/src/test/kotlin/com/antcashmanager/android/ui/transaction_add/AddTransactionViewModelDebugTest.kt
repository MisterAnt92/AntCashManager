package com.antcashmanager.android.ui.transaction_add

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.testutil.FakeCategoryRepository
import com.antcashmanager.android.testutil.FakeSettingsRepository
import com.antcashmanager.android.testutil.FakeTransactionRepository
import com.antcashmanager.android.ui.screen.transactions.addImport.AddTransactionViewModel
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * Test debug per capire il problema con i test del ViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelDebugTest : BaseUnitTest() {

    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var categoryRepository: FakeCategoryRepository
    private lateinit var settingsRepository: FakeSettingsRepository

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
        transactionRepository = FakeTransactionRepository(listOf(mockTransaction))
        categoryRepository = FakeCategoryRepository(mockCategories)
        settingsRepository = FakeSettingsRepository()
    }

    @Test
    fun init_shouldCheckInitialState_whenViewModelIsCreated() = runViewModelTest {
        val viewModel = AddTransactionViewModel(
            transactionRepository,
            categoryRepository,
            settingsRepository
        )

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
    fun init_shouldCheckModifyingMode_whenViewModelIsCreatedWithTransactionId() = runViewModelTest {
        val viewModel = AddTransactionViewModel(
            transactionRepository,
            categoryRepository,
            settingsRepository,
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
