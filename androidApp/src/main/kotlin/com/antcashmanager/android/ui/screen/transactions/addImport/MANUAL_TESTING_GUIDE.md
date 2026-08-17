# Manual Testing Guide - Meal Vouchers Feature (v1.7.2)

**Feature**: Improved meal vouchers transaction input with optional "Differenza Pagata" (difference paid)  
**Date**: 2026-08-15  
**Version**: v1.7.2  
**Status**: Ready for Manual Testing

---

## 📱 Test Environment Setup

### Required Devices/Emulators

- ✅ **Android 8.0 (API 26)** — Minimum SDK
- ✅ **Android 9.0 (API 28)** — Lower end
- ✅ **Android 12.0 (API 31)** — Mid-range
- ✅ **Android 14.0+ (API 34+)** — Target SDK

### Test Data Prerequisites

Before starting tests, ensure:
1. ✅ App built in DEBUG mode
2. ✅ Database initialized with test categories (including "Buoni Pasto")
3. ✅ All 13 language strings loaded (verify in Settings → Language)
4. ✅ No pending background tasks

### Language Testing

All tests should be executed in:
- **Primary**: Italian (IT) — "Differenza pagata"
- **Recommended**: English (EN) — "Difference paid (cash/cards/other)"
- **Sample**: 1 additional language (FR, DE, ES, etc.)

---

## 🧪 Test Scenarios

## SCENARIO 1: Create New Expense with Meal Vouchers Only

**Objective**: Verify that creating an EXPENSE transaction with ONLY meal vouchers (no additional payment) works correctly.

**Steps**:

1. Open app → Tap "Aggiungi Transazione" (Add Transaction)
2. Select Category: "Buoni Pasto" (Meal Vouchers)
3. Select Type: **USCITE** (Expenses)
4. Select Date: Today
5. Select Payment Type: **Buoni Pasto** (auto-selected after category)
6. Enter Title: "Pranzo aziendale" (Company lunch)
7. **Verify**: 
   - ✅ "Numero buoni" field visible
   - ✅ "Valore unitario" displays: 5.29€
   - ❌ "Differenza pagata" field **NOT visible** ← Check!
8. Enter Numero buoni: "5"
9. **Verify**:
   - ✅ Subtotale calculates: 5 × 5.29€ = 26.45€
   - ✅ Total Amount field shows: 26.45€
10. Leave "Differenza pagata" empty (not shown anyway)
11. Tap "Salva" (Save)
12. **Verify Success**:
    - ✅ Transaction saved with amount = 26.45€
    - ✅ mealVoucherCount = 5
    - ✅ No error dialogs

**Expected Result**: ✅ Transaction created with correct calculation

---

## SCENARIO 2: Create New Expense with Vouchers + Difference Paid

**Objective**: Verify that EXPENSE transactions with vouchers AND additional payment work correctly.

**Steps**:

1. Open app → Tap "Aggiungi Transazione"
2. Select Category: "Buoni Pasto"
3. Select Type: **USCITE** (Expenses)
4. Select Date: Today
5. Select Payment Type: **Buoni Pasto**
6. Enter Title: "Cena con famiglia" (Dinner with family)
7. **Verify**:
   - ✅ "Numero buoni" field visible
   - ✅ "Valore unitario" displays: 5.29€
   - ✅ **"Differenza pagata" field IS visible** ← Critical!
8. Enter Numero buoni: "5"
9. **Verify Calculation**:
   - ✅ Subtotale: 26.45€
   - ✅ "Differenza pagata" placeholder shows: "0.00"
10. Enter Differenza pagata: "3.55"
11. **Verify Real-Time Calculation**:
    - ✅ Total Amount updates to: 30.00€ (26.45 + 3.55)
12. Tap "Salva"
13. **Verify Success**:
    - ✅ Transaction saved with amount = 30.00€
    - ✅ mealVoucherCount = 5
    - ✅ mealVoucherDifference = 3.55

**Expected Result**: ✅ Automatic total calculation works correctly

**Calculation Verification**:
```
Voucher Count:      5
Voucher Value:      5.29€
Subtotal:           26.45€ (5 × 5.29)

Difference Paid:    3.55€
─────────────────────────────
TOTAL:              30.00€ ✅
```

