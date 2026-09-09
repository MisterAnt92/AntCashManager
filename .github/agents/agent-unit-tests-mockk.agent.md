---
description: "Unit test con MockK per ViewModel (onEvent), UseCase, repository, helper e componenti Compose. Base class per source set, naming senza backtick, comandi mirati."
---

# Agent: Unit Tests (MockK)

Regole in [AGENTS.md §12](../../AGENTS.md). Qui: dove mettere il test, cosa estendere, come scriverlo.

## Source set → base class → helper

| Cosa testi | Dove | Estendi | Helper |
|---|---|---|---|
| ViewModel, util Android host | `androidApp/src/test/kotlin` | `BaseUnitTest` | `runViewModelTest { }`, `testDispatcher`, `advanceUntilIdle()`, `launchInBackground { }` |
| Componente Compose | `androidApp/src/test/kotlin` | `BaseComposeUnitTest` | `composeTestRule` (`junit4.v2`), eredita `BaseUnitTest` |
| UseCase, logica `commonMain` | `shared/src/commonTest/kotlin` | `BaseUseCaseTest` | `runUnitTest { }`, `testDispatcher`, `TestDataBuilder`, `Fake*Repository` |
| Repository / mapper / parser | `shared/src/androidHostTest/kotlin` | — | JUnit4 + MockK, `runTest` |

Mai scrivere a mano `Dispatchers.setMain`, `StandardTestDispatcher()`, `runTest(...)` in `androidApp`: lo fa la base.

## Cosa non usare (zero tolleranza)

- `org.mockito.*`, `mockito-kotlin`, PowerMock, EasyMock → solo `io.mockk`.
- Robolectric, Room/DataStore reali, `Context`, file I/O in `src/test` (Robolectric solo in `src/androidTest`).
- `androidx.compose.ui.test.junit4.createComposeRule()` v1 → usa `BaseComposeUnitTest`.
- Backtick nei nomi test.

## MockK vs Fake

- **MockK** per i collaboratori del soggetto: UseCase nel test di ViewModel, repository nel test di UseCase, DAO/servizi.
- **Fake** (`testutil/Fake*Repository`) quando serve stato o Flow controllato dal test (cancellazione, in-memory, emissioni multiple).
- Classi pure (formatter, parser): nessun mock, testa l'output.

## ViewModel — guida via `onEvent`

```kotlin
class SettingsViewModelTest : BaseUnitTest() {
    private val settingsRepository = mockk<SettingsRepository>(relaxed = true)
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        every { settingsRepository.getTheme() } returns flowOf(AppTheme.SYSTEM)
        viewModel = SettingsViewModel(settingsRepository)
    }

    @Test
    fun onEvent_shouldPersistTheme_whenSetThemeReceived() = runViewModelTest {
        viewModel.onEvent(SettingEvent.SetTheme(AppTheme.ANNA))
        advanceUntilIdle()
        coVerify(exactly = 1) { settingsRepository.setTheme(AppTheme.ANNA) }
    }
}
```
Copri: stato iniziale, ogni evento → effetto su `state.value` o `coVerify`, failure → `errorState.isError`, cancellazione se c'è `activeJob`.

## UseCase — vedi `agent-usecase-pattern.agent.md` §Test

Asserzioni: `assertEquals(x, result.getOrThrow())`, `assertTrue(result.isFailure)`, `assertIs<DomainException.Case>(result.exceptionOrNull())`.

## Componente Compose

```kotlin
class VisibilityToggleButtonTest : BaseComposeUnitTest() {
    @Test
    fun button_shouldInvokeOnToggle_whenClicked() {
        var toggled = false
        composeTestRule.setContent { VisibilityToggleButton(visible = false, onToggle = { toggled = true }) }
        composeTestRule.onNodeWithContentDescription("Show").performClick()
        assertTrue(toggled)
    }
}
```
Testa visibilità, enabled/disabled, callback, `contentDescription`. Non testare RGB esatti.

## Naming

`method_shouldExpectedBehavior_whenCondition` — es. `onEvent_shouldPersistCustomFilter_whenSetDateRangeReceived`, `invoke_shouldReturnFailure_whenRepositoryThrows`, `formatCurrency_shouldReturnCompactValue_whenAmountIsLarge`.
Legacy: 7 file usano ancora i backtick; converti solo se stai già modificando quel file, mantenendo lo scopo del test.

## File di riferimento reali

- `androidApp/src/test/kotlin/com/antcashmanager/android/BaseUnitTest.kt`, `BaseComposeUnitTest.kt`
- `androidApp/src/test/kotlin/com/antcashmanager/android/ui/home/HomeViewModelMockkTest.kt`
- `androidApp/src/test/kotlin/com/antcashmanager/android/ui/settings/SettingsViewModelMockkTest.kt`
- `androidApp/src/test/kotlin/com/antcashmanager/android/ui/components/button/VisibilityToggleButtonTest.kt`
- `shared/src/commonTest/kotlin/com/antcashmanager/testutil/{BaseUseCaseTest,TestDataBuilder,FakeTransactionRepository}.kt`
- `shared/src/androidHostTest/kotlin/com/antcashmanager/data/repository/TransactionRepositoryImplTest.kt`
- `androidApp/src/test/kotlin/com/antcashmanager/android/util/CurrencyFormatterTest.kt`

## Comandi

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
./gradlew :androidApp:testFullDebugUnitTest --tests "*SettingsViewModel*" 2>&1 | tail -30
./gradlew :shared:testAndroidHostTest --tests "*InsertTransactionUseCase*" 2>&1 | tail -30
```

## Checklist

- [ ] Source set e base class corretti; nessun boilerplate dispatcher duplicato
- [ ] Solo MockK (+ Fake da `testutil/`)
- [ ] Nome `method_shouldX_whenY`, niente backtick, scopo del test preservato
- [ ] ViewModel testato via `onEvent`; UseCase via `invoke` con `Result`
- [ ] Happy path + failure (tipo eccezione) + cancellazione se applicabile
- [ ] Import puliti, package corretto
