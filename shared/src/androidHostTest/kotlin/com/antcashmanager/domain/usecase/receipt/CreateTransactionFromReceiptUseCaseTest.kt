package com.antcashmanager.domain.usecase.receipt

import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.ReceiptData
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.testutil.FakeTransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateTransactionFromReceiptUseCaseTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeTransactionRepository
    private lateinit var useCase: CreateTransactionFromReceiptUseCase

    private val sampleReceipt =
        ReceiptData(
            totalAmount = 68.90,
            vatRate = 22.0,
            vatAmount = 12.40,
            payee = "Esselunga S.p.A.",
            location = "Via Roma 1, Milano",
            paymentType = PaymentType.ELECTRONIC,
            rawText = "...",
        )

    private val sampleParams =
        CreateTransactionFromReceiptParams(
            receiptData = sampleReceipt,
            title = "Esselunga",
            categoryName = "Alimentari",
            categoryIcon = "🛒",
            categoryColor = 0xFF4CAF50,
            timestamp = 1000L,
        )

    @Before
    fun setup() {
        fakeRepo = FakeTransactionRepository()
        useCase =
            CreateTransactionFromReceiptUseCase(
                fakeRepo,
                UnconfinedTestDispatcher(testDispatcher.scheduler),
            )
    }

    // ── Happy Path ───────────────────────────────────────────────────────────

    @Test
    fun invoke_shouldInsertTransactionAndReturnId_whenValidParams() =
        runTest(testDispatcher) {
            val result = useCase(sampleParams)

            assertTrue(result.isSuccess)
            assertEquals(1L, result.getOrThrow())
        }

    @Test
    fun invoke_shouldCreateExpenseTransaction_always() =
        runTest(testDispatcher) {
            useCase(sampleParams)

            val saved = fakeRepo.transactions.value.first()
            assertEquals(TransactionType.EXPENSE, saved.type)
        }

    @Test
    fun invoke_shouldSetAmountFromReceipt() =
        runTest(testDispatcher) {
            useCase(sampleParams)

            val saved = fakeRepo.transactions.value.first()
            assertEquals(68.90, saved.amount, 0.001)
        }

    @Test
    fun invoke_shouldSetCategoryName() =
        runTest(testDispatcher) {
            useCase(sampleParams)

            val saved = fakeRepo.transactions.value.first()
            assertEquals("Alimentari", saved.category)
        }

    // ── Tipo pagamento ───────────────────────────────────────────────────────

    @Test
    fun invoke_shouldUseReceiptPaymentType_whenNoOverride() =
        runTest(testDispatcher) {
            val receiptWithCash = sampleReceipt.copy(paymentType = PaymentType.CASH)
            val params = sampleParams.copy(receiptData = receiptWithCash, paymentType = null)

            useCase(params)

            val saved = fakeRepo.transactions.value.first()
            assertEquals(PaymentType.CASH, saved.paymentType)
        }

    @Test
    fun invoke_shouldUseUserOverride_whenPaymentTypeExplicitlySet() =
        runTest(testDispatcher) {
            // L'OCR ha rilevato ELECTRONIC, ma l'utente ha scelto MEAL_VOUCHERS
            val params = sampleParams.copy(paymentType = PaymentType.MEAL_VOUCHERS)

            useCase(params)

            val saved = fakeRepo.transactions.value.first()
            assertEquals(PaymentType.MEAL_VOUCHERS, saved.paymentType)
        }

    @Test
    fun invoke_shouldAlwaysBeExpense_regardlessOfPaymentType() =
        runTest(testDispatcher) {
            for (pt in PaymentType.entries) {
                fakeRepo.transactions.value = emptyList()
                useCase(sampleParams.copy(paymentType = pt))
                assertEquals(
                    TransactionType.EXPENSE,
                    fakeRepo.transactions.value
                        .first()
                        .type,
                )
            }
        }

    // ── IVA nel campo notes ──────────────────────────────────────────────────

    @Test
    fun invoke_shouldEncodeVatInNotes_whenVatPresent() =
        runTest(testDispatcher) {
            useCase(sampleParams)

            val saved = fakeRepo.transactions.value.first()
            assertTrue(saved.notes.contains("IVA"))
            assertTrue(saved.notes.contains("22%"))
            assertTrue(saved.notes.contains("12"))
        }

    @Test
    fun invoke_shouldLeaveNotesEmpty_whenNoVat() =
        runTest(testDispatcher) {
            val noVatReceipt = sampleReceipt.copy(vatRate = 0.0, vatAmount = 0.0)
            val params = sampleParams.copy(receiptData = noVatReceipt)

            useCase(params)

            val saved = fakeRepo.transactions.value.first()
            assertTrue(saved.notes.isBlank())
        }

    // ── Titolo fallback ──────────────────────────────────────────────────────

    @Test
    fun invoke_shouldUseReceiptPayeeAsTitle_whenTitleIsBlank() =
        runTest(testDispatcher) {
            val params = sampleParams.copy(title = "")

            useCase(params)

            val saved = fakeRepo.transactions.value.first()
            assertEquals("Esselunga S.p.A.", saved.title)
        }

    @Test
    fun invoke_shouldUseDefaultTitle_whenBothTitleAndPayeeAreBlank() =
        runTest(testDispatcher) {
            val receipt = sampleReceipt.copy(payee = "")
            val params = sampleParams.copy(title = "", receiptData = receipt)

            useCase(params)

            val saved = fakeRepo.transactions.value.first()
            assertEquals("Scontrino", saved.title)
        }

    // ── Error Handling ───────────────────────────────────────────────────────

    @Test
    fun invoke_shouldReturnFailure_whenRepositoryThrows() =
        runTest(testDispatcher) {
            fakeRepo.errorToThrow = RuntimeException("DB error")

            val result = useCase(sampleParams)

            assertTrue(result.isFailure)
        }

    // ── Cancellation ─────────────────────────────────────────────────────────

    @Test
    fun invoke_shouldBeCancellable_beforeCompletion() =
        runTest(testDispatcher) {
            fakeRepo.operationDelayMs = 10_000L
            val cancellableUseCase = CreateTransactionFromReceiptUseCase(fakeRepo, testDispatcher)

            val job: Job = launch { cancellableUseCase(sampleParams) }
            job.cancel()
            advanceUntilIdle()

            assertTrue(job.isCancelled)
            assertTrue(fakeRepo.transactions.value.isEmpty())
        }
}
