package com.antcashmanager.domain.usecase

import com.antcashmanager.domain.util.runSuspendCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * Base class per UseCase con parametri il cui esito è espresso come [Result].
 *
 * Centralizza la conversione eccezione -> [Result.failure] tramite [runSuspendCatching], che a
 * differenza di [runCatching] non intercetta mai [kotlinx.coroutines.CancellationException].
 * Le subclass implementano [execute] restituendo direttamente il valore di successo: eventuali
 * eccezioni si propagano normalmente e vengono catturate in un unico punto da [invoke].
 *
 * @param Params Tipo del parametro di input
 * @param R Tipo del valore di successo racchiuso nel [Result] restituito da [invoke]
 * @param dispatcher Dispatcher su cui viene eseguita la logica. Default [Dispatchers.Default].
 */
abstract class BaseResultUseCase<in Params, out R>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    /**
     * Implementa la logica di business del UseCase, restituendo il valore di successo o lanciando
     * un'eccezione in caso di errore.
     * NON chiamare direttamente: usare [invoke].
     */
    protected abstract suspend fun execute(params: Params): R

    /**
     * Esegue il UseCase sul dispatcher configurato, catturando ogni eccezione (tranne
     * [kotlinx.coroutines.CancellationException]) in un [Result.failure].
     *
     * `final`: le subclass implementano SOLO [execute], mai [invoke], per garantire
     * che il dispatcher configurato e la gestione degli errori siano sempre rispettati.
     */
    suspend operator fun invoke(params: Params): Result<R> = withContext(dispatcher) {
        ensureActive()
        runSuspendCatching { execute(params) }
    }
}
