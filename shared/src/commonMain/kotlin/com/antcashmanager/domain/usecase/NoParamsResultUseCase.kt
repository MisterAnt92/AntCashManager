package com.antcashmanager.domain.usecase

import com.antcashmanager.domain.util.runSuspendCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * Base class per UseCase senza parametri il cui esito è espresso come [Result].
 *
 * Vedi [BaseResultUseCase] per il razionale: centralizza la conversione eccezione ->
 * [Result.failure] tramite [runSuspendCatching], cancellation-safe.
 *
 * @param R Tipo del valore di successo racchiuso nel [Result] restituito da [invoke]
 * @param dispatcher Dispatcher su cui viene eseguita la logica. Default [Dispatchers.Default].
 */
abstract class NoParamsResultUseCase<out R>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    /**
     * Implementa la logica di business del UseCase, restituendo il valore di successo o lanciando
     * un'eccezione in caso di errore.
     * NON chiamare direttamente: usare [invoke].
     */
    protected abstract suspend fun execute(): R

    /**
     * Esegue il UseCase sul dispatcher configurato, catturando ogni eccezione (tranne
     * [kotlinx.coroutines.CancellationException]) in un [Result.failure].
     *
     * `final`: le subclass implementano SOLO [execute], mai [invoke].
     */
    suspend operator fun invoke(): Result<R> = withContext(dispatcher) {
        ensureActive()
        runSuspendCatching { execute() }
    }
}