---

## SCENARIO 3: Create INCOME with Meal Vouchers (No Difference Field)

**Objective**: Verify that INCOME transactions do NOT show the "Differenza pagata" field (because income is not a payment).

**Steps**:

1. Open app → Tap "Aggiungi Transazione"
2. Select Category: "Buoni Pasto"
3. **Select Type: ENTRATE** (Income) ← **Critical difference**
4. Select Date: Today
5. Select Payment Type: **Buoni Pasto**
6. Enter Title: "Bonus buoni pasto da azienda" (Meal vouchers bonus from company)
7. **Verify CRITICAL Behavior**:
   - ✅ "Numero buoni" field visible
   - ✅ "Valore unitario" displays: 5.29€
   - ❌ **"Differenza pagata" field MUST NOT BE VISIBLE** ← Verify!
   - This is the key specification requirement
8. Enter Numero buoni: "10"
9. **Verify Calculation**:
   - ✅ Subtotale: 52.90€ (10 × 5.29)
   - ✅ Total Amount: 52.90€ (no difference added)
10. Try to scroll down to see if there's a hidden "Differenza pagata" field
    - ❌ Should NOT exist at all
11. Tap "Salva"
12. **Verify Success**:
    - ✅ Transaction saved with amount = 52.90€
    - ✅ mealVoucherCount = 10
    - ✅ No error about missing difference

**Expected Result**: ✅ EXPENSE-only constraint works correctly

**Why This Matters**: 
- INCOME (entrate) = receiving vouchers as a bonus
- EXPENSE (uscite) = spending vouchers as payment
- Difference field only makes sense for payments (EXPENSE)

---

## SCENARIO 4: Edit Existing Meal Voucher Transaction

**Objective**: Verify that editing transactions preserves and recalculates amounts correctly.

**Prerequisites**: 
- Existing transaction: 5 vouchers, 3.55€ difference, total 30.00€

**Steps**:

1. Navigate to Home → Find "Cena con famiglia" transaction
2. Tap to view details
3. Tap "Modifica" (Edit)
4. **Verify Loaded Values**:
   - ✅ Numero buoni: "5"
   - ✅ Differenza pagata: "3.55"
   - ✅ Total Amount: "30.00"
5. Change Numero buoni: "5" → "7"
6. **Verify Recalculation**:
   - ✅ Subtotale updates: 26.45€ → 37.03€ (7 × 5.29)
   - ✅ Total Amount updates: 30.00€ → 40.58€ (37.03 + 3.55)
7. Change Differenza pagata: "3.55" → "5.00"
8. **Verify Recalculation**:
   - ✅ Total Amount updates: 40.58€ → 42.03€ (37.03 + 5.00)
9. Tap "Aggiorna" (Update)
10. **Verify Success**:
    - ✅ Transaction updated with amount = 42.03€
    - ✅ mealVoucherCount = 7
    - ✅ mealVoucherDifference = 5.00

**Expected Result**: ✅ Edit and recalculation work correctly

---

## SCENARIO 5: Edit Income Transaction (Verify No Difference Field)

**Objective**: Verify that editing INCOME transactions doesn't show the difference field.

**Prerequisites**:
- Existing INCOME transaction: 10 vouchers, total 52.90€

**Steps**:

1. Navigate to Home → Find "Bonus buoni pasto da azienda" transaction
2. Tap to view details
3. Tap "Modifica" (Edit)
4. **Verify**:
   - ✅ Numero buoni: "10"
   - ❌ Differenza pagata field NOT visible
5. Change Numero buoni: "10" → "15"
6. **Verify Recalculation**:
   - ✅ Total Amount updates: 52.90€ → 79.35€ (15 × 5.29)
7. Tap "Aggiorna"
8. **Verify Success**:
   - ✅ Transaction updated with amount = 79.35€
   - ✅ No unexpected calculation

**Expected Result**: ✅ INCOME edit doesn't show difference field

---

## SCENARIO 6: Validation - Invalid Difference Values

**Objective**: Verify that validation prevents invalid inputs.

**Steps**:

1. Create new EXPENSE + Meal Vouchers transaction
2. Enter Numero buoni: "5"
3. Try entering Differenza pagata: "-1.00" (negative)
4. **Verify**:
   - ❌ Input rejected or error shown
   - ✅ Can't proceed to save
