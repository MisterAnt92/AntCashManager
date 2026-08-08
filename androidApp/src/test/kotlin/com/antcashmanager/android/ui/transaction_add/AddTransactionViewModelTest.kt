package com.antcashmanager.android.ui.transaction_add

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.testutil.FakeCategoryRepository
import com.antcashmanager.android.testutil.FakeSettingsRepository
import com.antcashmanager.android.testutil.FakeTransactionRepository
import com.antcashmanager.android.ui.screen.transactions.addImport.AddTransactionConstant
import com.antcashmanager.android.ui.screen.transactions.addImport.AddTransactionStep
import com.antcashmanager.android.ui.screen.transactions.addImport.AddTransactionViewModel
import com.antcashmanager.android.ui.screen.transactions.addImport.event.AddTransactionEvent
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
    fun submit_shouldPersistEnteredAmountDirectly_whenPaymentTypeIsMealVouchers() =
        runViewModelTest {
            viewModel = createViewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onEvent(AddTransactionEvent.SelectCategory(mockCategories[0]))
            viewModel.onEvent(AddTransactionEvent.UpdateTitle("Pranzo"))
            viewModel.onEvent(AddTransactionEvent.UpdateAmount("20.00"))
            viewModel.onEvent(AddTransactionEvent.SelectPaymentType(PaymentType.MEAL_VOUCHERS))
            viewModel.onEvent(AddTransactionEvent.UpdateMealVoucherCount("3"))
            viewModel.onEvent(AddTransactionEvent.Submit)
            testDispatcher.scheduler.advanceUntilIdle()

            // L'importo persistito deve essere esattamente quello inserito (gia' il totale),
            // senza sommare il subtotale dei buoni (3 * 5.29) sopra.
            val saved = transactionRepository.transactions.value.first { it.title == "Pranzo" }
            assertEquals(20.00, saved.amount, 0.001)
            assertEquals(3, saved.mealVoucherCount)
        }

    @Test
    fun submit_shouldNotInflateAmount_whenResavingLoadedMealVouchersTransaction() =
        runViewModelTest {
            val existingMealVoucherTransaction = mockTransaction.copy(
                id = 2L,
                amount = 20.0,
                paymentType = PaymentType.MEAL_VOUCHERS,
                mealVoucherCount = 3,
            )
            transactionRepository.transactions.value =
                transactionRepository.transactions.value + existingMealVoucherTransaction
            viewModel = createViewModel(transactionId = 2L)
            advanceUntilLoaded()

            assertEquals(
                "Il campo Importo deve mostrare il totale gia' salvato, non una sua parte",
                "20.0",
                viewModel.state.value.amount,
            )

            viewModel.onEvent(AddTransactionEvent.Submit)
            testDispatcher.scheduler.advanceUntilIdle()

            // Ri-salvando senza modifiche, l'importo persistito deve restare invariato:
            // non deve essere sommato di nuovo il subtotale dei buoni (3 * 5.29).
            val saved = transactionRepository.transactions.value.first { it.id == 2L }
            assertEquals(20.0, saved.amount, 0.001)
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
            getCategoriesUseCase = GetCategoriesUseCase(categoryRepository, testDispatcher),
            getMealVoucherValueUseCase = GetMealVoucherValueUseCase(
                settingsRepository,
                testDispatcher
            ),
            getTransactionByIdUseCase = GetTransactionByIdUseCase(
                transactionRepository,
                testDispatcher
            ),
            insertTransactionUseCase = InsertTransactionUseCase(
                transactionRepository,
                testDispatcher
            ),
            updateTransactionUseCase = UpdateTransactionUseCase(
                transactionRepository,
                testDispatcher
            ),
            deleteTransactionUseCase = DeleteTransactionUseCase(
                transactionRepository,
                testDispatcher
            ),
            getTransactionSuggestionsUseCase = GetTransactionSuggestionsUseCase(
                transactionRepository,
                settingsRepository,
                testDispatcher,
            ),
            analyticsManager = analyticsManager,
            transactionId = transactionId,
        )
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
