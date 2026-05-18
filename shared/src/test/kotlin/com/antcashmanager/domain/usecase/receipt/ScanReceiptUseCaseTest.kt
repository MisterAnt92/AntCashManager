package com.antcashmanager.domain.usecase.receipt

import com.antcashmanager.domain.exception.ReceiptScanException
import com.antcashmanager.domain.service.ReceiptOcrService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class ScanReceiptUseCaseTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeOcr: FakeReceiptOcrService
    private lateinit var useCase: ScanReceiptUseCase

    private val sampleImageBytes = ByteArray(100) { it.toByte() }
    private val receiptText = "ESSELUNGA\nVIA ROMA 1\nTOTALE EUR 68,90\nIVA 22% 12,40"

    @Before
    fun setup() {
        fakeOcr = FakeReceiptOcrService(textToReturn = receiptText)
        useCase = ScanReceiptUseCase(fakeOcr, UnconfinedTestDispatcher(testDispatcher.scheduler))
    }

    // ── Happy Path ───────────────────────────────────────────────────────────

    @Test
    fun invoke_shouldReturnReceiptData_whenOcrSucceeds() = runTest(testDispatcher) {
        val result = useCase(sampleImageBytes)

        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals(68.90, data.totalAmount, 0.01)
        assertEquals(22.0, data.vatRate, 0.001)
        assertFalse(data.payee.isBlank())
    }

    @Test
    fun invoke_shouldPreserveRawText_whenOcrSucceeds() = runTest(testDispatcher) {
        val data = useCase(sampleImageBytes).getOrThrow()
        assertEquals(receiptText, data.rawText)
    }

    // ── Error: empty image ────────────────────────────────────────────────────

    @Test
    fun invoke_shouldReturnInvalidImage_whenImageBytesEmpty() = runTest(testDispatcher) {
        val result = useCase(ByteArray(0))

        assertTrue(result.isFailure)
        assertIs<ReceiptScanException.InvalidImage>(result.exceptionOrNull())
    }

    // ── Error: no text extracted ──────────────────────────────────────────────

    @Test
    fun invoke_shouldReturnNoTextExtracted_whenOcrReturnsBlank() = runTest(testDispatcher) {
        fakeOcr = FakeReceiptOcrService(textToReturn = "   ")
        useCase = ScanReceiptUseCase(fakeOcr, UnconfinedTestDispatcher(testDispatcher.scheduler))

        val result = useCase(sampleImageBytes)

        assertTrue(result.isFailure)
        assertIs<ReceiptScanException.NoTextExtracted>(result.exceptionOrNull())
    }

    // ── Error: amount not found ───────────────────────────────────────────────

    @Test
    fun invoke_shouldReturnAmountNotFound_whenNoNumericValueInText() = runTest(testDispatcher) {
        fakeOcr = FakeReceiptOcrService(textToReturn = "SOLO TESTO SENZA NUMERI")
        useCase = ScanReceiptUseCase(fakeOcr, UnconfinedTestDispatcher(testDispatcher.scheduler))

        val result = useCase(sampleImageBytes)

        assertTrue(result.isFailure)
        assertIs<ReceiptScanException.AmountNotFound>(result.exceptionOrNull())
    }

    // ── Error: OCR fails ─────────────────────────────────────────────────────

    @Test
    fun invoke_shouldReturnOcrFailed_whenOcrThrows() = runTest(testDispatcher) {
        fakeOcr = FakeReceiptOcrService(shouldThrow = true)
        useCase = ScanReceiptUseCase(fakeOcr, UnconfinedTestDispatcher(testDispatcher.scheduler))

        val result = useCase(sampleImageBytes)

        assertTrue(result.isFailure)
        assertIs<ReceiptScanException.OcrFailed>(result.exceptionOrNull())
    }

    // ── Cancellation ─────────────────────────────────────────────────────────

    @Test
    fun invoke_shouldBeCancellable_beforeCompletion() = runTest(testDispatcher) {
        val slowOcr = SlowFakeReceiptOcrService(delayMs = 10_000L)
        val cancellableUseCase = ScanReceiptUseCase(slowOcr, testDispatcher)

        val job: Job = launch { cancellableUseCase(sampleImageBytes) }
        job.cancel()
        advanceUntilIdle()

        assertTrue(job.isCancelled)
        assertFalse(slowOcr.wasCalled)
    }
}

// ── Fake OCR Service ──────────────────────────────────────────────────────────

private class FakeReceiptOcrService(
    private val textToReturn: String = "",
    private val shouldThrow: Boolean = false,
) : ReceiptOcrService {
    override suspend fun extractText(imageBytes: ByteArray): Result<String> {
        if (shouldThrow) return Result.failure(RuntimeException("OCR engine error"))
        return Result.success(textToReturn)
    }
}

private class SlowFakeReceiptOcrService(private val delayMs: Long) : ReceiptOcrService {
    var wasCalled = false

    override suspend fun extractText(imageBytes: ByteArray): Result<String> {
        delay(delayMs)
        wasCalled = true
        return Result.success("TOTALE 10,00")
    }
}