5. Clear and try: "5.999" (more than 2 decimals)
6. **Verify**:
   - ❌ Input normalized or rejected
   - ✅ Can't save with invalid decimal places
7. Try: "abc" (non-numeric)
8. **Verify**:
   - ❌ Keyboard prevents invalid input OR
   - ✅ Field resets to "0.00"
9. Try: "0.00" (valid, zero)
10. **Verify**:
    - ✅ Accepted
    - ✅ Total = Subtotale only
11. Try: "99.99" (valid, large amount)
12. **Verify**:
    - ✅ Accepted
    - ✅ Total calculated correctly

**Expected Result**: ✅ Validation works correctly

---

## SCENARIO 7: Switch Payment Type (Difference Reset)

**Objective**: Verify that changing payment type resets the difference field.

**Steps**:

1. Create new EXPENSE transaction
2. Set Payment Type: **Buoni Pasto**
3. Enter Numero buoni: "5"
4. Enter Differenza pagata: "10.00"
5. **Verify**:
   - ✅ Total: 36.45€ (26.45 + 10.00)
6. Change Payment Type: Buoni Pasto → **Denaro** (Cash)
7. **Verify State Change**:
   - ❌ Meal vouchers section should disappear
   - ✅ Regular "Amount" field should appear
   - ✅ Differenza pagata value is reset
8. Enter Amount: "50.00"
9. **Verify Calculation**:
   - ✅ No voucher logic applied
   - ✅ Total = 50.00€
10. Change Payment Type back: Denaro → **Buoni Pasto**
11. **Verify Reset**:
    - ✅ Numero buoni: empty or "0"
    - ✅ Differenza pagata: "0" (reset)
    - ✅ Total: "0.00"

**Expected Result**: ✅ Reset behavior works correctly

---

## SCENARIO 8: UI Layout Verification (Responsive)

**Objective**: Verify that UI looks good on different screen sizes.

### Test on Phone (Portrait - 360dp width)

**Steps**:

1. Open transaction creation screen
2. Select EXPENSE + Meal Vouchers
3. **Verify UI**:
   - ✅ "Numero buoni" field fully visible
   - ✅ "Differenza pagata" field below with clear spacing
   - ✅ Padding between fields visible (SpacingSize.MD)
   - ✅ Total Amount field below, clearly separated
   - ✅ No text truncation
   - ✅ Keyboard doesn't hide fields
4. Scroll down to see full form
   - ✅ All fields accessible
   - ✅ Save button visible

### Test on Tablet (Landscape - 800dp width)

**Steps**:

1. Open transaction creation screen (landscape mode)
2. Select EXPENSE + Meal Vouchers
3. **Verify UI**:
   - ✅ Fields properly spaced
   - ✅ Card doesn't take excessive width
   - ✅ Layout responsive and not cramped
   - ✅ All fields visible without scrolling (if possible)

### Test on Foldable (Split Screen)

**Steps**:

1. Open transaction creation in split-screen mode
2. Select EXPENSE + Meal Vouchers
3. **Verify**:
   - ✅ UI adapts to available width
   - ✅ No layout breaks

**Expected Result**: ✅ Responsive layout works on all screen sizes

---

## SCENARIO 9: Accessibility Verification

**Objective**: Verify that the feature is accessible.

**Steps**:

1. Enable TalkBack (Settings → Accessibility → TalkBack)
2. Open transaction creation with Meal Vouchers
3. **Verify Screen Reader**:
   - ✅ "Numero buoni" label read correctly
   - ✅ Keyboard type announced ("numeric")
   - ✅ "Differenza pagata" label read (for EXPENSE)
   - ✅ Field type announced ("decimal" or "numeric")
   - ✅ "Total Amount" read as read-only field
   - ✅ Instructions clear for all fields
4. Navigate using accessibility focus
   - ✅ Logical tab order
   - ✅ No skip of fields
5. Enter data using keyboard only (no touch)
   - ✅ Can fill all fields
   - ✅ Can submit form

**Expected Result**: ✅ Accessibility works correctly

---

## SCENARIO 10: Multi-Language Verification

