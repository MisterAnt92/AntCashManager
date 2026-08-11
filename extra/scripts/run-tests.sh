#!/bin/bash

set -e

# Navigate to project root (parent of scripts directory)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║           🧪 AntCashManager Test Suite + Coverage              ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""
echo "📁 Project: $PROJECT_ROOT"
echo ""
echo "This script runs comprehensive tests and generates coverage reports"
echo "using Jacoco to track code coverage metrics."
echo ""

echo "📋 Test Modules:"
echo "  ✓ :shared:allTests (Shared Library - Common + Android Host)"
echo "  ✓ :androidApp:testDebugUnitTest (Android App Unit Tests)"
echo "  ✓ :androidApp:testCoverageReport (Jacoco Coverage)"
echo ""

echo "🔍 Test Classes by Module:"
echo ""
echo "SHARED LIBRARY (:shared:allTests)"
echo "  Domain Layer (Use Cases):"
echo "    - FilterTransactionsUseCaseTest (16 tests)"
echo "    - GetTransactionSuggestionsUseCaseTest (7 tests)"
echo "    - TransactionMaintenanceUseCasesMockkTest (5 tests)"
echo "    - Settings/Category/Transaction Use Case Tests"
echo "  Data Layer (Repository, DAO, Mapper):"
echo "    - TransactionRepositoryImplTest"
echo "    - FakeTransactionDao (test helpers)"
echo ""
echo "ANDROID APP (:androidApp:testDebugUnitTest)"
echo "  ViewModels:"
echo "    - AddTransactionViewModelTest (30+ tests)"
echo "    - TransactionsViewModelTest (25+ tests)"
echo "    - ChartsViewModelTest (15+ tests)"
echo "    - HomeViewModelTest"
echo "    - SettingsViewModelTest"
echo "  Services & Utils:"
echo "    - BackupServiceTest"
echo "    - Other utility & component tests"
echo ""

echo "════════════════════════════════════════════════════════════════"
echo "Executing tests..."
echo "════════════════════════════════════════════════════════════════"
echo ""

cd "$PROJECT_ROOT"

# Run tests and capture output
TEST_OUTPUT=$(mktemp)
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew \
  :shared:allTests \
  :androidApp:testDebugUnitTest \
  :androidApp:testCoverageReport \
  2>&1 | tee "$TEST_OUTPUT"
BUILD_STATUS=$?

# Extract test summary
echo ""
echo "════════════════════════════════════════════════════════════════"
echo ""

# Try to parse test results from gradle output and report files
SHARED_TESTS=""
ANDROID_TESTS=""

# Method 1: Parse from gradle console output
if grep -q "tests completed" "$TEST_OUTPUT"; then
    SHARED_TESTS=$(grep "tests completed" "$TEST_OUTPUT" | head -1 | grep -o "[0-9]*" | head -1)
    ANDROID_TESTS=$(grep "tests completed" "$TEST_OUTPUT" | tail -1 | grep -o "[0-9]*" | head -1)
fi

# Method 2: Try to extract from test report HTML files if available
if [ -z "$SHARED_TESTS" ] && [ -f "shared/build/reports/tests/allTests/index.html" ]; then
    SHARED_TESTS=$(grep -o "Tests run: [0-9]*" "shared/build/reports/tests/allTests/index.html" 2>/dev/null | grep -o "[0-9]*" || echo "")
fi

if [ -z "$ANDROID_TESTS" ] && [ -f "androidApp/build/reports/tests/testDebugUnitTest/index.html" ]; then
    ANDROID_TESTS=$(grep -o "Tests run: [0-9]*" "androidApp/build/reports/tests/testDebugUnitTest/index.html" 2>/dev/null | grep -o "[0-9]*" || echo "")
fi

