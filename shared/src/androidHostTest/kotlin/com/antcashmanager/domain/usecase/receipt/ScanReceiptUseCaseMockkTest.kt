package com.antcashmanager.domain.usecase.receipt

import com.antcashmanager.domain.exception.ReceiptScanException
import com.antcashmanager.domain.service.ReceiptOcrService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class ScanReceiptUseCaseMockkTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var ocrService: ReceiptOcrService
    private lateinit var useCase: ScanReceiptUseCase

    private val sampleImageBytes = ByteArray(64) { it.toByte() }

    @Before
    fun setup() {
        ocrService = mockk()
        useCase = ScanReceiptUseCase(ocrService = ocrService, dispatcher = testDispatcher)
    }

    @Test
    fun invoke_shouldReturnParsedReceiptData_whenOcrReturnsValidText() = runTest(testDispatcher) {
        coEvery { ocrService.extractText(sampleImageBytes) } returns Result.success(
            "SUPERMARKET\nTOTAL EUR 42,50\nVAT 22% 7,66",
        )

        val result = useCase(sampleImageBytes)

        assertTrue(result.isSuccess)
        assertEquals(42.50, result.getOrThrow().totalAmount, 0.01)
        coVerify(exactly = 1) { ocrService.extractText(sampleImageBytes) }
    }

    @Test
    fun invoke_shouldReturnOcrFailed_whenOcrReturnsFailure() = runTest(testDispatcher) {
        coEvery { ocrService.extractText(sampleImageBytes) } returns Result.failure(
            IllegalStateException("ocr-down"),
        )

        val result = useCase(sampleImageBytes)

        assertTrue(result.isFailure)
        assertIs<ReceiptScanException.OcrFailed>(result.exceptionOrNull())
        coVerify(exactly = 1) { ocrService.extractText(sampleImageBytes) }
    }

    @Test
    fun invoke_shouldReturnInvalidImage_whenImageBytesAreEmpty() = runTest(testDispatcher) {
        val result = useCase(ByteArray(0))

        assertTrue(result.isFailure)
        assertIs<ReceiptScanException.InvalidImage>(result.exceptionOrNull())
        coVerify(exactly = 0) { ocrService.extractText(any()) }
    }

    @Test
    fun invoke_shouldReturnNoTextExtracted_whenOcrReturnsBlankText() = runTest(testDispatcher) {
        coEvery { ocrService.extractText(sampleImageBytes) } returns Result.success("   ")

        val result = useCase(sampleImageBytes)

        assertTrue(result.isFailure)
        assertIs<ReceiptScanException.NoTextExtracted>(result.exceptionOrNull())
        coVerify(exactly = 1) { ocrService.extractText(sampleImageBytes) }
    }

    @Test
    fun invoke_shouldPreserveRawText_whenOcrSucceeds() = runTest(testDispatcher) {
        val rawText = "SUPERMARKET\nTOTAL EUR 42,50\nVAT 22% 7,66"
        coEvery { ocrService.extractText(sampleImageBytes) } returns Result.success(rawText)

        val result = useCase(sampleImageBytes)

        assertTrue(result.isSuccess)
        assertEquals(rawText, result.getOrThrow().rawText)
    }

    @Test
    fun invoke_shouldReturnAmountNotFound_whenNoNumericValueInText() = runTest(testDispatcher) {
        coEvery { ocrService.extractText(sampleImageBytes) } returns Result.success("SOLO TESTO SENZA NUMERI")

        val result = useCase(sampleImageBytes)

        assertTrue(result.isFailure)
        assertIs<ReceiptScanException.AmountNotFound>(result.exceptionOrNull())
        coVerify(exactly = 1) { ocrService.extractText(sampleImageBytes) }
    }

}