### Italian (IT) - Primary Language

**Steps**:

1. Settings → Language → **Italiano**
2. Create EXPENSE + Meal Vouchers transaction
3. **Verify Strings**:
   - ✅ "Buoni Pasto" (category/payment type)
   - ✅ "Numero buoni pasto" (field label)
   - ✅ "Valore unitario" (unit value display)
   - ✅ **"Differenza pagata (cash/carte/altro)"** ← Key string
   - ✅ "Importo totale buoni pasto" (total field label)
   - ✅ Subtotale correctly formatted with €

### English (EN)

**Steps**:

1. Settings → Language → **English**
2. Create EXPENSE + Meal Vouchers transaction
3. **Verify Strings**:
   - ✅ "Meal Vouchers"
   - ✅ "Number of meal vouchers"
   - ✅ **"Difference paid (cash/cards/other)"** ← Key string
   - ✅ Currency formatting ($ or € depending on locale)

### Sample Language (e.g., French FR)

**Steps**:

1. Settings → Language → **Français**
2. Create EXPENSE + Meal Vouchers transaction
3. **Verify Strings**:
   - ✅ "Chèques-repas"
   - ✅ **"Différence payée (espèces/cartes/autre)"** ← Key string
   - ✅ All labels translated correctly

**Expected Result**: ✅ All languages display correctly

---

## 🔢 Calculation Verification Matrix

| Scenario | Vouchers | Unit Value | Difference | Total | Status |
|----------|----------|------------|------------|-------|--------|
| Only vouchers | 5 | 5.29 | 0.00 | 26.45 | ✅ |
| Vouchers + difference | 5 | 5.29 | 3.55 | 30.00 | ✅ |
| Large difference | 5 | 5.29 | 50.00 | 76.45 | ✅ |
| Empty difference | 5 | 5.29 | (empty) | 26.45 | ✅ |
| Many vouchers | 99 | 5.29 | 1.23 | 524.44 | ✅ |
| Few vouchers | 1 | 5.29 | 0.01 | 5.30 | ✅ |

---

## ❌ Negative Tests (Should Fail/Prevent)

| Test Case | Input | Expected Behavior |
|-----------|-------|-------------------|
| INCOME type + difference field | See difference field | ❌ Field should NOT appear |
| Negative difference | -5.00 | ❌ Reject or show error |
| 3+ decimals | 5.999 | ❌ Normalize to 2 decimals or reject |
| Non-numeric | "abc" | ❌ Reject input |
| Non-numeric with text | "5.5abc" | ❌ Extract only "5.5" OR reject entirely |

---

## 📋 Pre-Commit Checklist

Before considering this feature ready for release:

### Functionality
- [ ] SCENARIO 1: Vouchers only (EXPENSE)
- [ ] SCENARIO 2: Vouchers + difference (EXPENSE)
- [ ] SCENARIO 3: INCOME with vouchers (no difference)
- [ ] SCENARIO 4: Edit existing transaction
- [ ] SCENARIO 5: Edit INCOME (no difference field)
- [ ] SCENARIO 6: Validation of invalid inputs
- [ ] SCENARIO 7: Payment type switching
- [ ] All calculation tests pass

### UI/UX
- [ ] SCENARIO 8: Responsive layout (phone, tablet, foldable)
- [ ] Field spacing is consistent (SpacingSize.MD between fields)
- [ ] Cards render with correct styling
- [ ] No text truncation
- [ ] Keyboard behavior is correct

### Accessibility
- [ ] SCENARIO 9: TalkBack screen reader works
- [ ] Tab order is logical
- [ ] All fields have proper labels
- [ ] Error messages are announced

### Internationalization
- [ ] SCENARIO 10: Italian strings correct
- [ ] English strings correct
- [ ] At least 1 additional language verified
- [ ] All 13 languages have translation

### Data Integrity
- [ ] Transaction saved with correct amount
- [ ] mealVoucherCount persisted correctly
- [ ] mealVoucherDifference persisted (if EXPENSE)
- [ ] Editing preserves history

### Edge Cases
- [ ] Zero vouchers (should not break)
- [ ] Very large difference (e.g., 999.99)
- [ ] Very small difference (e.g., 0.01)
- [ ] Back navigation doesn't save incomplete data
- [ ] Rotation during input preserves state

