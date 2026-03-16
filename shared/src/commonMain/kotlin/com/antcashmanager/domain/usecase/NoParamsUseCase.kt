package com.antcashmanager.domain.usecase

abstract class NoParamsUseCase<out Result> {
    abstract suspend operator fun invoke(): Result
}
