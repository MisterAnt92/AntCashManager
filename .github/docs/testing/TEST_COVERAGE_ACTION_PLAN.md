# Test Coverage Increase – Action Plan & Progress

**Date**: 2026-08-11  
**Current Test Count**: 453 (from 444 baseline)  
**Target**: Reach 60% androidApp, 75% shared:domain, 70% shared:data  

---

## ✅ What Works (Verified Pattern)

### HomeViewModelComprehensiveTest
**File**: `androidApp/src/test/kotlin/.../home/HomeViewModelComprehensiveTest.kt`  
**Tests**: 9 edge-case tests  
**Status**: ✅ Compiles & Runs

**Test Pattern that Works**:
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelComprehensiveTest : BaseUnitTest() {
    private lateinit var fakeRepo: FakeTransactionRepository
    private lateinit var fakeCategoryRepo: FakeCategoryRepository
    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        fakeRepo = FakeTransactionRepository()
        fakeCategoryRepo = FakeCategoryRepository()
        fakeSettingsRepository = FakeSettingsRepository()
        viewModel = HomeViewModel(
            transactionRepository = fakeRepo,
            settingsRepository = fakeSettingsRepository,
            categoryRepository = fakeCategoryRepo,
            dispatcher = testDispatcher,
            searchDebounceMs = 0L,  // ← Critical for testing!
        )
    }

    @After
    fun tearDown() {
        if (::viewModel.isInitialized) {
            viewModel.viewModelScope.cancel()
        }
    }

    @Test
    fun transactions_shouldBeEmpty_whenRepositoryIsEmpty() = runViewModelTest {
        val collectJob = launch { viewModel.transactions.collect {} }
        advanceUntilIdle()

        assertTrue(viewModel.transactions.value.isEmpty())
        collectJob.cancel()
    }
}
```

**Key Points**:
1. ✅ Extend `BaseUnitTest` for test dispatcher setup
2. ✅ Use `Fake*Repository` not `mockk`
3. ✅ Use `org.junit.Test`, not `kotlin.test.Test`
4. ✅ Collect flows with `launch { viewModel.state.collect {} }`
5. ✅ Always cancel jobs in `@After` tearDown
6. ✅ Use `advanceUntilIdle()` to complete async operations
7. ✅ Use `runViewModelTest` lambda for coroutine scope

---

## 📊 Coverage Progress

| Module | Target | Current | Status |
|--------|--------|---------|--------|
| **androidApp** | 60% | ~23% → 25%* | 🔄 In Progress |
| **shared:domain** | 75% | ~39% → 42%* | 🔄 In Progress |
| **shared:data** | 70% | ~15% → 18%* | 🔄 In Progress |

*Estimates based on 9 new test cases

---

## 🎯 High-Impact Test Files to Create Next

### 1. **ChartsViewModelComprehensiveTest** (HIGH PRIORITY)
**Estimated Coverage Impact**: +5-8%  
**Complexity**: Medium

**What to test**:
- Empty data handling
- Single data point
- Multiple data points
- Large dataset (100+ transactions)
- Mixed income/expense aggregation
- Date range filtering

**Pattern**:
```kotlin
class ChartsViewModelComprehensiveTest : BaseUnitTest() {
    // Similar setup to HomeViewModelComprehensiveTest
    // Use FakeTransactionRepository for test data
    
    @Test
    fun chartData_shouldBeEmpty_whenNoTransactions() = runViewModelTest {
        // Setup: empty repo
        // Act: load chart data
        // Assert: empty chart
    }
}
```

### 2. **AddTransactionViewModelComprehensiveTest** (HIGH PRIORITY)
**Estimated Coverage Impact**: +3-5%  
**Complexity**: Medium

**What to test**:
- New transaction creation
- Edit existing transaction
- Validation errors
- Amount limits
- Category selection

### 3. **ReceiptScanViewModelComprehensiveTest** (MEDIUM PRIORITY)
**Estimated Coverage Impact**: +2-4%  
**Complexity**: Medium

**What to test**:
- Image selection
- OCR processing
- Transaction creation from receipt
- Error handling

### 4. **Domain UseCase Tests** (MEDIUM PRIORITY)
**Estimated Coverage Impact**: +10-15%  
**Complexity**: Low

**Files to create**:
- `FilterTransactionsUseCaseTest.kt` (11 tests)
- `GetTransactionSuggestionsUseCaseTest.kt` (12 tests)
- `GetCategoriesUseCaseTest.kt` (8 tests)

**Pattern**: Already verified in `FilterTransactionsUseCaseTest.kt` (shared module)

### 5. **Repository Edge Case Tests** (MEDIUM PRIORITY)
**Estimated Coverage Impact**: +8-12%  
**Complexity**: Low-Medium

**Files to create**:
- `TransactionRepositoryEdgeCaseTest.kt` (20 tests)
- `CategoryRepositoryEdgeCaseTest.kt` (15 tests)

**Pattern**: Already verified in `TransactionRepositoryEdgeCaseTest.kt` (shared module)

---

## 🔧 How to Create New Test Files

### Step 1: Choose a ViewModel/UseCase
Start with high-impact targets (ChartsViewModel, AddTransactionViewModel)

### Step 2: Create Test File
```bash
mkdir -p androidApp/src/test/kotlin/com/antcashmanager/android/ui/screen/[feature]/
touch androidApp/src/test/kotlin/com/antcashmanager/android/ui/screen/[feature]/[Feature]ViewModelComprehensiveTest.kt
```

### Step 3: Copy Template
```kotlin
package com.antcashmanager.android.ui.screen.[feature]

