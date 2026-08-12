# Agent: ViewModel & StateFlow Pattern

**Purpose**: Specialized guidance for implementing ViewModel with StateFlow state management.

**See Also**: [AGENTS.md](../../AGENTS.md) for complete testing rules and architecture.

---

## StateFlow State Management Pattern

**Golden Rule**: ViewModel exposes immutable UI state via StateFlow.

```kotlin
package com.antcashmanager.android.ui.screen.yourfeature

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.ViewModel

data class YourFeatureState(
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class YourFeatureViewModel(
    private val useCase1: YourFeatureUseCase,
    private val useCase2: AnotherUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(YourFeatureState())
    val state: StateFlow<YourFeatureState> = _state.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            useCase1(params)
                .onSuccess { items ->
                    _state.update { it.copy(items = items, isLoading = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(error = error.message, isLoading = false) }
                }
        }
    }
}
```

**Critical**:
- ✅ MutableStateFlow PRIVATE (`_state`)
- ✅ StateFlow PUBLIC (`state`)
- ✅ Use `.asStateFlow()` to expose immutable view
- ✅ Use `.update { }` to modify state (safe, non-blocking)
- ❌ Never expose MutableStateFlow

---

## Constructor Requirements (CRITICAL)

ViewModel accepts **UseCase instances ONLY**, never repositories:

```kotlin
// ✅ CORRECT - UseCase dependencies
class TransactionsViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
) : ViewModel()

// ❌ WRONG - Direct repository dependency
class TransactionsViewModel(
    private val repository: TransactionRepository, // FORBIDDEN!
)
```

**Why**: Maintains Clean Architecture - Presentation layer depends on Domain (UseCase), not Data layer.

---

## Result<T> Consumption Pattern

```kotlin
fun insertTransaction(transaction: Transaction) {
    viewModelScope.launch {
        insertTransactionUseCase(transaction)
            .onSuccess { id ->
                Logger.i(TAG) { "Insert successful: $id" }
                _state.update { it.copy(lastInsertedId = id, error = null) }
            }
            .onFailure { error ->
                if (error is CancellationException) throw error // RE-THROW!
                Logger.e(TAG, error) { "Insert failed" }
                
                val message = when (error) {
                    is TransactionException.InvalidAmount -> "Invalid amount"
                    is TransactionException.DuplicateId -> "Transaction exists"
                    else -> error.message ?: "Unknown error"
                }
                _state.update { it.copy(error = message) }
            }
    }
}
```

**Critical**:
- ✅ Handle both onSuccess and onFailure
- ✅ Re-throw CancellationException
- ✅ Map domain exceptions to user-friendly messages
- ✅ Always update state for UI reflection

---

## Flow-Based State Collection

For continuous reactive data:

```kotlin
// ✅ CORRECT - Flow UseCase creates StateFlow
class TransactionsViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase,
) : ViewModel() {

    val transactions: StateFlow<List<Transaction>> = getTransactionsUseCase()
        .map { result ->
            result.getOrElse { emptyList() }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )
}

// ✅ CORRECT - Combining multiple flows
val uiState: StateFlow<MyState> = combine(
    transactionsUseCase(),
    settingsRepository.displayMode(),
) { transactions, displayMode ->
    MyState(
        transactions = transactions.getOrElse { emptyList() },
        displayMode = displayMode,
    )
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MyState())
```

---

## Cancellation & Cleanup

Handle coroutine cancellation properly:

```kotlin
// Option 1: Track active jobs
private var activeJob: Job? = null

fun cancelOperation() {
    activeJob?.cancel()
    activeJob = null
}

fun loadData() {
    activeJob?.cancel()
    activeJob = viewModelScope.launch {
        // ...
    }
}

// Option 2: Simple viewModelScope (no cleanup needed)
fun loadData() {
    viewModelScope.launch {
        // Automatically cancelled when ViewModel destroyed
    }
}
```

---

## Logging with Kermit

Never use `Log.d()` or `println()`:

