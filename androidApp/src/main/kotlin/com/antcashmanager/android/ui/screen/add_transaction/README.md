# Add Transaction Multi-Step Screen

## Overview
This package contains a complete, production-ready implementation of a multi-step transaction creation flow using Jetpack Compose and MVVM architecture.

## Files

### AddTransactionState.kt
State management for the multi-step wizard.

**Key Components:**
- `AddTransactionStep` - Enum representing the 4 steps:
  1. CATEGORY_SELECTION
  2. TYPE_SELECTION  
  3. DETAILS
  4. CONFIRMATION
  
- `AddTransactionState` - Data class holding the current UI state
- `AddTransactionEvent` - All possible user interactions

### AddTransactionViewModel.kt
Business logic layer managing the transaction creation flow.

**Key Features:**
- Event-driven architecture (1 entry point: `onEvent()`)
- Progressive validation (validates before allowing step transition)
- Integrated with UseCases for data persistence
- Error handling with user feedback
- Automatic state reset after successful submission

**Public Methods:**
- `onEvent(event: AddTransactionEvent)` - Main event handler
- `reset()` - Reset to initial state

### AddTransactionScreen.kt
Composable UI layer with all step implementations.

**Composables:**
- `AddTransactionScreen()` - Main screen connector (receives repos)
- `AddTransactionContent()` - Navigation dispatcher
- `CategorySelectionStep()` - Step 1
- `TypeSelectionStep()` - Step 2
- `DetailsStep()` - Step 3
- `ConfirmationStep()` - Step 4

**Plus supporting composables:**
- `CategoryCard` - Individual category selector
- `TypeRadioButton` - Individual type selector
- `ConfirmationField` - Riepilogo field display

## Usage

### In NavGraph
```kotlin
composable("add_transaction") {
    AddTransactionScreen(
        transactionRepository = transactionRepository,
        categoryRepository = categoryRepository,
        onNavigateBack = { navController.popBackStack() },
        onTransactionAdded = { navController.popBackStack() },
    )
}
```

### Navigation Flow
Triggered from `TransactionsScreen` FAB:
```kotlin
FloatingActionButton(
    onClick = { navController?.navigate("add_transaction") }
)
```

## Data Flow

```
UI Events (User clicks)
    ↓
onEvent() in ViewModel
    ↓
Update internal state
    ↓
Emit new SettingsState via Flow
    ↓
Recompose UI with new state
    ↓
Show appropriate step based on currentStep
```

## Step Details

### 1️⃣ Category Selection
- 2×2 grid layout
- Interactive cards with emoji icons
- Disabled "Next" button until category selected
- Shows category name below icon

### 2️⃣ Type Selection  
- Radio button options (Income/Expense)
- Shows previously selected category
- Disabled "Next" button until type selected
- Back button to revise category

### 3️⃣ Details
- Text input fields for core transaction data
- DatePickerDialog for date selection
- Shows breadcrumb (Category | Type)
- Disabled "Next" until title and amount filled
- Numeric validation for amount field

### 4️⃣ Confirmation
- Read-only summary of all entered data
- Only displays non-empty fields
- Back button to revise any field
- Save button triggers database insert
- Auto-resets state after successful save

## Validation Rules

Each step has specific validation requirements:

| Step | Validation | Error Handling |
|------|-----------|-----------------|
| Category | Category != null | Button disabled |
| Type | Type != null | Button disabled |
| Details | Title.notBlank() && Amount.notBlank() && Amount > 0 | Button disabled |
| Confirmation | All previous valid | Popup error if submit fails |

## Architecture Benefits

✅ **Separation of Concerns** - UI, Logic, State clearly separated
✅ **Testability** - ViewModel logic easily unit testable
✅ **Reusability** - ViewModel can be used in different UIs
✅ **Maintainability** - Changes localized to specific files
✅ **Scalability** - Easy to add new steps or fields
✅ **Performance** - Efficient state management with Flow
✅ **UX** - Guided process reduces user errors

## Extension Points

### Add a New Field
1. Add to `AddTransactionState.data class`
2. Add `UpdateField(value)` event to `AddTransactionEvent`
3. Add handler in `onEvent()` of ViewModel
4. Add UI input in appropriate step Composable
5. Update validation if needed

### Add a New Step
1. Add entry to `AddTransactionStep` enum
2. Add new step Composable with events
3. Add case to `AddTransactionContent()` when expression
4. Update validation logic
5. Update confirmation display if needed

## Testing Strategy

### Unit Tests (ViewModel)
```kotlin
@Test
fun testProgressThroughSteps() { ... }

@Test  
fun testValidationRules() { ... }

@Test
fun testDataPersistence() { ... }
```

### UI Tests (Compose)
```kotlin
@Test
fun testCategorySelection() { ... }

@Test
fun testTypeSelection() { ... }
```

## Future Enhancements

🔄 **State Persistence** - Save incomplete forms
🔄 **Recurring Transactions** - Full implementation  
🔄 **Tags** - Implement tag management
🔄 **Animations** - Add step transition animations
🔄 **Voice Input** - Voice-to-text for fields
🔄 **Camera** - Receipt photo attachment
🔄 **Undo/Redo** - Field history

## Performance Considerations

- Uses private Composables to prevent unnecessary recomposition
- StateFlow ensures updates only when state actually changes
- Lazy grid for categories prevents rendering all at once
- Proper use of coroutineScope for async operations

## Accessibility

All Composables include:
- ✅ ContentDescription for Icons
- ✅ Proper semantic grouping
- ✅ High contrast backgrounds for selection states
- ✅ Appropriate text sizing
- ✅ Touch target size >= 48dp