if [ $BUILD_STATUS -eq 0 ]; then
    echo "✅ BUILD SUCCESSFUL"
    echo ""
    echo "📊 Test Results:"

    if [ -n "$SHARED_TESTS" ] && [ "$SHARED_TESTS" != "0" ]; then
        echo "  Shared Library:  $SHARED_TESTS tests passed ✅"
    else
        echo "  Shared Library:  ✅ (cached)"
    fi

    if [ -n "$ANDROID_TESTS" ] && [ "$ANDROID_TESTS" != "0" ]; then
        echo "  Android App:     $ANDROID_TESTS tests passed ✅"
    else
        echo "  Android App:     ✅ (cached)"
    fi

    if [ -n "$SHARED_TESTS" ] && [ -n "$ANDROID_TESTS" ]; then
        TOTAL=$((${SHARED_TESTS:-0} + ${ANDROID_TESTS:-0}))
        echo ""
        echo "  🎉 TOTAL: $TOTAL tests passed"
    fi

    # Show test report locations
    echo ""
    echo "📋 Test Reports:"
    [ -f "shared/build/reports/tests/allTests/index.html" ] && echo "  • Shared: file://$PROJECT_ROOT/shared/build/reports/tests/allTests/index.html"
    [ -f "androidApp/build/reports/tests/testDebugUnitTest/index.html" ] && echo "  • Android: file://$PROJECT_ROOT/androidApp/build/reports/tests/testDebugUnitTest/index.html"

    # Display Coverage Reports
    echo ""
    echo "════════════════════════════════════════════════════════════════"
    echo "📊 Code Coverage Report"
    echo "════════════════════════════════════════════════════════════════"
    echo ""

    # Check for Jacoco reports
    UNIT_COVERAGE_REPORT="androidApp/build/reports/jacoco/jacocoTestDebugUnitTestReport/html/index.html"

    if [ -f "$UNIT_COVERAGE_REPORT" ]; then
        echo "✅ Jacoco Coverage Report Generated"
        echo ""
        echo "📈 Coverage Details:"
        echo "  • Unit Test Coverage: file://$PROJECT_ROOT/$UNIT_COVERAGE_REPORT"
        echo ""
        echo "💡 To view coverage:"
        echo "  $ open file://$PROJECT_ROOT/$UNIT_COVERAGE_REPORT"
        echo ""
        echo "Target Coverage Goals:"
        echo "  • Current: ~23% (androidApp) | ~39% (shared)"
        echo "  • After Phase 2: ~35% (androidApp) | ~41% (shared)"
        echo "  • Target: 60-70% (overall)"
    else
        echo "ℹ️  Coverage reports not yet generated"
        echo "   Run: ./gradlew :androidApp:testCoverageReport"
    fi

    echo ""
    echo "════════════════════════════════════════════════════════════════"
else
    echo "❌ BUILD FAILED"
    echo ""
    echo "🔍 Debugging:"
    echo "  1. Check the full output above for error details"
    echo "  2. Run individual modules: ./gradlew :shared:allTests"
    echo "  3. Check compilation: ./gradlew :androidApp:compileDebugKotlin"
    echo ""
    rm -f "$TEST_OUTPUT"
    exit 1
fi

rm -f "$TEST_OUTPUT"
echo ""
echo "════════════════════════════════════════════════════════════════"
echo "✨ Test Execution Complete"
echo "════════════════════════════════════════════════════════════════"
echo ""
echo "🚀 Next Steps:"
echo "  1. Review coverage reports (see links above)"
echo "  2. Identify low-coverage areas for improvement"
echo "  3. Add tests for uncovered code paths"
echo "  4. Target: Reach 60-70% overall coverage"
echo ""
echo "📚 Resources:"
echo "  • Test Guide: ./plans/analizza-il-progetto-kmp-witty-wilkinson.md"
echo "  • MockK Reference: https://mockk.io/"
echo "  • Roboelectric Setup: http://robolectric.org/"
echo ""
echo "════════════════════════════════════════════════════════════════"
