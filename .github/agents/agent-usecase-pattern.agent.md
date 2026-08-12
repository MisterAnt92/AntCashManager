# Agent: UseCase Pattern Implementation

**Purpose**: Specialized guidance for implementing domain UseCase classes with proper patterns.

**See Also**: [AGENTS.md](../../AGENTS.md) for complete architecture rules and testing standards.

---

## UseCase Base Class Selection

| Scenario | Base Class | Method | Example |
|----------|-----------|--------|---------|
| Single suspend call + params | `UseCase<Params, T>` | `override suspend fun execute(params: Params): T` | Insert transaction |
| Single suspend call, no params | `NoParamsUseCase<T>` | `override suspend fun execute(): T` | Get current balance |
| Flow-based stream + params | `ObservableUseCase<Params, T>` | `override fun execute(params): Flow<T>` | Observe transactions |
| Flow-based stream, no params | `NoParamsObservableUseCase<T>` | `override fun execute(): Flow<T>` | Observe settings |

---

## Implementation Pattern

```kotlin
package com.antcashmanager.domain.usecase.yourfeature

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * [Purpose description].
 * Returns [Result.success] with [ExpectedType] or [Result.failure] with [DomainException].
 * 
 * @param repository Required dependency
 * @param dispatcher Dispatcher for execution (default: Dispatchers.Default)
 */
class YourFeatureUseCase(
    private val repository: YourRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<InputParams, OutputType>(dispatcher) {

    override suspend fun execute(params: InputParams): OutputType {
        // Business logic here
        return repository.operation(params)
    }
}
```

**CRITICAL**: Implement `execute()` ONLY. Base class provides:
- `invoke()` - wraps execute() result in `Result<T>`
- Dispatcher management - executes on provided dispatcher
- CancellationException preservation - never swallowed

---

## Result<T> Pattern (MANDATORY)

**All UseCase must return Result<T>**

```kotlin
// ✅ CORRECT - Base class wraps return value
class GetUserUseCase(...) : UseCase<Long, User>(dispatcher) {
    override suspend fun execute(params: Long): User {
        return repository.getUser(params) 
            ?: throw UserException.NotFound(params)
    }
    // invoke() automatically wraps in Result<User>
}

// ❌ WRONG - Returning Result from execute()
class GetUserUseCase(...) : UseCase<Long, Result<User>>(dispatcher) {
    override suspend fun execute(params: Long): Result<User> = runCatching {
        repository.getUser(params) ?: throw UserException.NotFound(params)
    } // Double-wrapped!
}
```

---

## Custom Domain Exceptions

Define in `shared/commonMain/domain/exception/`:

```kotlin
sealed class TransactionException(message: String) : Exception(message) {
    class NotFound(id: Long) : TransactionException("Transaction $id not found")
    class InvalidAmount(amount: Double) : TransactionException("Amount must be > 0")
    class InsertFailed(cause: Throwable? = null) : TransactionException("Insert failed")
}

// Usage in UseCase
override suspend fun execute(params: Transaction): Unit {
    if (params.amount <= 0) throw TransactionException.InvalidAmount(params.amount)
    repository.insert(params)
}
```

**Rules**:
- ✅ Sealed classes for type safety
- ✅ Domain-specific names (TransactionException, not DataException)
- ✅ Live in Domain layer ONLY
- ❌ Generic exceptions (Exception, RuntimeException)

---

## Dispatcher Injection (MANDATORY)

All UseCase must accept dispatcher for testability:

```kotlin
// ✅ CORRECT
class MyUseCase(
    private val repo: Repository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default, // Testable default
) : UseCase<Params, Result>(dispatcher)

// ❌ WRONG - No dispatcher injection
class MyUseCase(
    private val repo: Repository,
) : UseCase<Params, Result>() // Can't override in tests!
```

**Why**: Allows tests to inject TestDispatcher for deterministic execution.

---

## Flow-Based UseCase

For reactive, continuous data streams:

```kotlin
// ✅ CORRECT - Flow UseCase
class GetTransactionsUseCase(
    private val repository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsObservableUseCase<List<Transaction>>(dispatcher) {

    override fun execute(): Flow<List<Transaction>> =
        repository.getAllTransactions()
}

// Usage in ViewModel
val transactions = getTransactionsUseCase()
    .map { result -> 
        result.onSuccess { setState { copy(transactions = it) } }
                .onFailure { setState { copy(error = it) } }
    }
    .stateIn(...)
```

---

## Cancellation Safety (CRITICAL)

Never swallow `CancellationException`:

```kotlin
// ✅ CORRECT - CancellationException propagates
class GetDataUseCase(...) : UseCase<Unit, Data>(dispatcher) {
    override suspend fun execute(params: Unit): Data {
        try {
            return repository.getData()
        } catch (e: Exception) {
            if (e is CancellationException) throw e // RE-THROW!
            throw DataException.FetchFailed(e)
        }
    }
}

// ❌ WRONG - Swallows CancellationException
try {
    return repository.getData()
} catch (e: Exception) {
    throw DataException.FetchFailed(e) // CancellationException caught!
}
```

---

## Code Length & Quality

- **Max 250 lines**: UseCase should be focused, single responsibility
- **KDoc mandatory**: Document purpose, params, exceptions, return value
- **No UI logic**: UseCase is domain-only
- **No direct DB/Network**: Use injected repository
- **No logging in execute()**: ViewModel handles logging

```kotlin
/**
 * Retrieves transaction by ID from repository.
 * 
 * @param params Transaction ID to fetch
 * @return [Transaction] if found
 * @throws [TransactionException.NotFound] if transaction doesn't exist
 * @throws [TransactionException.FetchFailed] if repository fails
 * 
 * **Usage**:
 * ```
 * getTransactionUseCase(123L)
 *     .onSuccess { transaction -> ... }
 *     .onFailure { error -> ... }
 * ```
 */
class GetTransactionUseCase(
    private val repository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<Long, Transaction>(dispatcher) { ... }
```

---

## Testing UseCase

Use `BaseUseCaseTest` in shared module:

```kotlin
class GetTransactionUseCaseTest : BaseUseCaseTest() {
    private val mockRepository = mockk<TransactionRepository>()
    private val useCase = GetTransactionUseCase(mockRepository)

    @Test
    fun invoke_shouldReturnTransaction_whenRepositorySucceeds() = runUnitTest {
        val expected = testTransaction(id = 123)
        coEvery { mockRepository.get(123) } returns expected
        
        val result = useCase(123)
        
        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
        coVerify(exactly = 1) { mockRepository.get(123) }
    }

    @Test
    fun invoke_shouldReturnFailure_whenRepositoryThrows() = runUnitTest {
        coEvery { mockRepository.get(any()) } throws SQLException()
        
        val result = useCase(999)
        
        assertTrue(result.isFailure)
    }
}
```

---

## Pre-Commit Checklist

- [ ] Extends correct base class (UseCase, NoParamsUseCase, ObservableUseCase, etc.)
- [ ] `execute()` implemented, `invoke()` NOT overridden
- [ ] Accepts `CoroutineDispatcher` parameter with default
- [ ] Returns Result&lt;T&gt; (or T if using Flow)
- [ ] Custom exceptions in Domain layer only
- [ ] CancellationException never swallowed
- [ ] KDoc documentation complete
- [ ] Under 250 lines
- [ ] No UI logic
- [ ] No direct repository operations (use injected repository)
- [ ] Tests cover happy path + failure scenarios
- [ ] Imports clean, package correct

---

## Quick Links

- **Full Testing Guide**: [agent-unit-tests-mockk.agent.md](agent-unit-tests-mockk.agent.md)
- **Architecture Overview**: [AGENTS.md](../../AGENTS.md)
- **Example UseCase**: `shared/src/commonMain/kotlin/com/antcashmanager/domain/usecase/`
