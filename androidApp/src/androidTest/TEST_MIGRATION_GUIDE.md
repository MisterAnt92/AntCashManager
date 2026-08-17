# Compose Test API v2 Migration Guide

**Date**: 2026-08-17  
**Status**: Migration Complete  
**Target**: Eliminate deprecation warnings from v1 `createComposeRule`

---

## Summary of Changes

This guide documents the migration from deprecated `androidx.compose.ui.test.junit4.createComposeRule` to `androidx.compose.ui.test.junit4.v2.createComposeRule` across all instrumentation tests.

### Files Updated

1. **AppExitBehaviorTest.kt**
   - Updated import to use v2 API
   - All `waitForIdle()` calls already present

2. **AppExitConfirmationDialogTest.kt**
   - Updated import to use v2 API
   - Added explicit `waitForIdle()` after `performClick()` calls
   - Synchronized with v2 dispatcher behavior

---

## Key Differences: v1 vs v2 API

### UnconfinedTestDispatcher (v1 - Deprecated)
- Executes tasks **immediately** and **synchronously**
- No queuing of coroutines
- Simpler test code but less realistic behavior
- **Status**: Deprecated as of Compose 1.6+

### StandardTestDispatcher (v2 - Current)
- Executes tasks in a **queue** like normal coroutines
- More realistic behavior matching production code
- Requires explicit synchronization points
- **Status**: Recommended for all new tests

---

## Changes Made

### 1. Import Updates

**Before**:
```kotlin
import androidx.compose.ui.test.junit4.createComposeRule
```

**After**:
```kotlin
import androidx.compose.ui.test.junit4.v2.createComposeRule
```

**Files Changed**:
- `androidApp/src/androidTest/kotlin/com/antcashmanager/android/ui/AppExitBehaviorTest.kt`
- `androidApp/src/androidTest/kotlin/com/antcashmanager/android/ui/components/dialog/AppExitConfirmationDialogTest.kt`

### 2. Synchronization Updates

**AppExitConfirmationDialogTest.kt** - Added explicit synchronization:

```kotlin
// AFTER performClick(), add waitForIdle() for v2 API
composeTestRule.onNodeWithText(...).performClick()
composeTestRule.waitForIdle()  // ← Synchronize with StandardTestDispatcher
```

**Tests Updated**:
- `confirmButtonCallsOnConfirmExit()`
- `dismissButtonCallsOnDismiss()`
- `onlyConfirmButtonTerminatesApp()`

### 3. AppExitBehaviorTest.kt

All tests already had proper `waitForIdle()` calls, so only import was updated.

---

## Migration Checklist

### ✅ Completed

- [x] Updated all imports to v2 API
- [x] Added explicit `waitForIdle()` after UI interactions
- [x] Verified import paths are correct
- [x] Removed redundant skip conditions
- [x] Fixed Samsung device assumption bug
- [x] Improved test robustness
- [x] Added comments explaining v2 API requirements

### ✅ Bug Fixes Included

1. **Samsung Device Test** (AppExitBehaviorTest.kt:309)
   - **Before**: `Assume.assumeTrue("Test is for Samsung devices only", false)` 
   - **After**: `Assume.assumeTrue("Test is for Samsung devices only", isSamsungDevice)`
   - **Impact**: Test now correctly skips only on non-Samsung devices

2. **Multiple Cycles Test** (AppExitBehaviorTest.kt:344-370)
   - **Before**: Called `setContent()` after `activity.finish()` (invalid)
   - **After**: Redesigned as dismiss/confirm cycle test without finish
   - **Impact**: Test now runs correctly without crashes

3. **SDK Check Redundancy** (AppExitBehaviorTest.kt:379-413)
   - **Before**: Duplicate skip checks for SDK level
   - **After**: Single clean check using `Assume.assumeTrue()`
   - **Impact**: Cleaner, more maintainable code

4. **Dialog Lifecycle Test** (AppExitBehaviorTest.kt:421-479)
   - **Before**: Tried to call `setContent()` after `activity.finish()`
   - **After**: Redesigned to test callback sequence without finish
   - **Impact**: Test now verifies proper callback ordering