import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.testutil.Fake*Repository  // ← Adjust
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class [Feature]ViewModelComprehensiveTest : BaseUnitTest() {
    private lateinit var fakeRepo: Fake[Name]Repository
    private lateinit var viewModel: [Feature]ViewModel

    @Before
    fun setup() {
        fakeRepo = Fake[Name]Repository()
        viewModel = [Feature]ViewModel(
            repository = fakeRepo,
            // ... other dependencies
            dispatcher = testDispatcher,
        )
    }

    @After
    fun tearDown() {
        if (::viewModel.isInitialized) {
            viewModel.viewModelScope.cancel()
        }
    }

    @Test
    fun feature_shouldDoX_whenConditionY() = runViewModelTest {
        // Arrange: setup fake data
        fakeRepo.data.value = listOf(...)
        
        // Act: trigger action
        val collectJob = launch { viewModel.state.collect {} }
        advanceUntilIdle()
        
        // Assert: verify result
        assertEquals(expected, viewModel.state.value)
        collectJob.cancel()
    }
}
```

### Step 4: Add 8-12 Tests
Cover:
- Empty/null cases
- Single item
- Multiple items
- Large dataset (100+)
- Edge cases (unicode, special chars)
- Boundary values
- Error conditions

### Step 5: Compile & Run
```bash
./gradlew :androidApp:testDebugUnitTest
```

Expected: +1 test per file function

---

## 📈 Estimated Timeline to Reach Targets

| Phase | Files | Tests | Effort | Coverage Δ |
|-------|-------|-------|--------|-----------|
| **Now** | 1 | 9 | Done | +1-2% |
| **Phase 2** | 3 VM tests | 30-36 | 2-3h | +8-12% |
| **Phase 3** | 5 UseCase tests | 55+ | 2-3h | +10-15% |
| **Phase 4** | 2 Repository tests | 35+ | 1-2h | +8-12% |
| **Phase 5** | Component tests | 20+ | 2-3h | +5-10% |

**Total**: ~10-15 hours to reach **60%+ coverage**

---

## ✨ Key Success Factors

1. **Use Fake Repositories** - They're already implemented and work perfectly
2. **Follow the Pattern** - Copy HomeViewModelComprehensiveTest structure
3. **Test Edge Cases** - Empty lists, unicode, large datasets, boundary values
4. **Proper Cleanup** - Always cancel jobs in @After
5. **Use runViewModelTest** - It handles dispatcher setup automatically
6. **No mockk for ViewModels** - Fake repositories are better for ViewModel tests

---

## 🚀 Next Immediate Actions

1. **Create ChartsViewModelComprehensiveTest** (30-45 min, +5% coverage)
2. **Create AddTransactionViewModelComprehensiveTest** (45 min, +3% coverage)
3. **Create ReceiptScanViewModelComprehensiveTest** (45 min, +2% coverage)
4. **Create Domain UseCase tests** (1-2h, +10% coverage)

**Total: 4-5 hours → +20% coverage increase**

---

## 📝 Current Status

✅ **Pattern Verified**: HomeViewModelComprehensiveTest (9 tests, compiles & runs)  
✅ **Test Count**: 453 (from 444)  
✅ **Framework Working**: BaseUnitTest, Fake repositories, runViewModelTest  
✅ **Next Target**: 60% androidApp (currently ~25%)  

---

**Remember**: The pattern works! Just copy, customize, and add 8-12 edge-case tests per ViewModel. Each test file adds 1-2% coverage.
