#!/bin/bash

set -e

# Navigate to project root (two levels up from scripts directory: scripts -> extra -> root)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Setup Java home
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║        🧪 AntCashManager Test Suite + Coverage Report          ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""
echo "📁 Project: $PROJECT_ROOT"
echo ""

echo "📋 Testing Configuration:"
echo "  Framework: JUnit 4 + MockK + Compose UI Test v2"
echo "  Coverage: Jacoco (line & branch coverage)"
echo "  Test Runners: :shared:allTests + :androidApp:testDebugUnitTest"
echo ""

echo "════════════════════════════════════════════════════════════════"
echo "🚀 Executing Test Suite..."
echo "════════════════════════════════════════════════════════════════"
echo ""

cd "$PROJECT_ROOT"

# Create temp file for test output
TEST_OUTPUT=$(mktemp)
trap "rm -f $TEST_OUTPUT" EXIT

# Run all tests with Jacoco coverage enabled
echo "Running unit tests..."
./gradlew \
  :shared:testAndroidHostTest \
  :androidApp:testDebugUnitTest \
  :androidApp:jacocoTestDebugUnitTestReport \
  --info 2>&1 | tee "$TEST_OUTPUT"

BUILD_STATUS=$?

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "📊 Test Summary"
echo "════════════════════════════════════════════════════════════════"
echo ""

# Extract test counts from gradle output
TOTAL_TESTS=$(grep -o "[0-9]* tests completed" "$TEST_OUTPUT" | tail -1 | grep -o "^[0-9]*" || echo "?")
FAILED_TESTS=$(grep -o "[0-9]* failed" "$TEST_OUTPUT" | tail -1 | grep -o "^[0-9]*" || echo "0")
SKIPPED_TESTS=$(grep -o "[0-9]* skipped" "$TEST_OUTPUT" | tail -1 | grep -o "^[0-9]*" || echo "0")

if [ "$TOTAL_TESTS" != "?" ]; then
    PASSED_TESTS=$((${TOTAL_TESTS:-0} - ${FAILED_TESTS:-0} - ${SKIPPED_TESTS:-0}))

    echo "📈 Test Execution Results:"
    echo ""
    echo "  Total Tests:    $TOTAL_TESTS"
    echo "  ✅ Passed:      $PASSED_TESTS"
    if [ "${FAILED_TESTS:-0}" -gt 0 ]; then
        echo "  ❌ Failed:      $FAILED_TESTS"
    else
        echo "  ❌ Failed:      0"
    fi
    echo "  ⏭️  Skipped:     ${SKIPPED_TESTS:-0}"
    echo ""
fi

# Parse and display test reports
echo "📋 Test Reports by Module:"
echo ""

if [ -f "shared/build/reports/tests/androidHostTest/index.html" ]; then
    echo "  ✅ Shared Library Tests"
    echo "     → file://$PROJECT_ROOT/shared/build/reports/tests/androidHostTest/index.html"
fi

if [ -f "androidApp/build/reports/tests/testDebugUnitTest/index.html" ]; then
    echo "  ✅ Android App Unit Tests"
    echo "     → file://$PROJECT_ROOT/androidApp/build/reports/tests/testDebugUnitTest/index.html"
fi

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "📊 Code Coverage Report"
echo "════════════════════════════════════════════════════════════════"
echo ""

# Check for Jacoco coverage files
ANDROID_COVERAGE_DIR="androidApp/build/reports/jacoco/jacocoTestDebugUnitTestReport/html"
SHARED_COVERAGE_DIR="shared/build/reports/jacoco/testAndroidHostTest/html"

# Function to display coverage report location and extract metrics
display_coverage_report() {
    local module=$1
    local coverage_dir=$2

    if [ -f "$coverage_dir/index.html" ]; then
        echo "  ✅ $module Coverage Report:"
        echo "     📄 file://$PROJECT_ROOT/$coverage_dir/index.html"
        echo ""
    elif [ -d "$coverage_dir" ]; then
        echo "  ⚠️  $module Coverage Report (directory exists, may not be fully generated)"
        echo ""
    fi
}

display_coverage_report "Android App" "$ANDROID_COVERAGE_DIR"
display_coverage_report "Shared Library" "$SHARED_COVERAGE_DIR"

# Display coverage goals and progress
echo "🎯 Coverage Goals:"
echo ""
echo "  Minimum (Enforce):  50%  (project-wide)"
echo "  Target (Phase 2):   70%  (core modules)"
echo "  Ideal:              80%+ (critical paths)"
echo ""
echo "  Module Targets:"
echo "    • androidApp:        60% (currently ~23%)"
echo "    • shared:domain:     75% (currently ~39%)"
echo "    • shared:data:       70% (currently ~15%)"
echo ""

# Build status
echo "════════════════════════════════════════════════════════════════"
if [ $BUILD_STATUS -eq 0 ]; then
    echo "✅ BUILD SUCCESSFUL - All tests executed"
    echo ""
    echo "📚 Next Steps:"
    echo "  1. Open coverage reports (links above) to identify gaps"
    echo "  2. Review failing tests (if any) and fix per testing guidelines"
    echo "  3. Add new tests for uncovered code paths"
    echo "  4. Target: reach 70% overall coverage"
    echo ""
    echo "📖 Resources:"
    echo "  • Testing Standards: AGENTS.md (Testing section)"
    echo "  • MockK Guide: .github/agents/agent-unit-tests-mockk.agent.md"
    echo "  • Compose UI Testing: https://developer.android.com/jetpack/compose/testing"
    echo ""
else
    echo "❌ BUILD FAILED - Fix errors above and retry"
    echo ""
    echo "🔍 Debugging:"
    echo "  1. Check error messages in output above"
    echo "  2. Run individual module: ./gradlew :shared:testAndroidHostTest"
    echo "  3. Run unit tests only: ./gradlew :androidApp:testDebugUnitTest"
    echo "  4. Check compilation: ./gradlew :androidApp:compileDebugKotlin"
    echo ""
    echo "💡 Testing Guidelines:"
    echo "  • Use MockK for mocking (never Mockito)"
    echo "  • Use Compose UI Test v2 (no Roboelectric in src/test)"
    echo "  • Extend BaseUnitTest for ViewModel tests"
    echo "  • Use launchInBackground() for Flow collectors"
    echo ""
    exit 1
fi

echo "════════════════════════════════════════════════════════════════"
echo "✨ Test Session Complete"
echo "════════════════════════════════════════════════════════════════"
echo ""