```kotlin
import co.touchlab.kermit.Logger

class YourViewModel(...) : ViewModel() {
    companion object {
        private const val TAG = "YourViewModel"
    }

    fun someAction() {
        Logger.d(TAG) { "Action started" }
        
        viewModelScope.launch {
            useCase()
                .onSuccess { result ->
                    Logger.i(TAG) { "Success: $result" }
                }
                .onFailure { error ->
                    Logger.e(TAG, error) { "Failed with error" }
                }
        }
    }
}
```

**Kermit advantages**:
- Works across KMP platforms
- Structured logging
- Tag-based filtering
- Production-ready

---

## State Data Class

Keep state simple, flat, and UI-focused:

```kotlin
// ✅ CORRECT - Flat, UI-focused state
data class TransactionsState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val sortBy: SortOption = SortOption.DATE,
    val selectedTransactionId: Long? = null,
)

// ❌ WRONG - Nested, complex state
data class TransactionsState(
    val data: TransactionData = TransactionData(),
    val metadata: MetadataInfo = MetadataInfo(),
    val ui: UiState = UiState(),
)

// ❌ WRONG - Duplicates ViewModel field
data class TransactionsState(
    val repository: TransactionRepository, // WHY?
)
```

**Rules**:
- ✅ Keep &lt;100 lines
- ✅ All fields related to UI
- ✅ NO repository or UseCase references
- ✅ Stay in `<Feature>State.kt` file only
- ❌ NO typealias
- ❌ NO nested data classes

---

## Code Size Limits

- **ViewModel: Max 300 lines** - if growing larger, extract UseCase or split screen
- **State: Max 100 lines** - keep simple
- **Single responsibility** - one feature per ViewModel

---

## Testing ViewModel

Use `BaseUnitTest` in `androidApp/src/test/`:

```kotlin
class TransactionsViewModelTest : BaseUnitTest() {
    private val mockUseCase = mockk<GetTransactionsUseCase>()
    private lateinit var viewModel: TransactionsViewModel

    @Before
    fun setup() {
        viewModel = TransactionsViewModel(mockUseCase)
    }

    @After
    fun tearDown() {
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun loadTransactions_shouldUpdateState_whenUseCaseSucceeds() = runViewModelTest {
        val expected = listOf(testTransaction(id = 1))
        coEvery { mockUseCase(Unit) } returns Result.success(expected)
        
        val collectJob = launch { viewModel.state.collect {} }
        viewModel.loadTransactions()
        advanceUntilIdle()
        
        assertEquals(expected, viewModel.state.value.transactions)
        assertFalse(viewModel.state.value.isLoading)
        collectJob.cancel()
    }

    @Test
    fun loadTransactions_shouldSetError_whenUseCaseFails() = runViewModelTest {
        val error = TransactionException.FetchFailed()
        coEvery { mockUseCase(Unit) } returns Result.failure(error)
        
        val collectJob = launch { viewModel.state.collect {} }
        viewModel.loadTransactions()
        advanceUntilIdle()
        
        assertNull(viewModel.state.value.transactions)
        assertNotNull(viewModel.state.value.error)
        collectJob.cancel()
    }
}
```

---

## Pre-Commit Checklist

- [ ] Extends `ViewModel`
- [ ] Constructor accepts UseCase instances ONLY
- [ ] Exposes public `StateFlow&lt;State&gt;`
- [ ] Keeps `MutableStateFlow` private
- [ ] Uses `.update { }` for state changes
- [ ] Uses `onSuccess`/`onFailure` for Result handling
- [ ] Re-throws CancellationException
- [ ] Uses Kermit for logging (never Log/println)
- [ ] Handles cancellation properly
- [ ] Under 300 lines
- [ ] State data class exists and under 100 lines
- [ ] Tests cover success + failure paths
- [ ] No UI logic in ViewModel
- [ ] Imports clean, package correct

---

## Quick Links

- **Full Architecture Guide**: [AGENTS.md](../../AGENTS.md)
- **UseCase Patterns**: [agent-usecase-pattern.agent.md](agent-usecase-pattern.agent.md)
- **UI/Screen Guide**: [agent-compose-ui.agent.md](agent-compose-ui.agent.md)
