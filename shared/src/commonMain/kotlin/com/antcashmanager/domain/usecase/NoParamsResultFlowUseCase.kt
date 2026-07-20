package com.antcashmanager.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Base class per UseCase senza parametri che restituiscono un [Flow] di [Result].
 *
 * Centralizza la conversione valore/eccezione -> [Result] tramite `.map { Result.success(it) }`
 * seguito da [catch]: quest'ultimo è per design trasparente alla cancellazione, cioè non
 * intercetta mai [kotlinx.coroutines.CancellationException] (a differenza di un `try/catch`
 * generico attorno a un `collect`).
 * Le subclass implementano [execute] restituendo il Flow "nudo" del valore di dominio.
 *
 * @param R Tipo del valore di successo racchiuso nel [Result] emesso da [invoke]
 * @param dispatcher Dispatcher su cui viene prodotto il Flow. Default [Dispatchers.Default].
 */
abstract class NoParamsResultFlowUseCase<R>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    /**
     * Implementa la logica di business del UseCase.
     * NON chiamare direttamente: usare [invoke].
     */
    protected abstract fun execute(): Flow<R>

    /**
     * Restituisce il Flow del valore di dominio racchiuso in [Result], con [flowOn] applicato
     * al dispatcher configurato.
     *
     * `final`: le subclass implementano SOLO [execute], mai [invoke].
     */
    operator fun invoke(): Flow<Result<R>> = execute()
        .map { Result.success(it) }
        .catch { emit(Result.failure(it)) }
        .flowOn(dispatcher)
}
