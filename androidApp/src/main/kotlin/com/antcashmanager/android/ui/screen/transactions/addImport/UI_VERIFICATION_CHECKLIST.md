# UI Verification Checklist - Meal Vouchers Feature

**Feature**: Meal Vouchers with "Differenza Pagata" (Difference Paid)  
**Version**: v1.7.2  
**Date**: 2026-08-15  
**Objective**: Comprehensive visual and interaction verification

---

## 📱 Screen Layout Verification

### When Payment Type = MEAL_VOUCHERS + Type = EXPENSE

#### Expected Visual Structure:

```
┌─────────────────────────────────────────────────┐
│  Aggiungi Transazione              [←] [🗑]     │
├─────────────────────────────────────────────────┤
│                                                 │
│  Categoria    [Buoni Pasto]      [edit]        │
│  Tipo         [USCITE]            [edit]        │
│  Data         [15 Aug 2026]       [edit]        │
│  Pagamento    [Buoni Pasto]       [edit]        │
│                                                 │
├─ Titolo ──────────────────────────────────────┤
│  [_______________________________] Required      │
│                                                 │
├─ Importo ─────────────────────────────────────┤
│  (Hidden - Meal Vouchers form instead)          │
│                                                 │
├─ ┌─ Buoni Pasto (Card) ───────────────────────┐ │
│  │                                             │ │
│  │ Buoni Pasto                                │ │
│  │                                             │ │
│  │ Valore unitario: 5.29€                    │ │
│  │                                             │ │
│  │ Numero buoni pasto:  [____5____]           │ │
│  │                                             │ │
│  │ ─────────────────────────────────────     │ │
│  │ (SpacingSize.MD padding here)              │ │
│  │ ─────────────────────────────────────     │ │
│  │                                             │ │
│  │ Differenza pagata:   [____3.55__]€         │ │
│  │                      0.00                   │ │
│  │ (campo visibile SOLO qui per EXPENSE)     │ │
│  │                                             │ │
│  │ Importo totale: [30.00] (read-only)       │ │
│  │                                             │ │
│  │ Subtotale buoni: 26.45€                   │ │
│  │ Differenza pagata: 3.55€                  │ │
│  │                                             │ │
│  └─────────────────────────────────────────────┘ │
│                                                 │
├─ Note, Payee, Location ──────────────────────┤
│  [_______________________________]              │
│  [_______________________________]              │
│  [_______________________________]              │
│                                                 │
├─ Tags ────────────────────────────────────────┤
│  [_______________________________]              │
│  [chip1] [chip2] [chip3]                       │
│                                                 │
├─ Ricorrenza ───────────────────────────────────┤
│  [ ] Ricorrente    Intervallo: [selezione]    │
│                                                 │
├─────────────────────────────────────────────────┤
│  [Indietro]                      [Salva]       │
└─────────────────────────────────────────────────┘
```

---

### When Payment Type = MEAL_VOUCHERS + Type = ENTRATE (INCOME)

#### Expected Visual Structure (DIFFERENT!):

```
┌─────────────────────────────────────────────────┐
│  Aggiungi Transazione              [←] [🗑]     │
├─────────────────────────────────────────────────┤
│                                                 │
│  Categoria    [Buoni Pasto]      [edit]        │
│  Tipo         [ENTRATE]           [edit]        │
│  Data         [15 Aug 2026]       [edit]        │
│  Pagamento    [Buoni Pasto]       [edit]        │
│                                                 │
├─ ┌─ Buoni Pasto (Card) ───────────────────────┐ │
│  │                                             │ │
│  │ Buoni Pasto                                │ │
│  │                                             │ │
│  │ Valore unitario: 5.29€                    │ │
│  │                                             │ │
│  │ Numero buoni pasto:  [___10____]           │ │
│  │                                             │ │
│  │ ❌ NO "Differenza pagata" field            │ │
│  │ ❌ NO "Importo totale" field               │ │
│  │ ❌ NO breakdown display                    │ │
│  │                                             │ │
│  │ (CRITICAL: These must NOT appear!)         │ │
│  │                                             │ │
│  │ Subtotale buoni: 52.90€                   │ │
│  │ (Display only - no total breakdown)        │ │
│  │                                             │ │
│  └─────────────────────────────────────────────┘ │
│                                                 │
└─────────────────────────────────────────────────┘
```

---

## ✅ Visual Verification Points

### Card Styling (DetailsMealVoucherSection)

- [ ] Card background: `secondaryContainer.copy(alpha = 0.3f)` (light background)
- [ ] Card corner radius: 16.dp (rounded corners)
- [ ] Card padding: 16.dp (internal spacing)
- [ ] Card width: `fillMaxWidth()` (full width)
- [ ] Text color: `onSecondaryContainer` (good contrast)

### Field Styling (OutlinedTextField)

