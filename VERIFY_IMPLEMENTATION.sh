#!/bin/bash
# Verification Script for Multi-Step Add Transaction Implementation

echo "═══════════════════════════════════════════════════════════════"
echo "Multi-Step Add Transaction - Verification Report"
echo "═══════════════════════════════════════════════════════════════"
echo ""

# Check if all files exist
echo "📁 Checking package structure..."
FILES=(
    "androidApp/src/main/kotlin/com/antcashmanager/android/ui/screen/add_transaction/AddTransactionState.kt"
    "androidApp/src/main/kotlin/com/antcashmanager/android/ui/screen/add_transaction/AddTransactionViewModel.kt"
    "androidApp/src/main/kotlin/com/antcashmanager/android/ui/screen/add_transaction/AddTransactionScreen.kt"
    "androidApp/src/main/kotlin/com/antcashmanager/android/ui/screen/add_transaction/README.md"
    "androidApp/src/main/kotlin/com/antcashmanager/android/ui/screen/add_transaction/ARCHITECTURE.md"
)

for file in "${FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file"
    else
        echo "❌ $file"
    fi
done

echo ""
echo "🔍 Checking old file..."
if [ -f "androidApp/src/main/kotlin/com/antcashmanager/android/ui/screen/transactions/AddTransactionScreenOld.kt" ]; then
    echo "✅ Old file archived as AddTransactionScreenOld.kt"
else
    echo "⚠️  Old file not found (already deleted?)"
fi

echo ""
echo "📝 Checking navigation integration..."
if grep -q "import com.antcashmanager.android.ui.screen.add_transaction.AddTransactionScreen" androidApp/src/main/kotlin/com/antcashmanager/android/navigation/NavGraph.kt; then
    echo "✅ NavGraph imports new AddTransactionScreen"
else
    echo "❌ NavGraph import missing"
fi

if grep -q 'composable("add_transaction")' androidApp/src/main/kotlin/com/antcashmanager/android/navigation/NavGraph.kt; then
    echo "✅ add_transaction route defined in NavGraph"
else
    echo "❌ add_transaction route not found"
fi

echo ""
echo "🎯 Checking event system..."
if grep -q "sealed interface AddTransactionEvent" androidApp/src/main/kotlin/com/antcashmanager/android/ui/screen/add_transaction/AddTransactionViewModel.kt; then
    echo "✅ Event system implemented"
else
    echo "❌ Event system not found"
fi

echo ""
echo "🔄 Checking step enum..."
STEPS=("CATEGORY_SELECTION" "TYPE_SELECTION" "DETAILS" "CONFIRMATION")
for step in "${STEPS[@]}"; do
    if grep -q "$step" androidApp/src/main/kotlin/com/antcashmanager/android/ui/screen/add_transaction/AddTransactionState.kt; then
        echo "✅ Step $step defined"
    else
        echo "❌ Step $step missing"
    fi
done

echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "Verification complete!"
echo "═══════════════════════════════════════════════════════════════"