---

## 🐛 Common Issues to Watch For

| Issue | Symptom | How to Check |
|-------|---------|-------------|
| Calculation wrong | Total doesn't match 26.45 + 3.55 = 30.00 | Compare with calculator |
| Difference shows for INCOME | "Differenza pagata" visible on INCOME trans | This is a CRITICAL bug |
| Difference persists on type change | Changing payment type keeps old difference | Should reset to "0" |
| Negative difference accepted | Can enter "-5.00" in difference field | Should be rejected |
| String not translated | English string shows in Italian | Check all 13 language files |
| UI overflow on small screen | Text truncated or fields overlap | Test on 360dp width device |
| Accessibility broken | TalkBack doesn't read fields | Enable TalkBack and navigate |

---

## ✅ Sign-Off Template

After completing manual testing, use this template:

```
MANUAL TESTING COMPLETION REPORT
═════════════════════════════════════════════════════

Feature: Meal Vouchers with Difference Paid (v1.7.2)
Date: [DATE]
Tester: [NAME]
Device: [MODEL] API [LEVEL]
Language: [IT/EN/FR/etc]

RESULTS SUMMARY
───────────────
Scenarios Passed:  [X]/10
Scenarios Failed:  [0]/10
Issues Found:      [0]

DETAILED RESULTS
────────────────
[ ] Scenario 1: Create EXPENSE with vouchers only
[ ] Scenario 2: Create EXPENSE with vouchers + difference
[ ] Scenario 3: Create INCOME with vouchers (no difference)
[ ] Scenario 4: Edit EXPENSE transaction
[ ] Scenario 5: Edit INCOME transaction
[ ] Scenario 6: Validation of invalid inputs
[ ] Scenario 7: Payment type switching
[ ] Scenario 8: Responsive layout
[ ] Scenario 9: Accessibility (TalkBack)
[ ] Scenario 10: Multi-language

CRITICAL CHECKS
────────────────
[✅] EXPENSE transactions show "Differenza pagata" field
[✅] INCOME transactions DO NOT show "Differenza pagata" field
[✅] Calculation: (vouchers × value) + difference = total
[✅] Difference field only visible for EXPENSE type
[✅] All strings translated to all 13 languages

SIGN-OFF
────────
Testing completed: [ ] YES / [ ] NO
Ready for release: [ ] YES / [ ] NO
Issues to fix:     [ ] NONE / [ ] [LIST]

Tester Signature: _________________ Date: ________
```

---

## 📞 Support & Troubleshooting

### If "Differenza pagata" doesn't appear on EXPENSE:

1. Check that `isExpenseType == true`
2. Check that DetailsMealVoucherSection receives correct parameter
3. Verify DetailsStep.kt line 263: `isExpenseType = state.selectedType == TransactionType.EXPENSE`
4. Rebuild app if not taking effect

### If calculation is wrong:

1. Verify TransactionValidator normalization
2. Check that totalAmount computed property includes difference
3. Test with calculator: (count × value) + difference

### If strings don't translate:

1. Verify string key is "add_transaction_meal_voucher_difference"
2. Check all 13 language files have this key
3. Clear app cache: Settings → Apps → AntCashManager → Clear Cache

### If validation doesn't work:

1. Check KeyboardOptions.keyboardType = KeyboardType.Decimal
2. Verify onValueChange callback is triggered
3. Check TransactionValidator.normalizeMealVoucherDifference() logic

---

## 🎯 Test Execution Order (Recommended)

For most efficient testing, execute scenarios in this order:

1. **Basic Functionality** (Scenarios 1-3) — 15 min
2. **Editing** (Scenarios 4-5) — 10 min
3. **Validation** (Scenario 6) — 5 min
4. **Behavior** (Scenario 7) — 5 min
5. **UI** (Scenario 8) — 10 min
6. **Accessibility** (Scenario 9) — 10 min
7. **Languages** (Scenario 10) — 15 min

**Total Estimated Time**: ~70 minutes per device/language combination

---

**Document Version**: 1.0  
**Last Updated**: 2026-08-15  
**Status**: Ready for Testing  
**Next Review**: After first user feedback
