package com.antcashmanager.android.ui.transaction_add

import com.antcashmanager.android.ui.screen.transactionAdd.AddTransactionEvent
import com.antcashmanager.android.ui.screen.transactionAdd.AddTransactionStep
import com.antcashmanager.android.ui.screen.transactionAdd.AddTransactionViewModel
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test per verificare il funzionamento del ViewModel in modalità aggiunta e modifica.
 *
 * Flusso semplificato:
 * - Nuova transazione: CATEGORY_SELECTION → DETAILS (salvataggio diretto)
 * - Modifica transazione: DETAILS (salvataggio diretto)
 * - Categoria, Tipo e Data sono sempre modificabili al tap tramite dialog.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelTest {

    private lateinit var mockTransactionRepository: TransactionRepository
    private lateinit var mockCategoryRepository: CategoryRepository
    private lateinit var viewModel: AddTransactionViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val mockCategories = listOf(
        Category(1, "Food", "🍔", 0xFFFF6B6B, "EXPENSE"),
        Category(2, "Salary", "💰", 0xFF51CF66, "INCOME"),
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
        recurrenceInterval = "",
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

    // ── Creazione nuova transazione ──

    @Test
    fun `new transaction starts at CATEGORY_SELECTION`() = runTest {
        viewModel = AddTransactionViewModel(mockTransactionRepository, mockCategoryRepository)

        val state = viewModel.state.value
        assertFalse("Should not be in modifying mode", state.isModifying)
        assertEquals(
            "Should start at CATEGORY_SELECTION",
            AddTransactionStep.CATEGORY_SELECTION,
            state.currentStep,
        )
    }

    @Test
    fun `category selection auto-advances to DETAILS in creation mode`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(mockTransactionRepository, mockCategoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Categories should be loaded", 2, viewModel.state.value.categories.size)

        viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[0]))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Should select Food category", mockCategories[0], state.selectedCategory)
        assertEquals(
            "Type should be auto-set to EXPENSE",
            TransactionType.EXPENSE,
            state.selectedType
        )
        assertEquals(
            "Should auto-advance to DETAILS",
            AddTransactionStep.DETAILS,
            state.currentStep,
        )
    }

    @Test
    fun `income category auto-selects INCOME type`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(mockTransactionRepository, mockCategoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[1]))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Should select Salary category", mockCategories[1], state.selectedCategory)
        assertEquals(
            "Type should be auto-set to INCOME",
            TransactionType.INCOME,
            state.selectedType
        )
    }

    // ── Modifica transazione esistente ──

    @Test
    fun `editing transaction starts at DETAILS in modifying mode`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            mockTransactionRepository,
            mockCategoryRepository,
            transactionId = 1L,
        )
        advanceUntilLoaded()

        val state = viewModel.state.value
        assertFalse("Should not be loading", state.isLoading)
        assertTrue("Should be in modifying mode", state.isModifying)
        assertEquals("Should start at DETAILS", AddTransactionStep.DETAILS, state.currentStep)
        assertEquals("Title should be loaded", "Test Transaction", state.title)
        assertEquals("Amount should be loaded", "100.0", state.amount)
    }

    @Test
    fun `editing transaction loads all fields`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            mockTransactionRepository,
            mockCategoryRepository,
            transactionId = 1L,
        )
        advanceUntilLoaded()

        val state = viewModel.state.value
        assertEquals("Notes should be loaded", "Test notes", state.notes)
        assertEquals("Payee should be loaded", "Test payee", state.payee)
        assertEquals("Location should be loaded", "Test location", state.location)
        assertEquals("Tags should be loaded", "test,tag", state.tags)
        assertFalse("Should not be recurring", state.isRecurring)
    }

    // ── EditCategory: apre sempre il dialog ──

    @Test
    fun `EditCategory opens category dialog`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(mockTransactionRepository, mockCategoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.EditCategory)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("Category dialog should be open", viewModel.state.value.showCategoryDialog)
    }

    @Test
    fun `EditCategory opens dialog in modifying mode`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            mockTransactionRepository,
            mockCategoryRepository,
            transactionId = 1L,
        )
        advanceUntilLoaded()

        viewModel.onEvent(AddTransactionEvent.EditCategory)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("Category dialog should be open", viewModel.state.value.showCategoryDialog)
    }

    @Test
    fun `DismissCategoryDialog closes dialog`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            mockTransactionRepository,
            mockCategoryRepository,
            transactionId = 1L,
        )
        advanceUntilLoaded()

        viewModel.onEvent(AddTransactionEvent.EditCategory)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue("Dialog should be open", viewModel.state.value.showCategoryDialog)

        viewModel.onEvent(AddTransactionEvent.DismissCategoryDialog)
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse("Dialog should be closed", viewModel.state.value.showCategoryDialog)
    }

    @Test
    fun `selecting category from dialog closes it`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            mockTransactionRepository,
            mockCategoryRepository,
            transactionId = 1L,
        )
        advanceUntilLoaded()

        viewModel.onEvent(AddTransactionEvent.EditCategory)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue("Dialog should be open", viewModel.state.value.showCategoryDialog)

        viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[1]))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse("Dialog should be closed", state.showCategoryDialog)
        assertEquals("Should select new category", mockCategories[1], state.selectedCategory)
        assertEquals(
            "Should remain at DETAILS in modifying mode",
            AddTransactionStep.DETAILS,
            state.currentStep
        )
    }

    @Test
    fun `category selection does NOT auto-advance in modifying mode`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            mockTransactionRepository,
            mockCategoryRepository,
            transactionId = 1L,
        )
        advanceUntilLoaded()

        assertEquals(
            "Should be at DETAILS",
            AddTransactionStep.DETAILS,
            viewModel.state.value.currentStep
        )

        viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[1]))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            "Step should remain at DETAILS",
            AddTransactionStep.DETAILS,
            viewModel.state.value.currentStep,
        )
    }

    // ── EditType: apre sempre il dialog ──

    @Test
    fun `EditType opens type dialog`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(mockTransactionRepository, mockCategoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.EditType)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("Type dialog should be open", viewModel.state.value.showTypeDialog)
    }

    @Test
    fun `EditType opens dialog in modifying mode`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            mockTransactionRepository,
            mockCategoryRepository,
            transactionId = 1L,
        )
        advanceUntilLoaded()

        viewModel.onEvent(AddTransactionEvent.EditType)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("Type dialog should be open", viewModel.state.value.showTypeDialog)
    }

    @Test
    fun `selecting type from dialog closes it`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            mockTransactionRepository,
            mockCategoryRepository,
            transactionId = 1L,
        )
        advanceUntilLoaded()

        viewModel.onEvent(AddTransactionEvent.EditType)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue("Dialog should be open", viewModel.state.value.showTypeDialog)

        viewModel.onEvent(AddTransactionEvent.SelectType(TransactionType.INCOME))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse("Dialog should be closed", state.showTypeDialog)
        assertEquals("Should select INCOME", TransactionType.INCOME, state.selectedType)
    }

    // ── EditDate: apre il date picker ──

    @Test
    fun `EditDate opens date picker`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(mockTransactionRepository, mockCategoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.EditDate)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("Date picker should be shown", viewModel.state.value.showDatePicker)
    }

    @Test
    fun `DismissDatePicker closes date picker`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(mockTransactionRepository, mockCategoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.EditDate)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue("Date picker should be open", viewModel.state.value.showDatePicker)

        viewModel.onEvent(AddTransactionEvent.DismissDatePicker)
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse("Date picker should be closed", viewModel.state.value.showDatePicker)
    }

    @Test
    fun `UpdateTimestamp updates the date`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(mockTransactionRepository, mockCategoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val newTimestamp = 1700000000000L
        viewModel.onEvent(AddTransactionEvent.UpdateTimestamp(newTimestamp))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Timestamp should be updated", newTimestamp, viewModel.state.value.timestamp)
    }

    // ── Navigazione ──

    @Test
    fun `PreviousStep goes back to CATEGORY_SELECTION from DETAILS in creation mode`() =
        runTest(testDispatcher) {
            viewModel = AddTransactionViewModel(mockTransactionRepository, mockCategoryRepository)
            testDispatcher.scheduler.advanceUntilIdle()

            // Avanza a DETAILS selezionando una categoria
            viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[0]))
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(AddTransactionStep.DETAILS, viewModel.state.value.currentStep)

            // Torna indietro
            viewModel.onEvent(AddTransactionEvent.PreviousStep)
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(
                "Should go back to CATEGORY_SELECTION",
                AddTransactionStep.CATEGORY_SELECTION,
                viewModel.state.value.currentStep,
            )
        }

    @Test
    fun `PreviousStep does nothing in modifying mode`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            mockTransactionRepository,
            mockCategoryRepository,
            transactionId = 1L,
        )
        advanceUntilLoaded()

        viewModel.onEvent(AddTransactionEvent.PreviousStep)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            "Should remain at DETAILS in modifying mode",
            AddTransactionStep.DETAILS,
            viewModel.state.value.currentStep,
        )
    }

    // ── Aggiornamento campi ──

    @Test
    fun `field updates work correctly`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(mockTransactionRepository, mockCategoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.UpdateTitle("New Title"))
        viewModel.onEvent(AddTransactionEvent.UpdateAmount("42.50"))
        viewModel.onEvent(AddTransactionEvent.UpdateNotes("Some notes"))
        viewModel.onEvent(AddTransactionEvent.UpdatePayee("John"))
        viewModel.onEvent(AddTransactionEvent.UpdateLocation("Rome"))
        viewModel.onEvent(AddTransactionEvent.UpdateTags("food,lunch"))
        viewModel.onEvent(AddTransactionEvent.SetRecurring(true))
        viewModel.onEvent(AddTransactionEvent.UpdateRecurrenceInterval("monthly"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("New Title", state.title)
        assertEquals("42.50", state.amount)
        assertEquals("Some notes", state.notes)
        assertEquals("John", state.payee)
        assertEquals("Rome", state.location)
        assertEquals("food,lunch", state.tags)
        assertTrue(state.isRecurring)
        assertEquals("monthly", state.recurrenceInterval)
    }

    // ── Submit ──

    @Test
    fun `submit fails without category and type`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(mockTransactionRepository, mockCategoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.UpdateTitle("Test"))
        viewModel.onEvent(AddTransactionEvent.UpdateAmount("10"))
        viewModel.onEvent(AddTransactionEvent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull("Should have an error", viewModel.state.value.error)
        assertFalse("Should not be saved", viewModel.state.value.isTransactionSaved)
    }

    @Test
    fun `submit fails without title`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(mockTransactionRepository, mockCategoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[0]))
        viewModel.onEvent(AddTransactionEvent.UpdateAmount("10"))
        viewModel.onEvent(AddTransactionEvent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull("Should have an error", viewModel.state.value.error)
    }

    @Test
    fun `submit succeeds with all required fields`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(mockTransactionRepository, mockCategoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[0]))
        viewModel.onEvent(AddTransactionEvent.UpdateTitle("Pizza"))
        viewModel.onEvent(AddTransactionEvent.UpdateAmount("12.50"))
        viewModel.onEvent(AddTransactionEvent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("Should be saved", viewModel.state.value.isTransactionSaved)
        assertNull("Should have no error", viewModel.state.value.error)
    }

    @Test
    fun `submit in modifying mode updates transaction`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            mockTransactionRepository,
            mockCategoryRepository,
            transactionId = 1L,
        )
        advanceUntilLoaded()

        viewModel.onEvent(AddTransactionEvent.UpdateTitle("Updated Title"))
        viewModel.onEvent(AddTransactionEvent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("Should be saved", viewModel.state.value.isTransactionSaved)
    }

    // ── Reset ──

    @Test
    fun `reset clears the state`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(mockTransactionRepository, mockCategoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.UpdateTitle("Test"))
        viewModel.onEvent(AddTransactionEvent.UpdateAmount("10"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.reset()

        val state = viewModel.state.value
        assertEquals("Title should be empty", "", state.title)
        assertEquals("Amount should be empty", "", state.amount)
        assertNull("Category should be null", state.selectedCategory)
        assertNull("Type should be null", state.selectedType)
        assertEquals(
            "Should be back at CATEGORY_SELECTION",
            AddTransactionStep.CATEGORY_SELECTION,
            state.currentStep,
        )
    }

    // ── Helper ──

    private fun advanceUntilLoaded() {
        testDispatcher.scheduler.advanceUntilIdle()
        var attempts = 0
        while (viewModel.state.value.isLoading && attempts < 10) {
            testDispatcher.scheduler.advanceUntilIdle()
            attempts++
        }
    }
}
