package com.antcashmanager.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * Base class per UseCase senza parametri che eseguono operazioni sospendibili.
 *
 * Il dispatcher è iniettabile per garantire testabilità con [kotlinx.coroutines.test.TestDispatcher].
 * Default: [Dispatchers.Default] (compatibile KMP commonMain).
 * In Android, usare [Dispatchers.IO] per operazioni su database/rete.
 *
 * @param R Tipo del risultato
 * @param dispatcher Dispatcher su cui viene eseguita la logica. Default [Dispatchers.Default].
 */
abstract class NoParamsUseCase<out R>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    /**
     * Implementa la logica di business del UseCase.
     * NON chiamare direttamente: usare [invoke].
     */
    protected abstract suspend fun execute(): R

    /**
     * Esegue il UseCase sul dispatcher configurato.
     * Supporta cancellazione cooperativa tramite structured concurrency.
     *
     * `final`: le subclass implementano SOLO [execute], mai [invoke], per garantire
     * che il dispatcher configurato sia sempre rispettato.
     */
    suspend operator fun invoke(): R = withContext(dispatcher) {
        ensureActive()
        execute()
    }
}
