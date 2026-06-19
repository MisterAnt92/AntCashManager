package com.antcashmanager.android

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before

/**
 * Classe base condivisa per i test unitari Android host-side.
 *
 * Centralizza:
 * - sostituzione di [Dispatchers.Main] con un [testDispatcher] deterministico;
 * - esecuzione dei test coroutine tramite [runUnitTest];
 * - helper comuni per lanciare collector/background jobs nei test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseUnitTest {

    protected val testDispatcher = StandardTestDispatcher()

    @Before
    open fun setUpDispatchers() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    open fun tearDownDispatchers() {
        Dispatchers.resetMain()
    }

    protected fun runUnitTest(block: suspend TestScope.() -> Unit) =
        runTest(testDispatcher) { block() }

    protected fun runViewModelTest(block: suspend TestScope.() -> Unit) =
        runUnitTest(block)

    protected fun TestScope.launchInBackground(block: suspend CoroutineScope.() -> Unit): Job =
        backgroundScope.launch(testDispatcher) { block() }
}

