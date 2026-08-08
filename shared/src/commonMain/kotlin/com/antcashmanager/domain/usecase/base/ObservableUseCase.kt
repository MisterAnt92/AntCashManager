package com.antcashmanager.domain.usecase.base

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Base class per UseCase con parametri che restituiscono un [Flow] di [Result] (stream di dati).
 *
 * Centralizza:
 * - la conversione valore/eccezione -> [Result] tramite `.map { Result.success(it) }` seguito da
 *   [catch]: quest'ultimo è per design trasparente alla cancellazione, cioè non intercetta mai
 *   [CancellationException] (a differenza di un `try/catch` generico attorno a un `collect`);
 * - l'applicazione del [flowOn] al dispatcher configurato;
 * - il logging Kermit in caso di errore del flusso (tag = nome classe).
 *
 * Le subclass implementano SOLO [execute] restituendo il Flow "nudo" del valore di dominio:
 * la wrappatura in [Result] e la gestione degli errori avviene centralizzata in [invoke].
 *
 * @param P Tipo del parametro di input
 * @param R Tipo del valore di successo emesso nel [Result] dal flusso restituito da [invoke]
 * @param dispatcher Dispatcher su cui viene prodotto il Flow. Default [Dispatchers.Default].
 */
abstract class ObservableUseCase<in P, R>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    protected val log: Logger = Logger.withTag(this::class.simpleName ?: "ObservableUseCase")

    /**
     * Implementa la logica di business del UseCase.
     * NON chiamare direttamente: usare [invoke].
     */
    protected abstract fun execute(params: P): Flow<R>

    /**
     * Restituisce il Flow del valore di dominio racchiuso in [Result], con [flowOn] applicato
     * al dispatcher configurato. Il [catch] a valle intercetta eccezioni del flusso (non
     * [CancellationException]) e le emette come [Result.failure].
     *
     * `final`: le subclass implementano SOLO [execute], mai [invoke].
     */
    operator fun invoke(params: P): Flow<Result<R>> = execute(params)
        .map { Result.success(it) }
        .catch { throwable ->
            if (throwable is CancellationException) throw throwable
            log.e(throwable = throwable) { "flow failed" }
            emit(Result.failure(throwable))
        }
        .flowOn(dispatcher)
}
