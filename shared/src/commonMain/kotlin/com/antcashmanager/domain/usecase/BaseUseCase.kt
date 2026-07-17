package com.antcashmanager.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * Base class per UseCase con parametri che eseguono operazioni sospendibili.
 *
 * Il dispatcher è iniettabile per garantire testabilità con [kotlinx.coroutines.test.TestDispatcher].
 * Default: [Dispatchers.Default] (compatibile KMP commonMain).
 * In Android, usare [Dispatchers.IO] per operazioni su database/rete.
 *
 * @param Params Tipo del parametro di input
 * @param R Tipo del risultato
 * @param dispatcher Dispatcher su cui viene eseguita la logica. Default [Dispatchers.Default].
 */
abstract class BaseUseCase<in Params, out R>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    /**
     * Implementa la logica di business del UseCase.
     * NON chiamare direttamente: usare [invoke].
     */
    protected abstract suspend fun execute(params: Params): R

    /**
     * Esegue il UseCase sul dispatcher configurato.
     * Supporta cancellazione cooperativa tramite structured concurrency: [ensureActive] verifica
     * che il Job non sia già stato cancellato prima di avviare [execute], così un'esecuzione
     * cancellata non svolge lavoro inutile (rilevante soprattutto per logica CPU-bound senza
     * altri suspension point).
     *
     * `final`: le subclass implementano SOLO [execute], mai [invoke], per garantire
     * che il dispatcher configurato sia sempre rispettato.
     */
    suspend operator fun invoke(params: Params): R = withContext(dispatcher) {
        ensureActive()
        execute(params)
    }
}
