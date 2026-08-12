# AntCashManager Architecture Overview

**Quick start guide for understanding the app structure and implementing new features.**

---

## 3-Layer Clean Architecture

```
┌─────────────────────────────────────┐
│   PRESENTATION (androidApp)         │  User Interface
│   - Screens (Composables)           │  State Management (StateFlow)
│   - ViewModels (no business logic)  │  Navigation
└─────────────────┬───────────────────┘
                  │ depends on Domain only
┌─────────────────▼───────────────────┐
│   DOMAIN (shared/commonMain)        │  Pure Kotlin (no Android)
│   - UseCases (business logic)       │  Models & Exceptions
│   - Repository Interfaces           │  Type-safe Result<T>
└─────────────────┬───────────────────┘
                  │ implemented by Data
┌─────────────────▼───────────────────┐
│   DATA (shared/androidMain)         │  Persistence
│   - Repository Implementations      │  Database (Room)
│   - Local Data (DataStore)          │  Entity Mapping
└─────────────────────────────────────┘
```

**Golden Rule**: Dependency flows DOWN only. Presentation → Domain → Data.

---

## Feature Implementation Flow

### 1. Define Domain UseCase
```
shared/commonMain/domain/usecase/<feature>/
└── Get<Feature>UseCase.kt          # Business logic, pure Kotlin
```

### 2. Create Data Repository
```
shared/androidMain/data/repository/
└── <Feature>RepositoryImpl.kt       # Implements domain interface
```

### 3. Build ViewModel
```
androidApp/ui/screen/<feature>/
├── <Feature>ViewModel.kt            # Injects UseCase only
├── <Feature>State.kt                # Immutable UI state
└── <Feature>Screen.kt               # Composable UI
```

### 4. Implement Screen Composable
```
androidApp/ui/screen/<feature>/view/
├── <Component>View.kt               # Sub-composables
└── <AnotherComponent>View.kt
```

### 5. Wire Dependencies (Koin DI)
```
androidApp/di/
└── AppModule.kt                     # UseCase & ViewModel registration
```

---

## Key Patterns at a Glance

### UseCase (Domain)
```kotlin
class GetTransactionsUseCase(
    private val repository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsObservableUseCase<List<Transaction>>(dispatcher) {
    override fun execute(): Flow<List<Transaction>> = 
        repository.getAllTransactions()
}
```
✅ Extends base class | ✅ Accepts dispatcher | ✅ Implements execute() | ✅ Returns raw value (base class wraps in Result)

### ViewModel (Presentation)
```kotlin
class TransactionsViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(TransactionsState())
    val state: StateFlow<TransactionsState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            getTransactionsUseCase()
                .map { result → result.getOrElse { emptyList() } }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
        }
    }
}
```
✅ Private MutableStateFlow | ✅ Public StateFlow | ✅ Injects UseCase only | ✅ Handles Result

### Screen (Presentation UI)
```kotlin
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    Column {
        ScreenHeader(title = stringResource(R.string.transactions))
        TransactionsContent(state = state)
    }
}
```
✅ No business logic | ✅ All strings via stringResource | ✅ State parameter passed | ✅ Reuse components

---

## File Organization (Package-by-Feature)

```
androidApp/
├── src/main/kotlin/com/antcashmanager/android/
│   ├── ui/screen/
│   │   ├── home/
│   │   │   ├── HomeScreen.kt
│   │   │   ├── HomeViewModel.kt
│   │   │   ├── HomeState.kt
│   │   │   ├── HomeConstants.kt (if needed)
│   │   │   ├── model/ (feature models)
│   │   │   └── view/ (sub-composables)
│   │   ├── transactions/
│   │   ├── categories/
│   │   ├── charts/
│   │   ├── settings/
│   │   └── ...
│   ├── ui/components/
│   │   ├── card/
│   │   │   ├── AppCard.kt (reusable)
│   │   │   └── BalanceCard.kt
│   │   ├── button/
│   │   │   └── AppButton.kt
│   │   ├── layout/
│   │   │   └── ScreenHeader.kt
│   │   └── ...
│   ├── di/
│   │   └── AppModule.kt (DI configuration)
│   └── navigation/
│       └── NavGraph.kt (routing)
│
shared/
├── src/commonMain/kotlin/com/antcashmanager/
│   └── domain/
│       ├── model/
│       │   ├── Transaction.kt
│       │   ├── Category.kt
│       │   └── ...
│       ├── repository/
│       │   └── TransactionRepository.kt (interfaces)
│       ├── usecase/
│       │   ├── transaction/
│       │   │   ├── GetTransactionsUseCase.kt
│       │   │   ├── InsertTransactionUseCase.kt
│       │   │   └── ...
│       │   └── ...
│       └── exception/
│           ├── TransactionException.kt
│           ├── CategoryException.kt
│           └── ...
│
├── src/androidMain/kotlin/com/antcashmanager/
│   └── data/
│       ├── repository/
│       │   └── TransactionRepositoryImpl.kt
│       ├── local/
│       │   ├── database/
│       │   │   └── TransactionDao.kt
│       │   └── datasource/
│       │       └── TransactionLocalDataSource.kt
│       └── mapper/
│           └── TransactionMapper.kt
```

