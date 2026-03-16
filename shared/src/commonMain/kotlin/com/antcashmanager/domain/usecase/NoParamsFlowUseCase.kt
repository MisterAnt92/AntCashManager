package com.antcashmanager.domain.usecase

import kotlinx.coroutines.flow.Flow

abstract class NoParamsFlowUseCase<out Result> {
    abstract operator fun invoke(): Flow<Result>
}