**For all OutlinedTextFields inside the meal voucher card**:

- [ ] Shape: `RoundedCornerShape(16.dp)`
- [ ] Label: Visible and clear
- [ ] Placeholder: Shown when empty (e.g., "0.00")
- [ ] Keyboard type: 
  - Number for "Numero buoni"
  - Decimal for "Differenza pagata"
- [ ] Single line: true (no line breaks)
- [ ] Width: `fillMaxWidth()` (full width)

**For "Importo totale" field**:

- [ ] enabled: false (read-only)
- [ ] alpha: 0.7f (visually disabled)
- [ ] Value: Always calculated, never editable

### Spacing (SpacingSize enum)

**Between Numero buoni and Differenza pagata**:
- [ ] Changed from `SpacingSize.XS` to `SpacingSize.MD` ← IMPORTANT
- [ ] Visual separation clear and not cramped
- [ ] Not excessive (not `SpacingSize.LG`)

**Between Differenza pagata and Importo totale**:
- [ ] `SpacingSize.XS` (minimal - just visual separation)

**Between Importo totale and Subtotale display**:
- [ ] `SpacingSize.XS` (minimal)

---

## 🎨 Responsive Layout Tests

### Test on Phone (360dp width - SMALL)

**Setup**: Device/emulator with 360dp width (portrait)

**Verify**:

- [ ] All fields fully visible without truncation
- [ ] No horizontal scrolling needed
- [ ] Keyboard doesn't hide critical fields
- [ ] Labels don't overflow
- [ ] Card margins appropriate (not touching edges)
- [ ] "Save" button fully tappable at bottom
- [ ] Touch targets >= 48dp (Android standard)

**Test Interaction**:

1. Enter Numero buoni: "5"
2. Verify subtitle updates
3. Enter Differenza pagata: "3.55"
4. Verify total updates
5. Try to click Save button — should be fully accessible

### Test on Tablet (800dp width - LARGE)

**Setup**: Tablet in landscape mode or 800dp+ width

**Verify**:

- [ ] Card width appropriate (not too wide)
- [ ] Doesn't look stretched or cramped
- [ ] Layout remains centered
- [ ] Spacing proportional
- [ ] All text readable at comfortable size

### Test on Foldable (Dynamic Width)

**Setup**: Foldable device or dual-screen mode

**Verify**:

- [ ] Adapts to available width
- [ ] No layout breaks at crease line
- [ ] Fields reflow correctly when height changes

---

## 🌍 Text & Translation Verification

### Italian (IT) - Primary Language

**Strings to Verify**:

```
✅ "Buoni Pasto" (section title)
✅ "Valore unitario" (unit value label)
✅ "Numero buoni pasto" (field label)
✅ "Differenza pagata (cash/carte/altro)" (field label)
   └─ Exact match required
✅ "Importo totale buoni pasto" (total field label)
✅ "Subtotale buoni" (calculation display)
✅ "Differenza pagata:" (breakdown display)
```

**Character Encoding**:
- [ ] Special characters (è, à, ì, ù) display correctly
- [ ] No mojibake or corrupted text
- [ ] Currency symbol (€) displays correctly

### English (EN)

```
✅ "Meal Vouchers"
✅ "Unit value"
✅ "Number of meal vouchers"
✅ "Difference paid (cash/cards/other)"
✅ "Total meal vouchers amount"
```

### French (FR) Sample

```
✅ "Chèques-repas"
✅ "Valeur unitaire"
✅ "Nombre de chèques-repas"
✅ "Différence payée (espèces/cartes/autre)"
✅ "Montant total des chèques-repas"
```

**Verification Method**:

1. Settings → Language → Select language
2. Create EXPENSE + Meal Vouchers transaction
3. Verify each string matches expected translation
4. Check no string references (e.g., "@string/key" visible)

---

## 🔢 Calculation Display Verification

### Subtotale Display

When `mealVoucherCount > 0`:

```
Subtotale buoni: 26.45€
```

**Verify**:
- [ ] Calculation correct: count × 5.29
- [ ] Format: "X.XX€" with 2 decimals
- [ ] Currency symbol properly positioned
- [ ] Displayed in primary color

### Differenza Pagata Display

When `mealVoucherDifference > 0` AND `isExpenseType`:

```
Differenza pagata: 3.55€
```

**Verify**:
- [ ] Only shown when EXPENSE type
- [ ] Format: "X.XX€" with 2 decimals
- [ ] Displayed in secondary color

### Total Amount Field

Always displayed when `isMealVouchersPayment`:

```
[30.00] (read-only, disabled state)
```

**Verify**:
- [ ] Value = subtotal + difference
- [ ] Read-only (disabled appearance)
- [ ] Alpha reduced (0.7f) to show disabled state
- [ ] Can't be edited
- [ ] Label clearly indicates "Total"

