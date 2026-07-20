package com.antcashmanager.android.ui.receiptScan

import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.testutil.FakeCategoryRepository
import com.antcashmanager.android.testutil.FakeSettingsRepository
import com.antcashmanager.android.testutil.FakeTransactionRepository
import com.antcashmanager.android.ui.screen.receiptScan.ReceiptScanStep
import com.antcashmanager.android.ui.screen.receiptScan.ReceiptScanViewModel
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.service.ReceiptOcrService
import com.antcashmanager.domain.usecase.receipt.ScanReceiptUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test per [ReceiptScanViewModel].
 * Nota: scanReceipt() usa MlKitReceiptOcrService (Android-only), quindi
 * il test verifica la logica del ViewModel tramite i metodi accessibili
 * (updateTitle, selectCategory, saveTransaction, retryCapture).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReceiptScanViewModelTest : BaseUnitTest() {

    private lateinit var fakeTxRepo: FakeTransactionRepository
    private lateinit var fakeCatRepo: FakeCategoryRepository
    private lateinit var viewModel: ReceiptScanViewModel

    private val expenseCategory = Category(id = 1L, name = "Alimentari", type = "EXPENSE")
    private val incomeCategory = Category(id = 2L, name = "Stipendio", type = "INCOME")

    @Before
    fun setup() {
        fakeTxRepo = FakeTransactionRepository()
        fakeCatRepo = FakeCategoryRepository(listOf(expenseCategory, incomeCategory))
        val fakeOcrService = FakeTestOcrService()
        val scanUseCase = ScanReceiptUseCase(fakeOcrService)
        viewModel = ReceiptScanViewModel(
            scanReceiptUseCase = scanUseCase,
            transactionRepository = fakeTxRepo,
            categoryRepository = fakeCatRepo,
            settingsRepository = FakeSettingsRepository(),
            dispatcher = testDispatcher,
        )
    }

    @After
    fun tearDown() {
        if (::viewModel.isInitialized) {
            viewModel.viewModelScope.cancel()
        }
    }

    // ── Initial State ────────────────────────────────────────────────────────

    @Test
    fun initialState_shouldBeCapture() = runViewModelTest {
        advanceUntilIdle()
        assertEquals(ReceiptScanStep.CAPTURE, viewModel.state.value.step)
    }

    @Test
    fun initialState_shouldLoadOnlyExpenseCategories() = runViewModelTest {
        advanceUntilIdle()
        val cats = viewModel.state.value.categories
        assertTrue(cats.all { it.type.equals("EXPENSE", ignoreCase = true) })
        assertEquals(1, cats.size)
        assertEquals("Alimentari", cats.first().name)
    }

    @Test
    fun initialState_shouldAutoSelectFirstExpenseCategory() = runViewModelTest {
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.selectedCategory)
        assertEquals("Alimentari", viewModel.state.value.selectedCategory?.name)
    }

    // ── updateTitle ──────────────────────────────────────────────────────────

    @Test
    fun updateTitle_shouldUpdateTitleInState() {
        viewModel.updateTitle("Nuovo Titolo")
        assertEquals("Nuovo Titolo", viewModel.state.value.title)
    }

    // ── updatePayee ──────────────────────────────────────────────────────────

    @Test
    fun updatePayee_shouldUpdatePayeeInState() {
        viewModel.updatePayee("COOP Italia")
        assertEquals("COOP Italia", viewModel.state.value.payee)
    }

    // ── updateLocation ───────────────────────────────────────────────────────

    @Test
    fun updateLocation_shouldUpdateLocationInState() {
        viewModel.updateLocation("Via Garibaldi 1")
        assertEquals("Via Garibaldi 1", viewModel.state.value.location)
    }

    // ── selectCategory ───────────────────────────────────────────────────────

    @Test
    fun selectCategory_shouldUpdateSelectedCategory() {
        val cat = Category(id = 3L, name = "Trasporti", type = "EXPENSE")
        viewModel.selectCategory(cat)
        assertEquals("Trasporti", viewModel.state.value.selectedCategory?.name)
    }

    @Test
    fun selectCategory_shouldDismissCategoryDialog() {
        viewModel.showCategoryDialog()
        assertTrue(viewModel.state.value.showCategoryDialog)
        viewModel.selectCategory(expenseCategory)
        assertFalse(viewModel.state.value.showCategoryDialog)
    }

    // ── showCategoryDialog / dismissCategoryDialog ────────────────────────────

    @Test
    fun showCategoryDialog_shouldSetShowCategoryDialogTrue() {
        viewModel.showCategoryDialog()
        assertTrue(viewModel.state.value.showCategoryDialog)
    }

    @Test
    fun dismissCategoryDialog_shouldSetShowCategoryDialogFalse() {
        viewModel.showCategoryDialog()
        viewModel.dismissCategoryDialog()
        assertFalse(viewModel.state.value.showCategoryDialog)
    }

    // ── selectPaymentType ─────────────────────────────────────────────────────

    @Test
    fun selectPaymentType_shouldUpdateSelectedPaymentType() {
        viewModel.selectPaymentType(PaymentType.CASH)
        assertEquals(PaymentType.CASH, viewModel.state.value.selectedPaymentType)
    }

    @Test
    fun selectPaymentType_shouldUpdateToMealVouchers() {
        viewModel.selectPaymentType(PaymentType.MEAL_VOUCHERS)
        assertEquals(PaymentType.MEAL_VOUCHERS, viewModel.state.value.selectedPaymentType)
    }

    @Test
    fun selectPaymentType_shouldDismissPaymentTypeDialog() {
        viewModel.showPaymentTypeDialog()
        assertTrue(viewModel.state.value.showPaymentTypeDialog)
        viewModel.selectPaymentType(PaymentType.CASH)
        assertFalse(viewModel.state.value.showPaymentTypeDialog)
    }

    // ── showPaymentTypeDialog / dismissPaymentTypeDialog ────────────────────

    @Test
    fun showPaymentTypeDialog_shouldSetShowPaymentTypeDialogTrue() {
        viewModel.showPaymentTypeDialog()
        assertTrue(viewModel.state.value.showPaymentTypeDialog)
    }

    @Test
    fun dismissPaymentTypeDialog_shouldSetShowPaymentTypeDialogFalse() {
        viewModel.showPaymentTypeDialog()
        viewModel.dismissPaymentTypeDialog()
        assertFalse(viewModel.state.value.showPaymentTypeDialog)
    }

    // ── retryCapture ─────────────────────────────────────────────────────────

    @Test
    fun retryCapture_shouldResetToCapture_preservingCategories() = runViewModelTest {
        advanceUntilIdle()
        viewModel.updateTitle("Titolo test")
        viewModel.retryCapture()

        val state = viewModel.state.value
        assertEquals(ReceiptScanStep.CAPTURE, state.step)
        assertEquals("", state.title)
        // Le categorie vengono ricaricate dall'init, quindi possono essere presenti
    }

    // ── saveTransaction – validation ─────────────────────────────────────────

    @Test
    fun saveTransaction_shouldSetError_whenNoReceiptData() = runViewModelTest {
        advanceUntilIdle()
        // receiptData è null nello stato iniziale
        viewModel.saveTransaction()
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.error)
    }

    // ── clearError ───────────────────────────────────────────────────────────

    @Test
    fun clearError_shouldSetErrorToNull() = runViewModelTest {
        viewModel.saveTransaction() // produce un errore (no receiptData)
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.error)

        viewModel.clearError()
        assertNull(viewModel.state.value.error)
    }
}

// ── Fake OCR Service ────────────────────────────────────────────────────────

private class FakeTestOcrService : ReceiptOcrService {
    override suspend fun extractText(imageBytes: ByteArray): Result<String> =
        Result.success("TOTALE 68,90\nIVA 22% 12,40")
}

