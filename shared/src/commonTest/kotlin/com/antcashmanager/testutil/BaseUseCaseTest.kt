package com.antcashmanager.testutil

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

/**
 * Base comune per i test degli UseCase in `shared`: centralizza il
 * [StandardTestDispatcher] e l'helper [runUnitTest] così i singoli test non
 * duplicano questo boilerplate. Gli UseCase ricevono il dispatcher via
 * costruttore, quindi qui non serve alcuno scambio di `Dispatchers.Main`
 * (necessario invece per i ViewModel in androidApp).
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseUseCaseTest {
    protected val testDispatcher = StandardTestDispatcher()

    protected fun runUnitTest(block: suspend TestScope.() -> Unit) =
        runTest(testDispatcher) { block() }
}
