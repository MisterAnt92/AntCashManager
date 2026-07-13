package com.antcashmanager.android.ui.transaction_add

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.ui.screen.transactionAdd.AddTransactionEvent
import com.antcashmanager.android.ui.screen.transactionAdd.AddTransactionStep
import com.antcashmanager.android.ui.screen.transactionAdd.AddTransactionViewModel
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
class AddTransactionViewModelTest : BaseUnitTest() {

    private lateinit var mockTransactionRepository: TransactionRepository
    private lateinit var mockCategoryRepository: CategoryRepository
    private lateinit var mockSettingsRepository: SettingsRepository
    private lateinit var viewModel: AddTransactionViewModel

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

            override suspend fun updateCategoryData(
                categoryName: String,
                icon: String,
                color: Long
            ) {
                // No-op for test
            }

            // Metodi per suggerimenti
            override fun getDistinctTitles() = flowOf(listOf("Spesa", "Carburante", "Ristorante"))
            override fun getDistinctPayees() = flowOf(listOf("Supermercato", "Stazione"))
            override fun getDistinctNotes() = flowOf(listOf("Cena con amici", "Spesa mensile"))
            override fun getDistinctLocations() = flowOf(listOf("Milano", "Roma"))
            override fun getDistinctTags() = flowOf(listOf("Food", "Transport", "Shopping"))
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

