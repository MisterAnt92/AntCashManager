package com.antcashmanager.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Base class per UseCase con parametri che eseguono operazioni sospendibili.
 *
 * Il dispatcher è iniettabile per garantire testabilità con [kotlinx.coroutines.test.TestDispatcher].
 * Default: [Dispatchers.Default] (compatibile KMP commonMain).
 * In Android, usare [Dispatchers.IO] per operazioni su database/rete.
 *
 * @param Params Tipo del parametro di input
 * @param Result Tipo del risultato
 * @param dispatcher Dispatcher su cui viene eseguita la logica. Default [Dispatchers.Default].
 */
abstract class BaseUseCase<in Params, out Result>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    /**
     * Implementa la logica di business del UseCase.
     * NON chiamare direttamente: usare [invoke].
     */
    protected abstract suspend fun execute(params: Params): Result

    /**
     * Esegue il UseCase sul dispatcher configurato.
     * Supporta cancellazione cooperativa tramite structured concurrency.
     */
    suspend operator fun invoke(params: Params): Result = withContext(dispatcher) {
        execute(params)
    }
}
