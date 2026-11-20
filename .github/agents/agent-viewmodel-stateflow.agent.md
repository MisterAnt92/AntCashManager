---
description: "ViewModel UDF pattern: AndroidBaseViewModel<Event,State>, handleEvent() routing, inherited StateFlow<State>, UIEvent/UIState, emitEvent(). Use when creating or refactoring a ViewModel, its Event or State."
---

# Agent: ViewModel (UDF)

Rules live in [AGENTS.md §7](../../AGENTS.md). This file gives the templates, copied from real code.

## Architecture types (`:architectureModule`)

- `UIEvent` — abstract class, all Event sealed classes extend it. Carries `id: String` (UUID).
- `UIState` — abstract marker class, all State data classes extend it.
- `BaseViewModel<E, S>` — interface (not class); `AndroidBaseViewModel` implements it.
- `AndroidBaseViewModel<E : UIEvent, S : UIState>` — the concrete Android base (`ViewModel` + `BaseViewModel`). Provides `_state`, `state`, `emitEvent()`, logging helpers, `handleError {}`.

## Files to create

```
ui/screen/<feature>/<Feature>Event.kt      sealed class : UIEvent(), one type per user action
ui/screen/<feature>/<Feature>State.kt      data class : UIState(), includes errorState: ErrorState
ui/screen/<feature>/<Feature>ViewModel.kt  AndroidBaseViewModel<<Feature>Event, <Feature>State>
di/AppModule.kt                            viewModel { FeatureViewModel(get(), get()) }
```

## Event (from `categories/CategoryEvent.kt`)

```kotlin
sealed class CategoryEvent : UIEvent() {
    data class AddCategory(val name: String, val icon: String, val color: Long, val type: String = "EXPENSE") : CategoryEvent()
    data class UpdateCategory(val category: Category) : CategoryEvent()
    data class DeleteCategory(val category: Category) : CategoryEvent()
    data object RetryLastOperation : CategoryEvent()
}
```

## State (from `categories/CategoriesState.kt`)

```kotlin
data class CategoriesState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val errorState: ErrorState = ErrorState(),   // com.antcashmanager.architecture.ErrorState
) : UIState()   // ← required: extends UIState marker from architectureModule
```

## ViewModel (from `categories/CategoriesViewModel.kt`, trimmed)

```kotlin
class CategoriesViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val insertCategoryUseCase: InsertCategoryUseCase,
) : AndroidBaseViewModel<CategoryEvent, CategoriesState>(CategoriesState()) {
    // _state and state are inherited from AndroidBaseViewModel — do NOT redeclare them

    init {
        viewModelScope.launch {
            getCategoriesUseCase().collect { result ->          // Flow<Result<List<Category>>>
                result
                    .onSuccess { cats -> _state.update { it.copy(categories = cats) } }
                    .onFailure { error ->
                        if (error is CancellationException) throw error
                        logError("Error loading categories", error)
                    }
            }
        }
    }

    override fun handleEvent(event: CategoryEvent) {
        logDebug("Event: $event")
        when (event) {
            is CategoryEvent.AddCategory -> addCategory(event.name, event.icon, event.color, event.type)
            is CategoryEvent.UpdateCategory -> updateCategory(event.category)
            is CategoryEvent.DeleteCategory -> deleteCategory(event.category)
            is CategoryEvent.RetryLastOperation -> logInfo("Retry requested")
        }
    }

    private fun addCategory(name: String, icon: String, color: Long, type: String) {
        viewModelScope.launch {
            insertCategoryUseCase(Category(name = name, icon = icon, color = color, type = type))
                .handleError { err -> _state.update { it.copy(errorState = err) } }
        }
    }
    // updateCategory / deleteCategory: same shape
}
```

## Preference-only ViewModel (from `theme/ThemeViewModel.kt`)

Allowed exception: `SettingsRepository` injected directly for preferences.

```kotlin
class ThemeViewModel(
    private val settingsRepository: SettingsRepository,
) : AndroidBaseViewModel<ThemeEvent, ThemeState>(ThemeState()) {
    // init: collect settingsRepository flows → _state.update { it.copy(...) }

    override fun handleEvent(event: ThemeEvent) {
        when (event) {
            is ThemeEvent.SetTheme -> viewModelScope.launch { settingsRepository.setTheme(event.theme) }
        }
    }
}
```
Prefer a single `state: StateFlow<State>` built with `combine(...)` when there are 2+ flows (see `DisplayViewModel`).

## Test (from `BaseUnitTest`)

```kotlin
class CategoriesViewModelTest : BaseUnitTest() {
    private val getCategories = mockk<GetCategoriesUseCase>()
    private val insertCategory = mockk<InsertCategoryUseCase>()
    private lateinit var viewModel: CategoriesViewModel

    @Before
    fun setup() {
        every { getCategories() } returns flowOf(Result.success(listOf(testCategory(name = "Food"))))
        viewModel = CategoriesViewModel(getCategories, insertCategory)
    }

    @Test
    fun handleEvent_shouldInsertCategory_whenAddCategoryReceived() = runViewModelTest {
        coEvery { insertCategory(any()) } returns Result.success(Unit)

        viewModel.emitEvent(CategoryEvent.AddCategory("Rent", "home", 0xFF0000L))
        advanceUntilIdle()

        coVerify(exactly = 1) { insertCategory(match { it.name == "Rent" }) }
        assertFalse(viewModel.state.value.errorState.isError)
    }
}
```

## Checklist

- [ ] Extends `AndroidBaseViewModel<FeatureEvent, FeatureState>(FeatureState())`
- [ ] `FeatureEvent : UIEvent()`, `FeatureState : UIState()` — required parent classes
- [ ] Do NOT redeclare `_state` or `state` — they are inherited
- [ ] `override fun handleEvent(event: E)` is the internal router (protected, not public)
- [ ] `emitEvent(event)` is the public entry point — called from Screen via `viewModel::emitEvent`
- [ ] Business data via UseCases; `SettingsRepository` only for preferences
- [ ] `handleError { }` for `Result`, re-throw `CancellationException` in flow collectors
- [ ] `logDebug/logError` from base — no `TAG`, `Log`, `println`
- [ ] No `Context`; ≤ 300 lines; State ≤ 100 lines in its own file
- [ ] Registered in `AppModule.presentationModule`
- [ ] Test extends `BaseUnitTest`, drives via `emitEvent`, name `method_shouldX_whenY`
- [ ] Imports clean, package matches directory