---

## ⌨️ Keyboard & Input Behavior

### Numero buoni Field

**Setup**: Focus on Numero buoni field

**Verify**:
- [ ] Keyboard type: Numeric (0-9 only)
- [ ] No decimal point allowed
- [ ] Max 3 digits (999 limit enforced)
- [ ] Leading zeros handled correctly
- [ ] Clear/backspace works

**Test Values**:
- [ ] Input "0" → Accepted
- [ ] Input "5" → Accepted
- [ ] Input "999" → Accepted
- [ ] Input "1000" → Rejected or trimmed to "100"
- [ ] Input "5.5" → Decimal rejected (numeric keyboard only)

### Differenza Pagata Field

**Setup**: Focus on Differenza pagata field

**Verify**:
- [ ] Keyboard type: Decimal (0-9 and . allowed)
- [ ] Allows comma or point for decimal separator
- [ ] Max 2 decimal places enforced
- [ ] Negative values rejected or normalized

**Test Values**:
- [ ] Input "0.00" → Accepted
- [ ] Input "3.55" → Accepted
- [ ] Input "3,55" → Normalized to "3.55"
- [ ] Input "-5.00" → Rejected/error shown
- [ ] Input "5.999" → Trimmed to "5.99" or rejected
- [ ] Input "abc" → Rejected or cleared

---

## 🎯 Focus & Navigation

### Tab Order Verification

**Setup**: Use Tab key to navigate (or accessibility focus)

**Expected Tab Order**:
1. Categoria button
2. Tipo button
3. Data button
4. Pagamento button
5. Titolo field
6. Numero buoni field (in meal voucher card)
7. **Differenza pagata field (if EXPENSE)** ← Critical
8. Importo totale field (read-only)
9. Note field
10. Payee field
11. Location field
12. Tags field
13. Ricorrenza toggle
14. Save button

**Verify**:
- [ ] Tab order logical and predictable
- [ ] No skipping of fields
- [ ] Focus visible (ring/highlight)
- [ ] Return to top after last field

### Focus Behavior

- [ ] Focus highlight visible on all fields
- [ ] Sufficient contrast (WCAG AA)
- [ ] Focus doesn't disappear on input

---

## 🔊 Accessibility Verification (TalkBack)

**Setup**: Enable TalkBack (Settings → Accessibility → TalkBack)

### Screen Reader Announcements

**Numero buoni Field**:
- [ ] Announced: "Numero buoni pasto, edit box, value 5"
- [ ] Keyboard type announced: "numeric"
- [ ] Label clearly associated

**Differenza pagata Field** (EXPENSE only):
- [ ] Announced: "Differenza pagata, edit box, value 3.55"
- [ ] Keyboard type announced: "decimal" or "numeric with decimal"
- [ ] Label clearly associated
- [ ] **NOT announced on INCOME type** ← Critical

**Importo totale Field**:
- [ ] Announced: "Importo totale buoni pasto, read-only, value 30.00"
- [ ] Disabled state announced clearly

**Card Container**:
- [ ] Announced: "Buoni Pasto" (section title)
- [ ] Card structure understandable

### Navigation with TalkBack

**Test**:
1. Navigate down through all fields
2. [ ] All fields reachable
3. [ ] Logical order maintained
4. [ ] No redundant announcements

**Input with TalkBack**:
1. Double-tap Numero buoni field
2. [ ] Keyboard opens
3. [ ] Can type using keyboard
4. [ ] Done button accessible
5. [ ] Screen reader announces entered value

### Help Text & Hints

- [ ] Help available for each field (if configured)
- [ ] Placeholder text ("0.00") announced
- [ ] Required field indicator communicated

---

## 🔄 State Persistence Verification

### Rotation (Portrait ↔ Landscape)

**Setup**: 
1. Enter EXPENSE + Meal Vouchers
2. Fill in: Numero buoni = "5", Differenza pagata = "3.55"
3. Rotate device 90°

**Verify**:
- [ ] Numero buoni value preserved: "5"
- [ ] Differenza pagata value preserved: "3.55"
- [ ] Total recalculated correctly: "30.00"
- [ ] No data loss
- [ ] Keyboard closed on rotation

### Navigation Away & Back

