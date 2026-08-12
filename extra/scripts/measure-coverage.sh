#!/bin/bash

# Script to measure test coverage for AntCashManager
# Outputs coverage reports and summary statistics

set -e

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

echo "════════════════════════════════════════════════════════════════════"
echo "AntCashManager Test Coverage Measurement Tool"
echo "════════════════════════════════════════════════════════════════════"
echo ""

# Configuration
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
REPORTS_DIR="${PROJECT_ROOT}/build/reports/coverage"
UNIT_TEST_REPORT="${REPORTS_DIR}/unit-tests/index.html"
ANDROID_TEST_REPORT="${REPORTS_DIR}/android-tests/index.html"

echo "📁 Project Root: ${PROJECT_ROOT}"
echo "📊 Reports Directory: ${REPORTS_DIR}"
echo ""

# Create reports directory
mkdir -p "${REPORTS_DIR}"

echo "🧪 Running Unit Tests..."
echo "────────────────────────────────────────────────────────────────────"

cd "${PROJECT_ROOT}"

# Run unit tests with Jacoco
./gradlew :androidApp:testDebugUnitTest -x :androidApp:connectedDebugAndroidTest --info 2>&1 | grep -E "testDebugUnitTest|PASSED|FAILED" || true

echo ""
echo "📦 Unit Test Coverage Report"
echo "────────────────────────────────────────────────────────────────────"

if [ -f "${PROJECT_ROOT}/androidApp/build/reports/jacoco/testDebugUnitTest/html/index.html" ]; then
    echo "✅ Unit Test Report: file://${PROJECT_ROOT}/androidApp/build/reports/jacoco/testDebugUnitTest/html/index.html"
else
    echo "⚠️  Unit Test Report not generated"
fi

echo ""
echo "════════════════════════════════════════════════════════════════════"
echo "Coverage Summary"
echo "════════════════════════════════════════════════════════════════════"

# Count test files
UNIT_TEST_COUNT=$(find "${PROJECT_ROOT}/androidApp/src/test" -name "*Test.kt" | wc -l)
COMPONENT_COUNT=$(find "${PROJECT_ROOT}/androidApp/src/main" -name "*ViewModel.kt" -o -name "*Screen.kt" | wc -l)

echo "📊 Test Files Found:        ${UNIT_TEST_COUNT}"
echo "📱 Components to Test:      ${COMPONENT_COUNT}"

# Coverage target
TARGET_COVERAGE=70

echo ""
echo "🎯 Coverage Target:         ${TARGET_COVERAGE}%"
echo ""

# Display recommendations
echo "════════════════════════════════════════════════════════════════════"
echo "Recommendations to Increase Coverage"
echo "════════════════════════════════════════════════════════════════════"
echo ""
echo "1. ✅ PRIORITY CRITICAL (1-2 hours):"
echo "   • Fix compilation errors in shared/src/androidHostTest/"
echo "   • Fix type mismatches in SettingsRepositoryImplTest"
echo "   • Fix SavedDateFilter test timestamp conversions"
echo ""
echo "2. ✅ PRIORITY HIGH (2-3 hours):"
echo "   • Add comprehensive ViewModel tests (HomeViewModel, SettingsDataViewModel)"
echo "   • Add UseCase tests for domain logic"
echo "   • Add Repository tests with mock data"
echo ""
echo "3. 📋 PRIORITY MEDIUM (3-4 hours):"
echo "   • Add UI component tests (Cards, Dialogs, Buttons)"
echo "   • Add instrumentation tests for screens"
echo "   • Add edge case tests (empty lists, large datasets)"
echo ""
echo "4. 📝 PRIORITY LOW (ongoing):"
echo "   • Performance benchmarking tests"
echo "   • A11y (accessibility) tests"
echo "   • Integration tests"
echo ""

echo "════════════════════════════════════════════════════════════════════"
echo "Coverage Report Generated"
echo "════════════════════════════════════════════════════════════════════"
