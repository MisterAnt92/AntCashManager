#!/bin/bash

set -e

# Navigate to project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Setup Java home
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

echo ""
echo "╔══════════════════════════════════════════════════════════════════╗"
echo "║                  🧪 AntCashManager Test Suite                    ║"
echo "╚══════════════════════════════════════════════════════════════════╝"
echo ""

cd "$PROJECT_ROOT"

# Create temp file for test output
TEST_OUTPUT=$(mktemp)
trap "rm -f $TEST_OUTPUT" EXIT

# Run all tests with Jacoco coverage enabled
echo "▶️  Running tests..."
echo "   • :shared:testAndroidHostTest"
echo "   • :androidApp:testDebugUnitTest"
echo ""

./gradlew \
  :shared:testAndroidHostTest \
  :androidApp:testDebugUnitTest \
  --info 2>&1 | tee "$TEST_OUTPUT"

BUILD_STATUS=$?

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  📊 RESULTS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Check if build succeeded
if [ $BUILD_STATUS -eq 0 ]; then
    echo "  ✅ All tests passed"
    echo ""

    # Display coverage report locations
    echo "📁 Coverage Reports:"
    echo ""

    if [ -f "androidApp/build/reports/jacoco/jacocoTestDebugUnitTestReport/html/index.html" ]; then
        echo "  ✓ Android App Unit Tests"
        echo "    file://$PROJECT_ROOT/androidApp/build/reports/jacoco/jacocoTestDebugUnitTestReport/html/index.html"
        echo ""
    fi

    if [ -f "shared/build/reports/tests/androidHostTest/index.html" ]; then
        echo "  ✓ Shared Library Tests"
        echo "    file://$PROJECT_ROOT/shared/build/reports/tests/androidHostTest/index.html"
        echo ""
    fi

    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  ✨ SUCCESS - All tests executed without errors"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
else
    echo "  ❌ Tests failed or had compilation errors"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  ❌ FAILURE - Fix errors above and retry"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "🔍 Debugging Options:"
    echo ""
    echo "  # Test only shared data layer:"
    echo "  ./gradlew :shared:testAndroidHostTest"
    echo ""
    echo "  # Test only Android app:"
    echo "  ./gradlew :androidApp:testDebugUnitTest"
    echo ""
    echo "  # Check compilation:"
    echo "  ./gradlew :androidApp:compileDebugKotlin"
    echo ""
    exit 1
fi

echo ""
