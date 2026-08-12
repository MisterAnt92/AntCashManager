# Test Coverage Expansion Strategy – AntCashManager

## Current State Analysis

### Coverage Baseline (as of 2026-08-11)
- **androidApp Unit Tests**: ~42 test files, ~500 test cases
- **shared CommonTest**: ~12 test files, ~150 test cases  
- **Shared AndroidHostTest**: ~5 test files, ~50 test cases
- **Overall Coverage**: ~23-30% (androidApp), ~39% (shared)
- **Target Coverage**: 70% for production-ready code

### Key Gaps Identified

#### 1. **ViewModel Coverage Gaps** (HIGH IMPACT)
- HomeViewModel: Partially tested
- TransactionsViewModel: Basic tests only
- SettingsDataViewModel: Basic tests only
- ReceiptScanViewModel: Missing edge cases
- ChartsViewModel: Missing filtering/sorting tests

#### 2. **UseCase Coverage Gaps** (MEDIUM IMPACT)
- GetTransactionsUseCase: Basic tests
- FilterTransactionsUseCase: Missing
- GetTransactionSuggestionsUseCase: Missing
- SetHomeDateFilterStateUseCase: Missing
- ExportDataUseCase: Missing
- ImportDataUseCase: Missing

#### 3. **Repository Coverage Gaps** (MEDIUM IMPACT)  
- TransactionRepositoryImpl: ~20% coverage (needs error scenarios)
- CategoryRepositoryImpl: ~10% coverage
- SettingsRepositoryImpl: ~15% coverage
- Backup/Restore operations: Missing

#### 4. **UI Component Coverage Gaps** (LOW IMPACT, but MANY FILES)
- Cards: 0% coverage (AppCategoryCard, HomeTopCard, TransactionCard, etc.)
- Dialogs: 0% coverage
- Button Components: 0% coverage
- Layout Components: 0% coverage

#### 5. **Domain Model Coverage Gaps** (LOW IMPACT)
- Transaction validation logic
- Category model equality
- Filter model logic
- Enum values testing

---

## Phased Implementation Plan

### Phase 1: Fix Critical Compilation Issues (1-2 hours)

**Objective**: Get test suite compiling completely

**Tasks**:
1. ✅ Fix SavedDateFilterTest (timestamp conversions)
2. ✅ Fix AppErrorTest (sealed class exceptions)
3. ✅ Fix CategoryTest (builder pattern variable shadowing)
4. ✅ Fix TransactionTest (timestamp variable shadowing)
5. ✅ Rename AppThemeTest → AppThemeEnumTest (avoid redeclaration)
6. ✅ Rename TransactionTest → TransactionDomainModelTest (avoid redeclaration)
7. 🔄 Fix SettingsRepositoryImplTest (Float type mismatch)
8. 🔄 Fix TransactionRepositoryImplTest (generic type inference)

**Current Status**: Steps 1-6 DONE, steps 7-8 IN PROGRESS

---

### Phase 2: Expand ViewModel Test Coverage (2-3 hours)

**Objective**: Achieve 60%+ coverage for all ViewModels

**High-Priority ViewModels**:

#### 2.1 HomeViewModel Extended Tests
```
Tests to Add:
├── Empty transaction list handling ✅ 
├── Balance calculation (income + expense) ✅
├── Date range filtering ✅
├── Suggestions generation (titles, payees, notes, locations, tags) ✅
├── Special character handling ✅
├── Large amount transactions ✅
├── Zero amount transactions ✅
├── Error scenarios (repository failures)
└── Multiple filter combinations
```

#### 2.2 TransactionsViewModel Extended Tests
```
Tests to Add:
├── Sorting by date, amount, title
├── Filtering by category and type
├── Search functionality (partial matches)
├── Unicode character support
├── Large dataset handling (1000+ items)
├── Empty result sets
└── Error handling
```

#### 2.3 SettingsDataViewModel Extended Tests
```
Tests to Add:
├── Data export (transactions + categories)
├── Data import from backup
├── Clear all data scenarios
├── Large dataset processing (10000+ items)
├── Decimal amount precision
├── Special character preservation
└── Error handling during operations
```

**Time Investment**: 
- HomeViewModel: 45 minutes (8-10 new tests)
- TransactionsViewModel: 45 minutes (10-12 new tests)
- SettingsDataViewModel: 45 minutes (10-12 new tests)

**Expected Coverage Increase**: +15-20%

---

### Phase 3: Expand UseCase Test Coverage (2-3 hours)

**Objective**: Test all critical domain logic

**UseCase Tests to Add**:

1. **FilterTransactionsUseCase**
   - Filter by category
   - Filter by date range
   - Filter by type (income/expense)
   - Combined filters
   - Empty result handling

2. **GetTransactionSuggestionsUseCase**
   - Generate title suggestions
   - Generate payee suggestions
   - Deduplicate suggestions
   - Handle empty history
   - Handle special characters

3. **SetHomeDateFilterStateUseCase**
   - Save custom date range
   - Save preset filter
   - Persist to settings
   - Error handling

4. **ExportDataUseCase** (NEW)
   - Export all transactions
   - Export all categories
   - Export in JSON format
   - Handle large datasets
   - Error scenarios

5. **ImportDataUseCase** (NEW)
   - Import from JSON
   - Validate import data
   - Handle duplicate detection
   - Merge vs replace strategies
   - Error recovery

