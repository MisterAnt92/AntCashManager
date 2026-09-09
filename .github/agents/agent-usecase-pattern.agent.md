---
description: "Domain UseCase implementation: base class choice, execute() returning raw value, dispatcher injection, domain exceptions, BaseUseCaseTest. Use when creating or testing a UseCase in shared/commonMain."
---

# Agent: UseCase

Rules live in [AGENTS.md §6](../../AGENTS.md). Base classes: `shared/src/commonMain/kotlin/com/antcashmanager/domain/usecase/base/`.

## Choose the base class

| Params | Flow | Base class | You implement |
|---|---|---|---|
| yes | no | `UseCase<P, R>` | `override suspend fun execute(params: P): R` |
| no | no | `NoParamsUseCase<R>` | `override suspend fun execute(): R` |
| yes | yes | `ObservableUseCase<P, R>` | `override fun execute(params: P): Flow<R>` |
| no | yes | `NoParamsObservableUseCase<R>` | `override fun execute(): Flow<R>` |

Multiple params → a nested `data class Params(...)` inside the UseCase (see `SyncTransactionCategoriesUseCase.Params`).

## Template

```kotlin
package com.antcashmanager.domain.usecase.transaction

/**
 * Inserts [Transaction]; fails with [TransactionException.InvalidAmount] when amount is 0.
 */
class InsertTransactionUseCase(
    private val repository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<Transaction, Long>(dispatcher) {
    override suspend fun execute(params: Transaction): Long {
        if (params.amount == 0.0) throw TransactionException.InvalidAmount(params.amount)
        return repository.insert(params)
    }
}
// Caller: insertTransactionUseCase(tx).onSuccess { id -> } .onFailure { e -> }
```

```kotlin
class GetCategoriesUseCase(
    private val repository: CategoryRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsObservableUseCase<List<Category>>(dispatcher) {
    override fun execute(): Flow<List<Category>> = repository.getAll()
}
// Caller collects Flow<Result<List<Category>>>
```

## What the base already does (do not re-implement)

- `invoke()` is final: runs `execute()` on the dispatcher, wraps in `Result`, preserves `CancellationException` (`runSuspendCatching` / `catch` operator), logs start/success/failure with Kermit (`log` field available).
- ⚠️ **Never** `execute(): Result<R> = runCatching { }` — double wrap. **Never** `try/catch` a `CancellationException` yourself.
- No logging inside `execute()` unless it adds domain context.

## Domain exceptions — `domain/exception/`

```kotlin
sealed class TransactionException(message: String) : Exception(message) {
    class NotFound(id: Long) : TransactionException("Transaction $id not found")
    class InvalidAmount(amount: Double) : TransactionException("Invalid amount: $amount")
}
```

## Settings get/set → generics, no new class

```kotlin
// AppModule.kt
factory<GetSettingUseCase<AppTheme>> { GetSettingUseCase(getter = { get<SettingsRepository>().getTheme() }) }
factory<SetSettingUseCase<AppTheme>> { SetSettingUseCase(setter = { get<SettingsRepository>().setTheme(it) }) }
```

## Test — `shared/src/commonTest`, extends `BaseUseCaseTest`

```kotlin
class InsertTransactionUseCaseTest : BaseUseCaseTest() {
    private val repository = mockk<TransactionRepository>()
    private val useCase = InsertTransactionUseCase(repository, testDispatcher)

    @Test
    fun invoke_shouldReturnId_whenRepositoryInserts() = runUnitTest {
        coEvery { repository.insert(any()) } returns 42L
        val result = useCase(testTransaction { amount = 10.0 })
        assertEquals(42L, result.getOrThrow())
    }

    @Test
    fun invoke_shouldFailWithInvalidAmount_whenAmountIsZero() = runUnitTest {
        val result = useCase(testTransaction { amount = 0.0 })
        assertIs<TransactionException.InvalidAmount>(result.exceptionOrNull())
        coVerify(exactly = 0) { repository.insert(any()) }
    }
}
```
Use `TestDataBuilder` (`testTransaction { }`, `testCategory { }`) and `Fake*Repository` from `shared/src/commonTest/.../testutil/` when a stateful fake reads better than a mock.

## Checklist

- [ ] Correct base class; `execute()` only, returns raw `R` / `Flow<R>`
- [ ] `dispatcher: CoroutineDispatcher = Dispatchers.Default` in constructor
- [ ] Exceptions from `domain/exception/`, never generic `RuntimeException`
- [ ] No Android imports, no UI, no direct DB/network
- [ ] Registered in `AppModule.useCaseModule`
- [ ] KDoc on class; ≤ 250 lines
- [ ] Test: happy path + failure with exact exception type; name `invoke_shouldX_whenY`
- [ ] Imports clean, package matches directory
