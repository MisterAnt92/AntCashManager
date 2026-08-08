package com.antcashmanager.domain.usecase.base

import co.touchlab.kermit.Logger
import com.antcashmanager.domain.util.runSuspendCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * Base class per UseCase one-shot con parametri il cui esito è espresso come [Result].
 *
 * Centralizza:
 * - la conversione eccezione -> [Result.failure] tramite [runSuspendCatching], che a differenza
 *   di [runCatching] non intercetta mai [kotlinx.coroutines.CancellationException] (structured
 *   concurrency preservata: la cancellazione si propaga sempre, mai loggata come errore);
 * - l'esecuzione sul dispatcher configurato nel costruttore;
 * - il logging Kermit del flusso (start/success a debug, failure a error) con tag = nome classe.
 *
 * Le subclass implementano SOLO [execute] restituendo direttamente il valore di successo:
 * eventuali eccezioni si propagano normalmente e vengono catturate in un unico punto da [invoke].
 *
 * @param P Tipo del parametro di input
 * @param R Tipo del valore di successo racchiuso nel [Result] restituito da [invoke]
 * @param dispatcher Dispatcher su cui viene eseguita la logica. Default [Dispatchers.Default].
 */
abstract class UseCase<in P, out R>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    protected val log: Logger = Logger.withTag(this::class.simpleName ?: "UseCase")

    /**
     * Implementa la logica di business del UseCase, restituendo il valore di successo o lanciando
     * un'eccezione in caso di errore.
     * NON chiamare direttamente: usare [invoke].
     */
    protected abstract suspend fun execute(params: P): R

    /**
     * Esegue il UseCase sul dispatcher configurato, catturando ogni eccezione (tranne
     * [kotlinx.coroutines.CancellationException]) in un [Result.failure].
     * Il chiamante concatena `.onSuccess { }` / `.onFailure { }` sul [Result] restituito.
     *
     * `final`: le subclass implementano SOLO [execute], mai [invoke], per garantire che il
     * dispatcher configurato, la gestione degli errori e il logging siano sempre rispettati.
     */
    suspend operator fun invoke(params: P): Result<R> = withContext(dispatcher) {
        ensureActive()
        log.d { "started" }
        runSuspendCatching { execute(params) }
            .onSuccess { log.d { "succeeded" } }
            .onFailure { log.e(throwable = it) { "failed" } }
    }
}
