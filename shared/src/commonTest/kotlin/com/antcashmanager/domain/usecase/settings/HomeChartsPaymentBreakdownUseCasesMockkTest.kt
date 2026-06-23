package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeChartsPaymentBreakdownUseCasesMockkTest {

    private lateinit var settingsRepository: SettingsRepository
    private val dispatcher = StandardTestDispatcher()

    private val homeFilter = SavedDateFilter(
        presetIndex = 1,
        from = 1_715_000_000_000L,
        to = 1_715_100_000_000L,
    )

    private val chartsFilter = SavedDateFilter(
        presetIndex = 2,
        from = 1_714_000_000_000L,
        to = 1_715_500_000_000L,
    )

    @BeforeTest
    fun setup() {
        settingsRepository = mockk()
    }

    @Test
    fun invoke_shouldReturnSuccessFilter_whenHomeDateFilterStateIsEmitted() = runTest(dispatcher) {
        every { settingsRepository.getHomeDateFilterState() } returns flowOf(homeFilter)
        val useCase = GetHomeDateFilterStateUseCase(
            settingsRepository = settingsRepository,
            dispatcher = dispatcher,
        )

        val result = useCase().first()

        assertTrue(result.isSuccess)
        assertEquals(homeFilter, result.getOrThrow())
    }

    @Test
    fun invoke_shouldReturnFailure_whenHomeDateFilterStateFlowThrows() = runTest(dispatcher) {
        val expectedError = IllegalStateException("home-filter-read-failed")
        every { settingsRepository.getHomeDateFilterState() } returns flow {
            throw expectedError
        }
        val useCase = GetHomeDateFilterStateUseCase(
            settingsRepository = settingsRepository,
            dispatcher = dispatcher,
        )

        val result = useCase().first()

        assertTrue(result.isFailure)
        assertSame(expectedError, result.exceptionOrNull())
    }

    @Test
    fun invoke_shouldPersistFilter_whenSetHomeDateFilterStateSucceeds() = runTest(dispatcher) {
        coEvery { settingsRepository.setHomeDateFilterState(homeFilter) } returns Unit
        val useCase = SetHomeDateFilterStateUseCase(
            settingsRepository = settingsRepository,
            dispatcher = dispatcher,
        )

        val result = useCase(homeFilter)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { settingsRepository.setHomeDateFilterState(homeFilter) }
    }

    @Test
    fun invoke_shouldReturnFailure_whenSetHomeDateFilterStateThrows() = runTest(dispatcher) {
        val expectedError = IllegalArgumentException("home-filter-write-failed")
        coEvery { settingsRepository.setHomeDateFilterState(homeFilter) } throws expectedError
        val useCase = SetHomeDateFilterStateUseCase(
            settingsRepository = settingsRepository,
            dispatcher = dispatcher,
        )

        val result = useCase(homeFilter)

        assertTrue(result.isFailure)
        assertSame(expectedError, result.exceptionOrNull())
        coVerify(exactly = 1) { settingsRepository.setHomeDateFilterState(homeFilter) }
    }

    @Test
    fun invoke_shouldReturnSuccessFilter_whenChartsDateFilterStateIsEmitted() = runTest(dispatcher) {
        every { settingsRepository.getChartsDateFilterState() } returns flowOf(chartsFilter)
        val useCase = GetChartsDateFilterStateUseCase(settingsRepository)

        val result = useCase().first()

        assertTrue(result.isSuccess)
        assertEquals(chartsFilter, result.getOrThrow())
    }

    @Test
    fun invoke_shouldPersistFilter_whenSetChartsDateFilterStateSucceeds() = runTest(dispatcher) {
        coEvery { settingsRepository.setChartsDateFilterState(chartsFilter) } returns Unit
        val useCase = SetChartsDateFilterStateUseCase(
            settingsRepository = settingsRepository,
            dispatcher = dispatcher,
        )

        val result = useCase(chartsFilter)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { settingsRepository.setChartsDateFilterState(chartsFilter) }
    }

    @Test
    fun invoke_shouldReturnSuccess_whenShowPaymentTypeBreakdownIsEmitted() = runTest(dispatcher) {
        every { settingsRepository.getShowPaymentTypeBreakdown() } returns flowOf(true)
        val useCase = GetShowPaymentTypeBreakdownUseCase(settingsRepository)

        val result = useCase().first()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
    }

    @Test
    fun invoke_shouldPersistValue_whenSetShowPaymentTypeBreakdownSucceeds() = runTest(dispatcher) {
        coEvery { settingsRepository.setShowPaymentTypeBreakdown(true) } returns Unit
        val useCase = SetShowPaymentTypeBreakdownUseCase(
            settingsRepository = settingsRepository,
            dispatcher = dispatcher,
        )

        val result = useCase(true)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { settingsRepository.setShowPaymentTypeBreakdown(true) }
    }
}

