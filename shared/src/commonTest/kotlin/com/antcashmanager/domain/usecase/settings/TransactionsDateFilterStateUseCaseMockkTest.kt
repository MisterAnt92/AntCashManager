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
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsDateFilterStateUseCaseMockkTest {
    private lateinit var settingsRepository: SettingsRepository

    private val expectedFilter =
        SavedDateFilter(
            presetIndex = 2,
            from = 1_715_000_000_000L,
            to = 1_715_500_000_000L,
        )

    @BeforeTest
    fun setup() {
        settingsRepository = mockk()
    }

    @Test
    fun invoke_shouldReturnSuccessFilter_whenRepositoryEmitsDateFilterState() =
        runTest {
            every { settingsRepository.getTransactionsDateFilterState() } returns flowOf(expectedFilter)
            val useCase = GetTransactionsDateFilterStateUseCase(settingsRepository)

            val result = useCase().first()

            assertTrue(result.isSuccess)
            assertEquals(expectedFilter, result.getOrThrow())
        }

    @Test
    fun invoke_shouldReturnFailure_whenRepositoryFlowThrowsException() =
        runTest {
            val expectedError = IllegalStateException("read-failed")
            every { settingsRepository.getTransactionsDateFilterState() } returns
                flow {
                    throw expectedError
                }
            val useCase = GetTransactionsDateFilterStateUseCase(settingsRepository)

            val result = useCase().first()

            assertTrue(result.isFailure)
            assertSame(expectedError, result.exceptionOrNull())
        }

    @Test
    fun invoke_shouldPersistFilter_whenRepositoryAcceptsDateFilterState() =
        runTest {
            coEvery { settingsRepository.setTransactionsDateFilterState(expectedFilter) } returns Unit
            val useCase =
                SetTransactionsDateFilterStateUseCase(
                    settingsRepository = settingsRepository,
                    dispatcher = StandardTestDispatcher(testScheduler),
                )

            val result = useCase(expectedFilter)

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { settingsRepository.setTransactionsDateFilterState(expectedFilter) }
        }

    @Test
    fun invoke_shouldReturnFailure_whenRepositoryRejectsDateFilterState() =
        runTest {
            val expectedError = IllegalArgumentException("write-failed")
            coEvery { settingsRepository.setTransactionsDateFilterState(expectedFilter) } throws expectedError
            val useCase =
                SetTransactionsDateFilterStateUseCase(
                    settingsRepository = settingsRepository,
                    dispatcher = StandardTestDispatcher(testScheduler),
                )

            val result = useCase(expectedFilter)

            assertTrue(result.isFailure)
            assertIs<IllegalArgumentException>(result.exceptionOrNull())
            assertSame(expectedError, result.exceptionOrNull())
            coVerify(exactly = 1) { settingsRepository.setTransactionsDateFilterState(expectedFilter) }
        }
}
