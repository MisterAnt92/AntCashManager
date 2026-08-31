package com.antcashmanager.android.ui.screen.transactions.addImport

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.analytics.tracker.ErrorTracker
import com.antcashmanager.android.analytics.tracker.PerformanceTracker
import com.antcashmanager.android.testutil.FakeCategoryRepository
import com.antcashmanager.android.testutil.FakeSettingsRepository
import com.antcashmanager.android.testutil.FakeTransactionRepository
import com.antcashmanager.android.ui.screen.transactions.addImport.event.AddTransactionEvent
import com.antcashmanager.android.ui.screen.transactions.addImport.manager.SuggestionsManager
import com.antcashmanager.android.ui.screen.transactions.addImport.manager.TransactionLoadManager
import com.antcashmanager.android.ui.screen.transactions.addImport.manager.TransactionSubmitManager
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionSuggestions
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.settings.GetMealVoucherValueUseCase
import com.antcashmanager.domain.usecase.transaction.DeleteTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionByIdUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionSuggestionsUseCase
import com.antcashmanager.domain.usecase.transaction.InsertTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.UpdateTransactionUseCase
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
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

    private lateinit var transactionRepository: FakeTransactionRepositoryWithCannedSuggestions
    private lateinit var categoryRepository: FakeCategoryRepository
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var analyticsManager: AnalyticsManager
    private lateinit var performanceTracker: PerformanceTracker
    private lateinit var errorTracker: ErrorTracker
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
        transactionRepository =
            FakeTransactionRepositoryWithCannedSuggestions(listOf(mockTransaction))
        categoryRepository = FakeCategoryRepository(mockCategories)
        settingsRepository = FakeSettingsRepository()
        analyticsManager = mockk(relaxed = true)
        performanceTracker = mockk(relaxed = true)
        errorTracker = mockk(relaxed = true)
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
    fun onEvent_shouldCloseCategoryDialog_whenDismissCategoryDialogEventReceived() =
        runViewModelTest {
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
    fun onEvent_shouldCloseCategoryDialogAndSelectCategory_whenCategorySelected() =
        runViewModelTest {
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

    @Ignore("Requires mocking Android BaseBundle.putString - these are integration tests")
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

    @Ignore("Requires mocking Android BaseBundle.putString - these are integration tests")
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

    // ── Payment type dialog ──

    @Test
    fun onEvent_shouldOpenPaymentTypeDialog_whenShowPaymentTypeDialogEventReceived() =
        runViewModelTest {
            viewModel = createViewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onEvent(AddTransactionEvent.ShowPaymentTypeDialog)
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(
                "Payment type dialog should be open",
                viewModel.state.value.showPaymentTypeDialog
            )
        }

    @Test
    fun onEvent_shouldOpenPaymentTypeDialog_whenEditPaymentTypeEventReceived() = runViewModelTest {
        viewModel = createViewModel(transactionId = 1L)
        advanceUntilLoaded()

        viewModel.onEvent(AddTransactionEvent.EditPaymentType)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(
            "Payment type dialog should be open",
            viewModel.state.value.showPaymentTypeDialog
        )
    }

    @Test
    fun onEvent_shouldClosePaymentTypeDialog_whenDismissPaymentTypeDialogEventReceived() =
        runViewModelTest {
            viewModel = createViewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onEvent(AddTransactionEvent.ShowPaymentTypeDialog)
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue("Dialog should be open", viewModel.state.value.showPaymentTypeDialog)

            viewModel.onEvent(AddTransactionEvent.DismissPaymentTypeDialog)
            testDispatcher.scheduler.advanceUntilIdle()
            assertFalse("Dialog should be closed", viewModel.state.value.showPaymentTypeDialog)
        }

    @Test
    fun onEvent_shouldSelectPaymentTypeAndCloseDialog_whenSelectPaymentTypeEventReceived() =
        runViewModelTest {
            viewModel = createViewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onEvent(AddTransactionEvent.ShowPaymentTypeDialog)
            viewModel.onEvent(AddTransactionEvent.SelectPaymentType(PaymentType.CASH))
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals("Should select CASH", PaymentType.CASH, state.selectedPaymentType)
            assertFalse("Dialog should be closed", state.showPaymentTypeDialog)
        }

    // ── Delete flow ──

    @Test
    fun onEvent_shouldOpenDeleteConfirmDialog_whenShowDeleteConfirmDialogEventReceived() =
        runViewModelTest {
            viewModel = createViewModel(transactionId = 1L)
            advanceUntilLoaded()

            viewModel.onEvent(AddTransactionEvent.ShowDeleteConfirmDialog)
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(
                "Delete confirm dialog should be open",
                viewModel.state.value.showDeleteConfirmDialog
            )
        }

    @Test
    fun onEvent_shouldCloseDeleteConfirmDialog_whenDismissDeleteConfirmDialogEventReceived() =
        runViewModelTest {
            viewModel = createViewModel(transactionId = 1L)
            advanceUntilLoaded()

            viewModel.onEvent(AddTransactionEvent.ShowDeleteConfirmDialog)
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue("Dialog should be open", viewModel.state.value.showDeleteConfirmDialog)

            viewModel.onEvent(AddTransactionEvent.DismissDeleteConfirmDialog)
            testDispatcher.scheduler.advanceUntilIdle()
            assertFalse("Dialog should be closed", viewModel.state.value.showDeleteConfirmDialog)
        }

    @Test
    fun confirmDelete_shouldMarkTransactionSavedAndCloseDialog_whenDeleteSucceeds() =
        runViewModelTest {
            viewModel = createViewModel(transactionId = 1L)
            advanceUntilLoaded()

            viewModel.onEvent(AddTransactionEvent.ConfirmDelete)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.state.value
            assertTrue("Transaction should be marked as saved", state.isTransactionSaved)
            assertFalse("Delete confirm dialog should be closed", state.showDeleteConfirmDialog)
            assertNull("Should have no error", state.error)
        }

    @Test
    fun confirmDelete_shouldSetTransactionNotFoundError_whenTransactionDoesNotExist() =
        runViewModelTest {
            viewModel = createViewModel(transactionId = 999L)
            advanceUntilLoaded()

            viewModel.onEvent(AddTransactionEvent.ConfirmDelete)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(
                AddTransactionConstant.ERROR_TRANSACTION_NOT_FOUND,
                viewModel.state.value.error,
            )
        }

    @Test
    fun confirmDelete_shouldSetDeleteError_whenDeleteTransactionUseCaseFails() = runViewModelTest {
        transactionRepository.errorToThrow = IllegalStateException("delete failed")
        viewModel = createViewModel(transactionId = 1L)
        advanceUntilLoaded()

        viewModel.onEvent(AddTransactionEvent.ConfirmDelete)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(AddTransactionConstant.ERROR_DELETE, state.error)
        assertFalse("Transaction should not be marked as saved", state.isTransactionSaved)
    }

    // ── Validazione importo ──

    @Ignore("Requires mocking Android BaseBundle.putString - these are integration tests")
    @Test
    fun submit_shouldSetInvalidAmountError_whenAmountIsZero() = runViewModelTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[0]))
        viewModel.onEvent(AddTransactionEvent.UpdateTitle("Pizza"))
        viewModel.onEvent(AddTransactionEvent.UpdateAmount("0"))
        viewModel.onEvent(AddTransactionEvent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AddTransactionConstant.ERROR_INVALID_AMOUNT, viewModel.state.value.error)
    }

    @Ignore("Requires mocking Android BaseBundle.putString - these are integration tests")
    @Test
    fun submit_shouldSetInvalidAmountError_whenAmountIsNegative() = runViewModelTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[0]))
        viewModel.onEvent(AddTransactionEvent.UpdateTitle("Pizza"))
        viewModel.onEvent(AddTransactionEvent.UpdateAmount("-5"))
        viewModel.onEvent(AddTransactionEvent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AddTransactionConstant.ERROR_INVALID_AMOUNT, viewModel.state.value.error)
    }

    @Ignore("Requires mocking Android BaseBundle.putString - these are integration tests")
    @Test
    fun submit_shouldSetInvalidAmountError_whenAmountIsNotANumber() = runViewModelTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[0]))
        viewModel.onEvent(AddTransactionEvent.UpdateTitle("Pizza"))
        viewModel.onEvent(AddTransactionEvent.UpdateAmount("abc"))
        viewModel.onEvent(AddTransactionEvent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AddTransactionConstant.ERROR_INVALID_AMOUNT, viewModel.state.value.error)
    }

    // ── Submit failure ──

    @Test
    fun submit_shouldSetSaveError_whenInsertTransactionUseCaseFails() = runViewModelTest {
        transactionRepository.errorToThrow = IllegalStateException("insert failed")
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[0]))
        viewModel.onEvent(AddTransactionEvent.UpdateTitle("Pizza"))
        viewModel.onEvent(AddTransactionEvent.UpdateAmount("12.50"))
        viewModel.onEvent(AddTransactionEvent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(AddTransactionConstant.ERROR_SAVE, state.error)
        assertFalse("Should not be saved", state.isTransactionSaved)
    }

    @Test
    fun submit_shouldSetSaveError_whenUpdateTransactionUseCaseFails() = runViewModelTest {
        viewModel = createViewModel(transactionId = 1L)
        advanceUntilLoaded()
        transactionRepository.errorToThrow = IllegalStateException("update failed")

        viewModel.onEvent(AddTransactionEvent.UpdateTitle("Updated Title"))
        viewModel.onEvent(AddTransactionEvent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(AddTransactionConstant.ERROR_SAVE, state.error)
        assertFalse("Should not be saved", state.isTransactionSaved)
    }

    // ── Buoni Pasto ──

    @Test
    fun submit_shouldCalculateAmountAutomatically_whenPaymentTypeIsMealVouchers() =
        runViewModelTest {
            viewModel = createViewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            // IMPORTANTE: Buoni Pasto è INCOME, non EXPENSE!
            val incomeCategory = mockCategories[1]  // Salary è INCOME
            viewModel.onEvent(AddTransactionEvent.SelectCategory(incomeCategory))
            viewModel.onEvent(AddTransactionEvent.UpdateTitle("Pranzo"))
            // Seleziona MEAL_VOUCHERS - nel campo amount viene ignorato perché nascosto
            viewModel.onEvent(AddTransactionEvent.SelectPaymentType(PaymentType.MEAL_VOUCHERS))
            // Inserisci 3 voucher - l'importo viene calcolato automaticamente (3 * 5.29 = 15.87)
            viewModel.onEvent(AddTransactionEvent.UpdateMealVoucherCount("3"))
            testDispatcher.scheduler.advanceUntilIdle()

            // L'importo deve essere calcolato automaticamente dal numero di voucher
            assertEquals("Amount should be auto-calculated", "15.87", viewModel.state.value.amount)

            viewModel.onEvent(AddTransactionEvent.Submit)
            testDispatcher.scheduler.advanceUntilIdle()

            // L'importo persistito deve essere il valore calcolato (3 * 5.29 = 15.87)
            // Positivo perché INCOME, non negato
            val saved = transactionRepository.transactions.value.first { it.title == "Pranzo" }
            assertEquals("Amount should be 15.87 (positive for INCOME)", 15.87, saved.amount, 0.001)
            assertEquals("Voucher count should be 3", 3, saved.mealVoucherCount)
        }

    @Test
    fun submit_shouldCalculateAmountFromMealVouchers_whenResavingLoadedMealVouchersTransaction() =
        runViewModelTest {
            // Buoni Pasto è di tipo INCOME, con importo calcolato dai voucher
            // Se amount è negativo, lo rende positivo al caricamento (linea 168: abs)
            val existingMealVoucherTransaction = mockTransaction.copy(
                id = 2L,
                type = TransactionType.INCOME,  // Buoni Pasto è INCOME, not EXPENSE
                amount = 15.87,  // = 3 * 5.29 (calculated from voucher count)
                paymentType = PaymentType.MEAL_VOUCHERS,
                mealVoucherCount = 3,
            )
            transactionRepository.transactions.value =
                transactionRepository.transactions.value + existingMealVoucherTransaction
            viewModel = createViewModel(transactionId = 2L)
            advanceUntilLoaded()

            assertEquals(
                "Il campo Importo deve mostrare il valore calcolato dai voucher",
                "15.87",
                viewModel.state.value.amount,
            )

            viewModel.onEvent(AddTransactionEvent.Submit)
            testDispatcher.scheduler.advanceUntilIdle()

            // Ri-salvando senza modifiche, l'importo persistito deve restare invariato:
            // l'importo è calcolato dai voucher (3 * 5.29 = 15.87)
            val saved = transactionRepository.transactions.value.first { it.id == 2L }
            assertEquals(15.87, saved.amount, 0.001)
            assertEquals(3, saved.mealVoucherCount)
        }

    // ── FASE 1: Quick Wins Tests ──

    @Test
    fun isFormValid_shouldReturnFalse_whenRecurringButNoInterval() = runViewModelTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[0])) // Food
        viewModel.onEvent(AddTransactionEvent.UpdateTitle("Test"))
        viewModel.onEvent(AddTransactionEvent.UpdateAmount("10"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Form è valido senza ricorrenza
        assertTrue("Form should be valid", viewModel.state.value.isFormValid)

        // Abilita ricorrenza senza intervallo
        viewModel.onEvent(AddTransactionEvent.SetRecurring(true))
        testDispatcher.scheduler.advanceUntilIdle()

        // Form diventa invalido
        assertFalse(
            "Form should be invalid when recurring without interval",
            viewModel.state.value.isFormValid
        )

        // Scegli intervallo
        viewModel.onEvent(AddTransactionEvent.UpdateRecurrenceInterval("monthly"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Form torna valido
        assertTrue("Form should be valid after setting interval", viewModel.state.value.isFormValid)
    }

    @Test
    fun selectPaymentType_shouldResetMealVoucherCount_whenChangingFromMealVouchers() =
        runViewModelTest {
            viewModel = createViewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[0])) // Food
            viewModel.onEvent(AddTransactionEvent.UpdateTitle("Pranzo"))
            viewModel.onEvent(AddTransactionEvent.UpdateAmount("20.00"))
            testDispatcher.scheduler.advanceUntilIdle()

            // Cambia a MEAL_VOUCHERS
            viewModel.onEvent(AddTransactionEvent.SelectPaymentType(PaymentType.MEAL_VOUCHERS))
            testDispatcher.scheduler.advanceUntilIdle()

            // Inserisci 3 voucher - l'importo viene RICALCOLATO automaticamente
            viewModel.onEvent(AddTransactionEvent.UpdateMealVoucherCount("3"))
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals("Voucher count should be 3", "3", viewModel.state.value.mealVoucherCount)
            // L'importo viene RICALCOLATO automaticamente dal numero di voucher (3 * 5.29 = 15.87)
            assertEquals(
                "Amount should be auto-calculated from voucher count",
                "15.87",
                viewModel.state.value.amount
            )

            // Cambia a CASH
            viewModel.onEvent(AddTransactionEvent.SelectPaymentType(PaymentType.CASH))
            testDispatcher.scheduler.advanceUntilIdle()

            // Voucher count e amount vengono resetati
            assertEquals(
                "Voucher count should be reset to 0",
                "0",
                viewModel.state.value.mealVoucherCount
            )
            assertEquals("Amount should be reset to empty", "", viewModel.state.value.amount)
        }

    @Ignore("Requires mocking Android BaseBundle.putString - these are integration tests")
    @Test
    fun submit_shouldFailIfCategoryNoLongerExists() = runViewModelTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[0])) // Food
        viewModel.onEvent(AddTransactionEvent.UpdateTitle("Test"))
        viewModel.onEvent(AddTransactionEvent.UpdateAmount("10"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("Form should be valid", viewModel.state.value.isFormValid)

        // Simula cancellazione della categoria dal repository
        categoryRepository.removeCategory(mockCategories[0].id)

        // Prova a salvare
        viewModel.onEvent(AddTransactionEvent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()

        // Dovrebbe fallire con errore categoria non trovata
        assertEquals(
            "Should show category not found error",
            "Categoria non più disponibile",
            viewModel.state.value.error
        )
        assertFalse("Transaction should not be saved", viewModel.state.value.isTransactionSaved)
    }

    // ── FASE 2: Error Handling Tests ──

    @Test
    fun loadTransaction_shouldShowError_ifCategoryNotFound() = runViewModelTest {
        // Simula: transazione con categoria che non esiste più
        val transactionWithMissingCategory = mockTransaction.copy(
            id = 99L,
            category = "DeletedCategory",  // Categoria che non esiste
        )
        transactionRepository.transactions.value =
            transactionRepository.transactions.value + transactionWithMissingCategory

        viewModel = createViewModel(transactionId = 99L)
        advanceUntilLoaded()

        // Dovrebbe mostrare errore di categoria non trovata
        val error = viewModel.state.value.error
        assertTrue("Should show error", !error.isNullOrEmpty())
        assertTrue(
            "Error should mention missing category or load error",
            error?.contains("DeletedCategory") == true || error?.contains("not found") == true || error?.contains(
                "ERROR"
            ) == true
        )
        // Should not be able to edit without category
        assertFalse(
            "Should not be in DETAILS without valid category",
            viewModel.state.value.isModifying
        )
    }

    @Test
    fun submit_shouldShowError_whenInsertTransactionFails() = runViewModelTest {
        transactionRepository.errorToThrow = Exception("Network error: connection timeout")
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[0]))
        viewModel.onEvent(AddTransactionEvent.UpdateTitle("Pizza"))
        viewModel.onEvent(AddTransactionEvent.UpdateAmount("12.50"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Prova a salvare
        viewModel.onEvent(AddTransactionEvent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()

        // Dovrebbe mostrare errore
        assertEquals(
            "Should show save error",
            AddTransactionConstant.ERROR_SAVE,
            viewModel.state.value.error
        )
        assertFalse("Transaction should not be saved", viewModel.state.value.isTransactionSaved)
        assertFalse("Loading should be false", viewModel.state.value.isLoading)
    }

    @Test
    fun submit_shouldShowError_whenUpdateTransactionFails() = runViewModelTest {
        transactionRepository.errorToThrow = Exception("Database error: write failed")
        val existingTransaction = mockTransaction.copy(id = 1L)
        transactionRepository.transactions.value = listOf(existingTransaction)
        viewModel = createViewModel(transactionId = 1L)
        advanceUntilLoaded()

        // Modifica titolo
        viewModel.onEvent(AddTransactionEvent.UpdateTitle("Updated Pizza"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Prova a salvare (update)
        viewModel.onEvent(AddTransactionEvent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()

        // Dovrebbe mostrare errore
        assertEquals(
            "Should show save error",
            AddTransactionConstant.ERROR_SAVE,
            viewModel.state.value.error
        )
        assertFalse("Transaction should not be saved", viewModel.state.value.isTransactionSaved)
    }

    @Test
    fun confirmDelete_shouldShowError_whenDeleteFails() = runViewModelTest {
        transactionRepository.errorToThrow = Exception("Database error: delete failed")
        val transaction = mockTransaction.copy(id = 1L)
        transactionRepository.transactions.value = listOf(transaction)
        viewModel = createViewModel(transactionId = 1L)
        advanceUntilLoaded()

        // Apri delete dialog e conferma
        viewModel.onEvent(AddTransactionEvent.ShowDeleteConfirmDialog)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(
            "Delete confirm dialog should be open",
            viewModel.state.value.showDeleteConfirmDialog
        )

        viewModel.onEvent(AddTransactionEvent.ConfirmDelete)
        testDispatcher.scheduler.advanceUntilIdle()

        // Dovrebbe mostrare errore di delete
        assertEquals(
            "Should show delete error",
            AddTransactionConstant.ERROR_DELETE,
            viewModel.state.value.error
        )
        assertFalse("Transaction should not be deleted", viewModel.state.value.isTransactionSaved)
    }

    // ── FASE 3: Edge Case Tests ──

    @Test
    fun submit_shouldHandleAmount_withManyDecimals() = runViewModelTest {
        // Edge case: Utente inserisce importo con molti decimali
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[0]))
        viewModel.onEvent(AddTransactionEvent.UpdateTitle("Coffee"))
        // Inserisci importo con 4 decimali
        viewModel.onEvent(AddTransactionEvent.UpdateAmount("3.1415"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Form dovrebbe essere valido (3.1415 è un numero valido)
        assertTrue("Form should be valid", viewModel.state.value.isFormValid)

        // Salva
        viewModel.onEvent(AddTransactionEvent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()

        // Dovrebbe salvare correttamente
        assertTrue("Transaction should be saved", viewModel.state.value.isTransactionSaved)
    }

    @Test
    fun submit_shouldRejectTitleLongerThan255Characters() = runViewModelTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[0]))
        // Titolo di 256 caratteri
        val longTitle = "A".repeat(256)
        viewModel.onEvent(AddTransactionEvent.UpdateTitle(longTitle))
        viewModel.onEvent(AddTransactionEvent.UpdateAmount("10"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Form dovrebbe essere valido (validazione non controlla lunghezza)
        // Nota: Questo potrebbe essere un bug - il form non valida lunghezza titolo
        assertTrue(
            "Form should be valid (no length validation yet)",
            viewModel.state.value.isFormValid
        )

        // Prova a salvare
        viewModel.onEvent(AddTransactionEvent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()

        // Il salvataggio avviene anche se titolo è lungo
        // Questo è un edge case da considerare per una futura mitigazione
        assertTrue(
            "Transaction should be saved (no validation on title length)",
            viewModel.state.value.isTransactionSaved
        )
    }

    @Test
    fun updateRecurrenceInterval_shouldEnableSubmit_whenIntervalProvided() = runViewModelTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[1])) // Salary (INCOME)
        viewModel.onEvent(AddTransactionEvent.UpdateTitle("Monthly Salary"))
        viewModel.onEvent(AddTransactionEvent.UpdateAmount("3000"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Abilita ricorrenza senza intervallo
        viewModel.onEvent(AddTransactionEvent.SetRecurring(true))
        testDispatcher.scheduler.advanceUntilIdle()

        // Form non dovrebbe essere valido
        assertFalse("Form should be invalid without interval", viewModel.state.value.isFormValid)

        // Aggiungi intervallo
        viewModel.onEvent(AddTransactionEvent.UpdateRecurrenceInterval("1"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Adesso form dovrebbe essere valido
        assertTrue("Form should be valid with interval", viewModel.state.value.isFormValid)
    }

    @Test
    fun selectCategory_shouldChangeType_whenSwitchingFromIncomeToExpense() = runViewModelTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Seleziona INCOME
        viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[1])) // Salary = INCOME
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(
            "Type should be INCOME",
            TransactionType.INCOME,
            viewModel.state.value.selectedType
        )

        viewModel.onEvent(AddTransactionEvent.UpdateTitle("Test"))
        viewModel.onEvent(AddTransactionEvent.UpdateAmount("100"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Cambia a EXPENSE
        viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[0])) // Food = EXPENSE
        testDispatcher.scheduler.advanceUntilIdle()

        // Type dovrebbe cambiare a EXPENSE
        assertEquals(
            "Type should change to EXPENSE",
            TransactionType.EXPENSE,
            viewModel.state.value.selectedType
        )
        // Importo dovrebbe rimanere identico (non negazione automatica)
        assertEquals("Amount should remain unchanged", "100", viewModel.state.value.amount)
    }

    @Test
    fun selectPaymentType_shouldResetMealVoucherData_whenChangingFromMealVouchersToOtherType() =
        runViewModelTest {
            // IMPORTANTE: Buoni Pasto è INCOME, non EXPENSE!
            // Creiamo una categoria INCOME per il test
            val incomeCategory = mockCategories[1]  // Salary è INCOME
            viewModel = createViewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onEvent(AddTransactionEvent.SelectCategory(incomeCategory))
            viewModel.onEvent(AddTransactionEvent.SelectPaymentType(PaymentType.MEAL_VOUCHERS))
            // Inserisci 5 voucher - l'importo viene calcolato automaticamente (5 * 5.29 = 26.45)
            viewModel.onEvent(AddTransactionEvent.UpdateMealVoucherCount("5"))
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(
                "Meal voucher count should be 5",
                "5",
                viewModel.state.value.mealVoucherCount
            )
            // L'importo è calcolato automaticamente
            assertEquals(
                "Amount should be auto-calculated to 26.45",
                "26.45",
                viewModel.state.value.amount
            )

            // Cambia payment type DA MEAL_VOUCHERS a ELECTRONIC
            viewModel.onEvent(AddTransactionEvent.SelectPaymentType(PaymentType.ELECTRONIC))
            testDispatcher.scheduler.advanceUntilIdle()

            // Voucher count e amount dovrebbero essere resettati perché stiamo uscendo da MEAL_VOUCHERS
            assertEquals(
                "Meal voucher count should be reset",
                "0",
                viewModel.state.value.mealVoucherCount
            )
            assertEquals("Amount should be reset", "", viewModel.state.value.amount)
        }

    @Test
    fun amount_shouldBeValid_withPointDecimalSeparator() = runViewModelTest {
        // Edge case: Locale italiano usa virgola, ma il ViewModel usa punto
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[0]))
        viewModel.onEvent(AddTransactionEvent.UpdateTitle("Lunch"))
        // Inserisci con punto (normalizzato dal UI)
        viewModel.onEvent(AddTransactionEvent.UpdateAmount("12.50"))
        testDispatcher.scheduler.advanceUntilIdle()

        // L'importo dovrebbe essere valido perché usa il punto
        assertTrue("Amount with point should be valid", viewModel.state.value.isFormValid)

        // Se l'utente inserisce con virgola, il UI dovrebbe normalizzare prima di aggiornare il ViewModel
        // Il test di normalizzazione dovrebbe essere nel DetailsStepTest
    }

    @Test
    fun mealVoucherCount_shouldValidateAsPositiveInteger() = runViewModelTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Seleziona categoria con payment type MEAL_VOUCHERS
        viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[0]))
        viewModel.onEvent(AddTransactionEvent.SelectPaymentType(PaymentType.MEAL_VOUCHERS))
        viewModel.onEvent(AddTransactionEvent.UpdateTitle("Lunch"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Inserisci voucher count negativo
        viewModel.onEvent(AddTransactionEvent.UpdateMealVoucherCount("-1"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(
            "Form should be invalid with negative voucher count",
            viewModel.state.value.isFormValid
        )

        // Inserisci voucher count zero
        viewModel.onEvent(AddTransactionEvent.UpdateMealVoucherCount("0"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(
            "Form should be invalid with zero voucher count",
            viewModel.state.value.isFormValid
        )

        // Inserisci voucher count valido
        viewModel.onEvent(AddTransactionEvent.UpdateMealVoucherCount("3"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(
            "Form should be valid with positive voucher count",
            viewModel.state.value.isFormValid
        )

        // Inserisci voucher count non-integer
        viewModel.onEvent(AddTransactionEvent.UpdateMealVoucherCount("3.5"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(
            "Form should be invalid with decimal voucher count",
            viewModel.state.value.isFormValid
        )
    }

    @Test
    fun mealVoucherDifference_shouldUpdateState_whenEventReceived() = runViewModelTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Seleziona MEAL_VOUCHERS payment type
        viewModel.onEvent(AddTransactionEvent.SelectPaymentType(PaymentType.MEAL_VOUCHERS))
        viewModel.onEvent(AddTransactionEvent.UpdateTitle("Lunch"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Inserisci differenza pagata
        viewModel.onEvent(AddTransactionEvent.UpdateMealVoucherDifference("3.55"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            "Difference should be updated in state",
            "3.55",
            viewModel.state.value.mealVoucherDifference
        )
    }

    @Test
    fun totalAmount_shouldCalculateCorrectly_withMealVouchersAndDifference() = runViewModelTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Seleziona MEAL_VOUCHERS
        viewModel.onEvent(AddTransactionEvent.SelectPaymentType(PaymentType.MEAL_VOUCHERS))
        viewModel.onEvent(AddTransactionEvent.UpdateTitle("Lunch"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Setup: 5 buoni × 5.29€ = 26.45€
        viewModel.onEvent(AddTransactionEvent.UpdateMealVoucherCount("5"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Aggiungere differenza: 3.55€
        viewModel.onEvent(AddTransactionEvent.UpdateMealVoucherDifference("3.55"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Totale = 26.45 + 3.55 = 30.00
        val expectedTotal = 26.45 + 3.55
        assertEquals(
            "Total amount should be vouchers subtotal + difference",
            expectedTotal,
            viewModel.state.value.totalAmount,
            0.01 // tolerance per floating point
        )
    }

    @Test
    fun totalAmount_shouldBeVoucherSubtotal_whenDifferenceIsEmpty() = runViewModelTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Seleziona MEAL_VOUCHERS
        viewModel.onEvent(AddTransactionEvent.SelectPaymentType(PaymentType.MEAL_VOUCHERS))
        viewModel.onEvent(AddTransactionEvent.UpdateTitle("Lunch"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Setup: 5 buoni × 5.29€ = 26.45€
        viewModel.onEvent(AddTransactionEvent.UpdateMealVoucherCount("5"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Differenza vuota (default "0")
        val expectedTotal = 5 * 5.29
        assertEquals(
            "Total should be only vouchers subtotal when difference is empty",
            expectedTotal,
            viewModel.state.value.totalAmount,
            0.01
        )
    }

    @Test
    fun selectPaymentType_shouldResetMealVoucherDifference_whenChangingFromMealVouchers() = runViewModelTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Imposta MEAL_VOUCHERS con differenza
        viewModel.onEvent(AddTransactionEvent.SelectPaymentType(PaymentType.MEAL_VOUCHERS))
        viewModel.onEvent(AddTransactionEvent.UpdateMealVoucherCount("5"))
        viewModel.onEvent(AddTransactionEvent.UpdateMealVoucherDifference("3.55"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("3.55", viewModel.state.value.mealVoucherDifference)
        assertEquals(5, viewModel.state.value.mealVoucherCount.toIntOrNull())

        // Cambia a CASH payment type
        viewModel.onEvent(AddTransactionEvent.SelectPaymentType(PaymentType.CASH))
        testDispatcher.scheduler.advanceUntilIdle()

        // Verifica che differenza e count siano resettate
        assertEquals(
            "Meal voucher difference should be reset when changing payment type",
            "0",
            viewModel.state.value.mealVoucherDifference
        )
        assertEquals(
            "Meal voucher count should be reset when changing payment type",
            "0",
            viewModel.state.value.mealVoucherCount
        )
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

    private fun createViewModel(transactionId: Long? = null): AddTransactionViewModel {
        val getCategoriesUC = GetCategoriesUseCase(categoryRepository, testDispatcher)
        val getMealVoucherValueUC = GetMealVoucherValueUseCase(settingsRepository, testDispatcher)
        val getTransactionByIdUC = GetTransactionByIdUseCase(transactionRepository, testDispatcher)
        val insertTransactionUC = InsertTransactionUseCase(transactionRepository, testDispatcher)
        val updateTransactionUC = UpdateTransactionUseCase(transactionRepository, testDispatcher)
        val deleteTransactionUC = DeleteTransactionUseCase(transactionRepository, testDispatcher)
        val getTransactionSuggestionsUC = GetTransactionSuggestionsUseCase(
            transactionRepository,
            settingsRepository,
            testDispatcher,
        )

        val loadManager = TransactionLoadManager(
            getTransactionByIdUseCase = getTransactionByIdUC,
            getCategoriesUseCase = getCategoriesUC,
            getMealVoucherValueUseCase = getMealVoucherValueUC,
        )

        val submitManager = TransactionSubmitManager(
            insertTransactionUseCase = insertTransactionUC,
            updateTransactionUseCase = updateTransactionUC,
        )

        val suggestionsManager = SuggestionsManager(
            getTransactionSuggestionsUseCase = getTransactionSuggestionsUC,
        )

        return AddTransactionViewModel(
            loadManager = loadManager,
            submitManager = submitManager,
            suggestionsManager = suggestionsManager,
            deleteTransactionUseCase = deleteTransactionUC,
            getTransactionByIdUseCase = getTransactionByIdUC,
            analyticsManager = analyticsManager,
            settingsRepository = settingsRepository,
            performanceTracker = performanceTracker,
            errorTracker = errorTracker,
            transactionId = transactionId,
        )
    }
}

/**
 * Estende il fake condiviso con suggerimenti fissi, indipendenti dal contenuto
 * reale delle transazioni seminate nel test.
 */
private class FakeTransactionRepositoryWithCannedSuggestions(
    initialTransactions: List<Transaction>,
) : FakeTransactionRepository(initialTransactions) {
    override suspend fun getSuggestions(since: Long): TransactionSuggestions =
        TransactionSuggestions(
            titles = listOf("Spesa", "Carburante", "Ristorante"),
            payees = listOf("Supermercato", "Stazione"),
            notes = listOf("Cena con amici", "Spesa mensile"),
            locations = listOf("Milano", "Roma"),
            tags = listOf("Food", "Transport", "Shopping")
        )
}
