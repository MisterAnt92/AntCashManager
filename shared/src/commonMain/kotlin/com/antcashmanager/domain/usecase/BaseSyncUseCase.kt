package com.antcashmanager.domain.usecase

/**
 * Base class per UseCase puramente sincroni che non richiedono coroutine o dispatcher.
 * Utile per logiche di trasformazione dati, formattazione o calcoli immediati.
 *
 * @param Params Tipo del parametro di input
 * @param R Tipo del risultato
 */
abstract class BaseSyncUseCase<in Params, out R> {

    /**
     * Implementa la logica di business del UseCase.
     * NON chiamare direttamente: usare [invoke].
     */
    protected abstract fun execute(params: Params): R

    /**
     * Esegue il UseCase in modo sincrono.
     */
    operator fun invoke(params: Params): R = execute(params)
}