5. **Activity Launch Mode Test** (AppExitBehaviorTest.kt:487-518)
   - **Before**: Fragile `simpleName` check
   - **After**: Robust existence and state verification
   - **Impact**: Less brittle test

---

## Testing the Migration

### Compile & Run

```bash
# Compile tests
./gradlew androidApp:compileAndroidTestKotlin

# Run tests on device/emulator
./gradlew androidApp:connectedAndroidTest

# Run specific test class
./gradlew androidApp:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.antcashmanager.android.ui.AppExitBehaviorTest
```

### Expected Results

✅ **No Deprecation Warnings** for `createComposeRule`  
✅ **All Tests Pass** on API 26+ devices  
✅ **StandardTestDispatcher** behavior matches expectations

---

## Common v2 API Patterns

### Pattern 1: Synchronize After UI Interaction

```kotlin
// Click button
composeTestRule.onNodeWithText("Click Me").performClick()

// Synchronize with StandardTestDispatcher
composeTestRule.waitForIdle()

// Now verify results
assert(callbackInvoked)
```

### Pattern 2: Multiple Setups

```kotlin
// First setup
composeTestRule.setContent { ... }
composeTestRule.waitForIdle()

// Interaction
composeTestRule.onNodeWithText(...).performClick()
composeTestRule.waitForIdle()

// Verify
assert(condition)
```

### Pattern 3: State Changes

```kotlin
// Verify initial state
composeTestRule.waitForIdle()
assert(initialCondition)

// Trigger change
composeTestRule.onNodeWithText(...).performClick()
composeTestRule.waitForIdle()

// Verify new state
assert(newCondition)
```

---

## Migration Reference

### When to Add `waitForIdle()`

✅ **Always add after**:
- `performClick()`
- `performScrollTo()`
- `performTextInput()`
- `performKeyInput()`
- Any action that triggers recomposition

✅ **Consider adding after**:
- `setContent()` (especially for complex hierarchies)
- Large state updates
- Async operations

❌ **Not needed after**:
- `assertIsDisplayed()`
- `assert()` checks
- Simple reads like `onNode...()`

---

## Documentation & Resources

### Android Compose Testing Docs
- [Compose Testing API Documentation](https://developer.android.com/jetpack/compose/testing)
- [Testing Cheat Sheet](https://developer.android.com/codelabs/jetpack-compose-testing)

### Dispatcher Behavior
- `StandardTestDispatcher` queues tasks like real coroutines
- `UnconfinedTestDispatcher` (deprecated) executed immediately
- v2 API is more realistic and better for real-world scenarios

### Troubleshooting

**Issue**: "Node not found" errors after actions
- **Cause**: Missing `waitForIdle()` after `performClick()`
- **Fix**: Add `composeTestRule.waitForIdle()` after UI interactions

**Issue**: Timeouts in tests
- **Cause**: Blocking operations in callbacks
- **Fix**: Verify callbacks are non-blocking

**Issue**: State not updated after click
- **Cause**: v2 API queues tasks, need synchronization
- **Fix**: Add `waitForIdle()` to ensure all queued tasks complete

---

## Future Maintenance

### New Tests
When writing new tests:
1. Always use `androidx.compose.ui.test.junit4.v2.createComposeRule`
2. Add `waitForIdle()` after `performClick()` and similar actions
3. Add comments explaining v2 API synchronization

### Test Updates
When modifying existing tests:
1. Ensure imports use v2 API
2. Add `waitForIdle()` after UI interactions
3. Document any dispatcher-specific behavior

---

## Status

✅ **Migration Complete**  
✅ **All Deprecation Warnings Eliminated**  
✅ **Tests Updated for v2 API Behavior**  
✅ **Bug Fixes Included**  

Ready for production testing on all API levels (26+).

---

**Document Version**: 1.0  
**Last Updated**: 2026-08-17  
**Status**: Complete and Documented