**Key Rule**: Organize by FEATURE (home/, transactions/), NOT by technical type (models/, repositories/).

---

## Testing Strategy

| What to Test | Where | Base Class | Framework |
|---|---|---|---|
| ViewModel logic | `androidApp/src/test/` | `BaseUnitTest` | JUnit 4 + MockK |
| UseCase business logic | `shared/src/commonTest/` | `BaseUseCaseTest` | JUnit 4 + MockK |
| Repository implementations | `shared/src/androidHostTest/` | — | JUnit 4 + MockK |
| Full screen flows | `androidApp/src/androidTest/` | — | AndroidJUnit4 + Compose |

✅ MockK only (no Mockito) | ✅ Compose UI Test v2 (no Roboelectric in unit tests) | ✅ Test naming: `method_shouldExpectedBehavior_whenCondition`

---

## Dependency Injection (Koin)

```kotlin
// AppModule.kt
val dataModule = module {
    single<TransactionRepository> { TransactionRepositoryImpl(...) }
    // Room DB, DataStore, etc.
}

val useCaseModule = module {
    single { GetTransactionsUseCase(get()) }
    single { InsertTransactionUseCase(get()) }
}

val presentationModule = module {
    viewModel { TransactionsViewModel(get()) }
}

val appModules = listOf(dataModule, useCaseModule, presentationModule)
```

**Rule**: Inject UseCase (and only UseCase) into ViewModels, never repositories directly.

---

## Important Rules

### ✅ DO

- ✅ Keep business logic in UseCase (domain layer)
- ✅ Use StateFlow for UI state (immutable, observable)
- ✅ Inject `CoroutineDispatcher` into UseCase for testability
- ✅ Extend base classes (`UseCase`, `ViewModel`, `BaseUnitTest`)
- ✅ Use `stringResource()` for ALL user strings (5 locales: en, it, fr, de, es)
- ✅ Reuse components from `ui/components/` before creating new ones
- ✅ Apply MaterialTheme for colors/typography
- ✅ Handle `Result<T>` with `.onSuccess { }` and `.onFailure { }`
- ✅ Re-throw `CancellationException` in catch blocks

### ❌ DON'T

- ❌ Put business logic in composables
- ❌ Pass Repository to ViewModel (use UseCase instead)
- ❌ Hardcode strings, colors, or font sizes
- ❌ Use Mockito (use MockK only)
- ❌ Swallow `CancellationException`
- ❌ Import Android in Domain layer
- ❌ Recreate existing components
- ❌ Return `Result<T>` from UseCase.execute() (base class wraps it)

---

## Quick Links

- **Complete Guide**: [AGENTS.md](../../AGENTS.md)
- **UseCase Patterns**: [.github/agents/agent-usecase-pattern.agent.md](.github/agents/agent-usecase-pattern.agent.md)
- **ViewModel Patterns**: [.github/agents/agent-viewmodel-stateflow.agent.md](.github/agents/agent-viewmodel-stateflow.agent.md)
- **UI Patterns**: [.github/agents/agent-compose-ui.agent.md](.github/agents/agent-compose-ui.agent.md)
- **Testing Guide**: [.github/agents/agent-unit-tests-mockk.agent.md](.github/agents/agent-unit-tests-mockk.agent.md)

---

## New Feature Checklist

- [ ] Create UseCase in `domain/usecase/<feature>/`
- [ ] Create Repository interface in `domain/repository/`
- [ ] Implement Repository in `data/repository/`
- [ ] Create ViewModel in `ui/screen/<feature>/`
- [ ] Create State data class in `ui/screen/<feature>/`
- [ ] Create Screen composable in `ui/screen/<feature>/`
- [ ] Extract sub-composables to `ui/screen/<feature>/view/`
- [ ] Add ALL strings to `strings.xml` (5 locales)
- [ ] Add 2+ Previews (light + dark) to Screen
- [ ] Write ViewModel tests in `androidApp/src/test/`
- [ ] Write UseCase tests in `shared/src/commonTest/`
- [ ] Register UseCase & ViewModel in `AppModule.kt`
- [ ] Add navigation route to `NavGraph.kt`
- [ ] Pre-commit verification (imports, build, tests)

---

**Last Updated**: 2026-08-12  
**Version**: v1.7.0

For detailed guidance on any pattern, see linked agents above.