**Time Investment**: 2-3 hours (3-4 tests per UseCase)

**Expected Coverage Increase**: +10-15%

---

### Phase 4: Repository & Data Layer Testing (2-3 hours)

**Objective**: Test data persistence and query logic

**Tests to Add**:

1. **TransactionRepositoryImpl** (+10% coverage)
   - getSuggestions() with various inputs
   - getTransactionsByDateRange()
   - Filter edge cases
   - Error scenarios
   - Database transaction handling

2. **CategoryRepositoryImpl** (NEW)
   - CRUD operations
   - Category ordering
   - Hidden category filtering
   - Error handling

3. **SettingsRepositoryImpl** (+5% coverage)
   - DataStore operations
   - Default value handling
   - Type conversions
   - Error recovery

**Time Investment**: 2-3 hours

**Expected Coverage Increase**: +8-12%

---

### Phase 5: Component & Integration Tests (3-4 hours)

**Objective**: Test UI components and integration scenarios

**Component Tests** (Lower Priority):
- Card components (DisplayName, Compose preview)
- Button components (reorder, visibility toggle)
- Dialog components (confirm, input)
- Layout components (spacing, arrangement)

**Integration Tests**:
- End-to-end transaction creation → persistence
- Filter → display cycle
- Backup → restore cycle
- Search → filtering → display

**Time Investment**: 3-4 hours (20+ component + integration tests)

**Expected Coverage Increase**: +10-20%

---

## Success Metrics

| Metric | Current | Target | Timeline |
|--------|---------|--------|----------|
| Unit Test Count | 550+ | 750+ | Phase 2-3 |
| Coverage (androidApp) | 23-30% | 60-70% | Phase 2-3 |
| Coverage (shared) | 39% | 70%+ | Phase 2-3 |
| ViewModel Coverage | 40% | 70% | Phase 2 (2-3 hrs) |
| UseCase Coverage | 30% | 70% | Phase 3 (2-3 hrs) |
| Repository Coverage | 15% | 60% | Phase 4 (2-3 hrs) |
| Build Time (tests only) | ~30s | ~45s | N/A |

---

## Quick Win Opportunities (1-2 hours max)

These tasks provide maximum coverage increase with minimal time investment:

1. **ViewModel Error Scenarios** (30 mins)
   - Test failure paths (repository returns error)
   - Test retry logic
   - Test user feedback (error messages)
   
2. **UseCase Edge Cases** (30 mins)
   - Empty input handling
   - Null/default values
   - Large datasets
   - Special characters in data

3. **Repository Mock Variations** (30 mins)
   - Slow database responses
   - Concurrent access
   - Data corruption recovery

---

## Execution Checklist

### Phase 1 - Critical Fixes (STATUS: IN PROGRESS)
- [x] SavedDateFilterTest - timestamp conversion fix
- [x] AppErrorTest - sealed class exception handling
- [x] CategoryTest - variable shadowing fix
- [x] TransactionTest - variable shadowing fix  
- [x] AppThemeTest → AppThemeEnumTest rename
- [x] TransactionTest → TransactionDomainModelTest rename
- [ ] SettingsRepositoryImplTest - Float type mismatch
- [ ] TransactionRepositoryImplTest - generic type fix

### Phase 2 - ViewModel Tests
- [ ] HomeViewModelExtendedTest (error scenarios, filtering)
- [ ] TransactionsViewModelExtendedTest (sorting, searching)
- [ ] SettingsDataViewModelExtendedTest (export/import)
- [ ] ReceiptScanViewModelExtendedTest (OCR edge cases)
- [ ] ChartsViewModelExtendedTest (chart operations)

### Phase 3 - UseCase Tests  
- [ ] FilterTransactionsUseCaseTest
- [ ] GetTransactionSuggestionsUseCaseTest
- [ ] SetHomeDateFilterStateUseCaseTest
- [ ] ExportDataUseCaseTest (NEW)
- [ ] ImportDataUseCaseTest (NEW)

### Phase 4 - Repository Tests
- [ ] TransactionRepositoryImplTest - expand
- [ ] CategoryRepositoryImplTest (NEW)
- [ ] SettingsRepositoryImplTest - expand

### Phase 5 - Integration Tests
- [ ] Transaction creation → persistence flow
- [ ] Backup → restore cycle
- [ ] Filter → display → sort flow

---

## Notes & Considerations

1. **Test Data**: Use existing TestDataBuilder pattern for consistent test data generation
2. **Mocking**: Use MockK exclusively (no Mockito, no Roboelectric in unit tests)
3. **Async**: Use `runViewModelTest` and `advanceUntilIdle()` for coroutine testing
4. **Performance**: Aim for <1s per test, <30s total suite
5. **Coverage Tools**: Use Jacoco for automated coverage reporting
6. **CI/CD**: Add coverage thresholds to PR checks (min 70%)

---

## References

- **Testing Stack Standard (KMP)**: AGENTS.md - Testing section
- **MockK Documentation**: https://mockk.io/
- **Compose Testing**: https://developer.android.com/jetpack/compose/testing
- **Jacoco**: https://www.eclemma.org/jacoco/

---

**Last Updated**: 2026-08-11  
**Author**: AI Assistant  
**Status**: Phase 1 IN PROGRESS, Phases 2-5 PLANNED