        mockSettingsRepository = mockk(relaxed = true) {
            every { getMealVoucherValue() } returns flowOf(5.29)
        }
    }

    // ── Creazione nuova transazione ──

    @Test
    fun newTransaction_shouldStartAtCategorySelection_whenInitialized() = runViewModelTest {
        viewModel = createViewModel()

        val state = viewModel.state.value
        assertFalse("Should not be in modifying mode", state.isModifying)
        assertEquals(
            "Should start at CATEGORY_SELECTION",
            AddTransactionStep.CATEGORY_SELECTION,
            state.currentStep,
        )
    }

    @Test
    fun categorySelection_shouldAutoAdvanceToDetails_whenInCreationMode() = runViewModelTest {
        viewModel = createViewModel()
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
    fun incomeCategory_shouldAutoSelectIncomeType_whenSelected() = runViewModelTest {
        viewModel = createViewModel()
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
    fun editingTransaction_shouldStartAtDetails_whenInModifyingMode() = runViewModelTest {
        viewModel = createViewModel(transactionId = 1L)
        advanceUntilLoaded()

        val state = viewModel.state.value
        assertFalse("Should not be loading", state.isLoading)
        assertTrue("Should be in modifying mode", state.isModifying)
        assertEquals("Should start at DETAILS", AddTransactionStep.DETAILS, state.currentStep)
        assertEquals("Title should be loaded", "Test Transaction", state.title)
        assertEquals("Amount should be loaded", "100.0", state.amount)
    }

    @Test
    fun editingTransaction_shouldLoadAllFields_whenTransactionExists() = runViewModelTest {
        viewModel = createViewModel(transactionId = 1L)
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
    fun onEvent_shouldOpenCategoryDialog_whenEditCategoryEventReceived() = runViewModelTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.EditCategory)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("Category dialog should be open", viewModel.state.value.showCategoryDialog)
    }

    @Test
    fun onEvent_shouldOpenCategoryDialog_whenEditCategoryInModifyingMode() = runViewModelTest {
        viewModel = createViewModel(transactionId = 1L)
        advanceUntilLoaded()

        viewModel.onEvent(AddTransactionEvent.EditCategory)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("Category dialog should be open", viewModel.state.value.showCategoryDialog)
    }

    @Test
    fun onEvent_shouldCloseCategoryDialog_whenDismissCategoryDialogEventReceived() = runViewModelTest {
        viewModel = createViewModel(transactionId = 1L)
        advanceUntilLoaded()

        viewModel.onEvent(AddTransactionEvent.EditCategory)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue("Dialog should be open", viewModel.state.value.showCategoryDialog)

        viewModel.onEvent(AddTransactionEvent.DismissCategoryDialog)
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse("Dialog should be closed", viewModel.state.value.showCategoryDialog)
    }

    @Test
    fun onEvent_shouldCloseCategoryDialogAndSelectCategory_whenCategorySelected() = runViewModelTest {
        viewModel = createViewModel(transactionId = 1L)
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
    fun categorySelection_shouldNotAutoAdvance_whenInModifyingMode() = runViewModelTest {
        viewModel = createViewModel(transactionId = 1L)
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
    fun onEvent_shouldOpenTypeDialog_whenEditTypeEventReceived() = runViewModelTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.EditType)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("Type dialog should be open", viewModel.state.value.showTypeDialog)
    }

    @Test
    fun onEvent_shouldOpenTypeDialog_whenEditTypeInModifyingMode() = runViewModelTest {
        viewModel = createViewModel(transactionId = 1L)
        advanceUntilLoaded()

        viewModel.onEvent(AddTransactionEvent.EditType)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("Type dialog should be open", viewModel.state.value.showTypeDialog)
    }

    @Test
    fun onEvent_shouldCloseTypeDialogAndSelectType_whenTypeSelected() = runViewModelTest {
        viewModel = createViewModel(transactionId = 1L)
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
    fun onEvent_shouldOpenDatePicker_whenEditDateEventReceived() = runViewModelTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.EditDate)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("Date picker should be shown", viewModel.state.value.showDatePicker)
    }

    @Test
    fun onEvent_shouldCloseDatePicker_whenDismissDatePickerEventReceived() = runViewModelTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.EditDate)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue("Date picker should be open", viewModel.state.value.showDatePicker)

        viewModel.onEvent(AddTransactionEvent.DismissDatePicker)
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse("Date picker should be closed", viewModel.state.value.showDatePicker)
    }

    @Test
    fun onEvent_shouldUpdateTimestamp_whenUpdateTimestampEventReceived() = runViewModelTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val newTimestamp = 1700000000000L
        viewModel.onEvent(AddTransactionEvent.UpdateTimestamp(newTimestamp))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Timestamp should be updated", newTimestamp, viewModel.state.value.timestamp)
    }

    // ── Navigazione ──

    @Test
    fun onEvent_shouldGoBackToCategorySelection_whenPreviousStepInCreationMode() =
        runViewModelTest {
            viewModel = createViewModel()
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
    fun onEvent_shouldDoNothing_whenPreviousStepInModifyingMode() = runViewModelTest {
        viewModel = createViewModel(transactionId = 1L)
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
    fun onEvent_shouldUpdateAllFields_whenFieldUpdateEventsReceived() = runViewModelTest {
        viewModel = createViewModel()
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
    fun submit_shouldFail_whenCategoryAndTypeAreMissing() = runViewModelTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.UpdateTitle("Test"))
        viewModel.onEvent(AddTransactionEvent.UpdateAmount("10"))
        viewModel.onEvent(AddTransactionEvent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull("Should have an error", viewModel.state.value.error)
        assertFalse("Should not be saved", viewModel.state.value.isTransactionSaved)
    }

    @Test
    fun submit_shouldFail_whenTitleIsMissing() = runViewModelTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[0]))
        viewModel.onEvent(AddTransactionEvent.UpdateAmount("10"))
        viewModel.onEvent(AddTransactionEvent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull("Should have an error", viewModel.state.value.error)
    }

    @Test
    fun submit_shouldSucceed_whenAllRequiredFieldsAreProvided() = runViewModelTest {
        viewModel = createViewModel()
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
    fun submit_shouldUpdateTransaction_whenInModifyingMode() = runViewModelTest {
        viewModel = createViewModel(transactionId = 1L)
        advanceUntilLoaded()

        viewModel.onEvent(AddTransactionEvent.UpdateTitle("Updated Title"))
        viewModel.onEvent(AddTransactionEvent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("Should be saved", viewModel.state.value.isTransactionSaved)
    }

    // ── Reset ──

    @Test
    fun reset_shouldClearAllFields_whenCalled() = runViewModelTest {
        viewModel = createViewModel()
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

    // ── Suggerimenti Transazioni ──

    @Test
    fun init_shouldLoadSuggestions_whenViewModelIsCreated() = runViewModelTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Should have title suggestions", 3, state.titleSuggestions.size)
        assertEquals("Should have payee suggestions", 2, state.payeeSuggestions.size)
        assertEquals("Should have notes suggestions", 2, state.notesSuggestions.size)
        assertEquals("Should have location suggestions", 2, state.locationSuggestions.size)
        assertEquals("Should have tags suggestions", 3, state.tagsSuggestions.size)

        assertTrue("Should contain Spesa", state.titleSuggestions.contains("Spesa"))
        assertTrue("Should contain Milano", state.locationSuggestions.contains("Milano"))
    }

    @Test
    fun init_shouldLoadSuggestions_whenEditingTransaction() = runViewModelTest {
        viewModel = createViewModel(transactionId = 1L)
        advanceUntilLoaded()

        val state = viewModel.state.value
        assertTrue("Should have suggestions loaded", state.titleSuggestions.isNotEmpty())
        assertTrue("Should have payee suggestions", state.payeeSuggestions.isNotEmpty())
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

    private fun createViewModel(transactionId: Long? = null): AddTransactionViewModel =
        AddTransactionViewModel(
            transactionRepository = mockTransactionRepository,
            categoryRepository = mockCategoryRepository,
            settingsRepository = mockSettingsRepository,
            transactionId = transactionId,
            dispatcher = testDispatcher,
        )
}