**Setup**:
1. Enter partial transaction data
2. Tap back button (don't save)

**Verify**:
- [ ] Dialog: "Discard changes?" shown
- [ ] Options: Cancel / Discard

**If Cancel**:
- [ ] Return to form
- [ ] Data preserved

**If Discard**:
- [ ] Return to previous screen
- [ ] Data cleared

### App Lifecycle

**Setup**:
1. Enter transaction with meal vouchers
2. Press Home (minimize app)
3. Wait 30 seconds
4. Return to app (recent apps)

**Verify**:
- [ ] App restored to same state
- [ ] Form data preserved
- [ ] No crashes

---

## 🎨 Visual Consistency Tests

### Color Verification

**Card Background**:
- [ ] Light secondary container color
- [ ] Semi-transparent (alpha 0.3)
- [ ] Consistent across light/dark theme

**Text Colors**:
- [ ] Labels: Primary color or strong contrast
- [ ] Values: `onSecondaryContainer` for card text
- [ ] Disabled fields: Reduced contrast (alpha 0.7f)

**Total Amount Display**:
- [ ] Primary color emphasizing calculation
- [ ] Bold or SemiBold font weight

**Subtotale Display**:
- [ ] Secondary color distinguishing from total
- [ ] SemiBold weight

### Font Verification

**Title ("Buoni Pasto")**:
- [ ] `titleSmall` style
- [ ] FontWeight.Bold
- [ ] Appropriate size hierarchy

**Field Labels**:
- [ ] `labelMedium` or equivalent
- [ ] Clear and readable

**Values/Calculations**:
- [ ] `bodyMedium` for balance
- [ ] `bodySmall` for secondary info

---

## 📊 Data Accuracy Verification

### Calculation Tests (with Calculator)

| Count | Value | Difference | Expected Total | Actual | Status |
|-------|-------|------------|-----------------|--------|--------|
| 5 | 5.29 | 0.00 | 26.45 | __ | [ ] |
| 5 | 5.29 | 3.55 | 30.00 | __ | [ ] |
| 10 | 5.29 | 0.00 | 52.90 | __ | [ ] |
| 1 | 5.29 | 0.01 | 5.30 | __ | [ ] |
| 99 | 5.29 | 99.99 | 624.43 | __ | [ ] |

**Verification Method**:
1. Enter each scenario
2. Compare screen total with calculator
3. Mark [ ] if match, [✗] if mismatch

---

## 🐛 Known Issues to Monitor

| Issue | Impact | How to Spot |
|-------|--------|-----------|
| Difference field shows on INCOME | CRITICAL | Create INCOME + vouchers, see field |
| Calculation off by 0.01€ | High | 5×5.29 = 26.45 not 26.44 |
| Field truncation on small screen | Medium | 360dp phone width, check overflow |
| String not translated | Medium | Change language, see missing key |
| TalkBack doesn't read field | High | Enable TalkBack, navigate with focus |
| Keyboard type wrong | Medium | Numeric field allows decimals |
| Total field editable | CRITICAL | Try to tap/type in total field |
| Negative difference accepted | High | Enter -5.00, should reject |

---

## ✅ Sign-Off Checklist

Complete this checklist before marking feature as "UI Verified":

### Layout & Styling
- [ ] Card styling correct (background, radius, padding)
- [ ] Field styling consistent (shapes, sizes)
- [ ] Spacing correct (SpacingSize.MD between key fields)
- [ ] No visual glitches or overlaps

### Responsive Design
- [ ] Layout OK on 360dp (small phone)
- [ ] Layout OK on 800dp (tablet)
- [ ] Layout OK on foldable/split-screen
- [ ] All fields accessible without horizontal scroll

### Text & Translation
- [ ] Italian (IT) strings correct
- [ ] English (EN) strings correct
- [ ] Sample language (e.g., FR) correct
- [ ] Character encoding OK (no mojibake)
- [ ] Currency display correct

### Keyboard & Input
- [ ] Numero buoni: numeric keyboard only
- [ ] Differenza pagata: decimal keyboard
- [ ] Negative values rejected
- [ ] Precision enforced (2 decimals max)

### Accessibility
- [ ] TalkBack reads all fields
- [ ] Logical tab order maintained
- [ ] Focus visible on all interactive elements
- [ ] Screen reader announces field values

### Calculation
- [ ] Subtotal calculated: count × 5.29
- [ ] Total calculated: subtotal + difference
- [ ] Display shows correct currency format
- [ ] Sample calculations verified with calculator

### Critical Specification (EXPENSE-only)
- [ ] EXPENSE transaction: Differenza pagata field VISIBLE
- [ ] INCOME transaction: Differenza pagata field HIDDEN
- [ ] Edit EXPENSE: Field populated correctly
- [ ] Edit INCOME: Field not shown

### Data Persistence
- [ ] Rotation preserves data
- [ ] Navigation away/back works
- [ ] App lifecycle state preserved
- [ ] No crashes on state changes

---

**Tester Name**: ___________________  
**Date**: ___________________  
**Device**: ___________________  
**API Level**: ___________________  
**Language**: ___________________  

**Overall Result**: ✅ PASS / ❌ FAIL  
**Issues Found**: ____________________  
**Sign-Off**: ✅ Ready for Release / ❌ Needs Fixes

---

**Document Version**: 1.0  
**Last Updated**: 2026-08-15  
**Status**: Ready for Testing
