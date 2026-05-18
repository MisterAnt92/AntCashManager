package com.antcashmanager.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

/**
 * Base class per UseCase con parametri che restituiscono un [Flow].
 *
 * Applica automaticamente [flowOn] con il dispatcher configurato per garantire
 * che la produzione degli elementi avvenga sul thread corretto.
 * Il dispatcher è iniettabile per garantire testabilità con [kotlinx.coroutines.test.TestDispatcher].
 *
 * @param Params Tipo del parametro di input
 * @param Result Tipo degli elementi emessi dal Flow
 * @param dispatcher Dispatcher su cui viene prodotto il Flow. Default [Dispatchers.Default].
 */
abstract class FlowUseCase<in Params, out Result>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    /**
     * Implementa la logica di business del UseCase.
     * NON chiamare direttamente: usare [invoke].
     */
    protected abstract fun execute(params: Params): Flow<Result>

    /**
     * Restituisce il Flow con [flowOn] applicato al dispatcher configurato.
     * Il Flow è cancellabile: la cancellazione del collector cancella la produzione.
     */
    operator fun invoke(params: Params): Flow<Result> = execute(params).flowOn(dispatcher)
}
