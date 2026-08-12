#!/bin/bash

set -e

# Navigate to project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Setup Java home
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Parse arguments
TEST_TYPE="${1:-all}"  # all, unit, instrumentation, coverage

echo ""
echo "╔══════════════════════════════════════════════════════════════════╗"
echo "║                  🧪 AntCashManager Test Suite                    ║"
echo "╚══════════════════════════════════════════════════════════════════╝"
echo ""
echo "Test Type: $TEST_TYPE"
echo ""

cd "$PROJECT_ROOT"

# Create temp file for test output
TEST_OUTPUT=$(mktemp)
trap "rm -f $TEST_OUTPUT" EXIT

BUILD_STATUS=0

# ─── Run Unit Tests ──────────────────────────────────────────────────────────
if [ "$TEST_TYPE" = "all" ] || [ "$TEST_TYPE" = "unit" ]; then
    echo "▶️  Running Unit Tests..."
    echo "   • :shared:testAndroidHostTest"
    echo "   • :androidApp:testDebugUnitTest"
    echo ""

    ./gradlew \
      :shared:testAndroidHostTest \
      :androidApp:testDebugUnitTest \
      --info 2>&1 | tee "$TEST_OUTPUT" || BUILD_STATUS=$?
fi

# ─── Run Instrumentation Tests ──────────────────────────────────────────────
if [ "$TEST_TYPE" = "all" ] || [ "$TEST_TYPE" = "instrumentation" ]; then
    echo ""
    echo "▶️  Running Instrumentation Tests..."
    echo "   • :androidApp:connectedDebugAndroidTest"
    echo ""
    echo "⚠️  Make sure a device or emulator is connected!"
    echo ""

    ./gradlew \
      :androidApp:connectedDebugAndroidTest \
      --info 2>&1 | tee -a "$TEST_OUTPUT" || BUILD_STATUS=$?
fi

# ─── Generate Coverage Reports ──────────────────────────────────────────────
if [ "$TEST_TYPE" = "all" ] || [ "$TEST_TYPE" = "coverage" ]; then
    echo ""
    echo "▶️  Generating Coverage Reports (JaCoCo)..."
    echo "   • :shared:testAndroidHostTest (with coverage)"
    echo "   • :androidApp:testDebugUnitTest (with coverage)"
    echo "   • jacocoTestDebugUnitTestReport"
    echo ""

    ./gradlew \
      :shared:testAndroidHostTest \
      :androidApp:testDebugUnitTest \
      :shared:testCoverageReport \
      :androidApp:jacocoTestDebugUnitTestReport \
      :androidApp:testCoverageReport \
      --info 2>&1 | tee -a "$TEST_OUTPUT" || BUILD_STATUS=$?
fi

# ─── Helper: aggrega i risultati da file TEST-*.xml JUnit ──────────────────────
# Usage: parse_test_results <xml_dir> → stampa totale/passed/failed/skipped/errors
parse_test_results() {
    local xml_dir="$1"
    local total=0 failures=0 errors=0 skipped=0

    if [ ! -d "$xml_dir" ]; then
        echo "n/a"
        return
    fi

    while IFS= read -r file; do
        t=$(grep -oP '(?<=tests=")[0-9]+' "$file" | head -1); t=${t:-0}
        f=$(grep -oP '(?<=failures=")[0-9]+' "$file" | head -1); f=${f:-0}
        e=$(grep -oP '(?<=errors=")[0-9]+' "$file" | head -1); e=${e:-0}
        s=$(grep -oP '(?<=skipped=")[0-9]+' "$file" | head -1); s=${s:-0}
        total=$(( total + t ))
        failures=$(( failures + f ))
        errors=$(( errors + e ))
        skipped=$(( skipped + s ))
    done < <(find "$xml_dir" -maxdepth 1 -name "TEST-*.xml" 2>/dev/null)

    local passed=$(( total - failures - errors - skipped ))
    echo "$total $passed $failures $errors $skipped"
}

# ─── Helper: stampa una riga di riepilogo modulo ────────────────────────────────
print_module_summary() {
    local label="$1"
    local xml_dir="$2"
    local html_report="$3"

    read -r total passed failures errors skipped <<< "$(parse_test_results "$xml_dir")"

    if [ "$total" = "n/a" ] || [ -z "$total" ]; then
        printf "  %-40s  %s\n" "$label" "⚠️  nessun risultato trovato"
        return
    fi

    local status_icon="✅"
    if [ "$failures" -gt 0 ] || [ "$errors" -gt 0 ]; then
        status_icon="❌"
    fi

    printf "  %s %-42s  %s tests  |  ✓ %s  ✗ %s  ↷ %s\n" \
        "$status_icon" "$label" "$total" "$passed" "$(( failures + errors ))" "$skipped"

    if [ -n "$html_report" ] && [ -f "$html_report" ]; then
        printf "    📄 file://%s\n" "$html_report"
    fi
}

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  📊 RESULTS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# ─── Show Unit Test Results ─────────────────────────────────────────────────────
if [ "$TEST_TYPE" = "all" ] || [ "$TEST_TYPE" = "unit" ] || [ "$TEST_TYPE" = "coverage" ]; then
    echo "  📦 Unit Tests"
    echo ""

    SHARED_XML_DIR="$PROJECT_ROOT/shared/build/intermediates/unit_test_results/androidMain/testAndroidHostTest"
    SHARED_HTML="$PROJECT_ROOT/shared/build/reports/tests/testAndroidHostTest/index.html"
    print_module_summary ":shared:testAndroidHostTest (Domain + Data)" "$SHARED_XML_DIR" "$SHARED_HTML"

    echo ""

    ANDROID_XML_DIR="$PROJECT_ROOT/androidApp/build/intermediates/unit_test_results/debug/testDebugUnitTest"
    ANDROID_HTML="$PROJECT_ROOT/androidApp/build/reports/tests/testDebugUnitTest/index.html"
    print_module_summary ":androidApp:testDebugUnitTest (Presentation)" "$ANDROID_XML_DIR" "$ANDROID_HTML"

    echo ""
fi

# ─── Show Instrumentation Test Results ─────────────────────────────────────────
if [ "$TEST_TYPE" = "all" ] || [ "$TEST_TYPE" = "instrumentation" ]; then
    echo "  📱 Instrumentation Tests"
    echo ""

    INSTRUMENTATION_XML_DIR="$PROJECT_ROOT/androidApp/build/outputs/androidTest-results/connected/debug"
    INSTRUMENTATION_HTML="$PROJECT_ROOT/androidApp/build/reports/androidTests/connected/debug/index.html"

    if [ -d "$INSTRUMENTATION_XML_DIR" ] || [ -f "$INSTRUMENTATION_HTML" ]; then
        print_module_summary ":androidApp:connectedDebugAndroidTest" "$INSTRUMENTATION_XML_DIR" "$INSTRUMENTATION_HTML"
    else
        printf "  %-40s  %s\n" ":androidApp:connectedDebugAndroidTest" "⚠️  No device/emulator connected or no results found"
    fi
    echo ""
fi

# ─── Show Coverage Reports ────────────────────────────────────────────────────
if [ "$TEST_TYPE" = "all" ] || [ "$TEST_TYPE" = "coverage" ]; then
    echo "  📈 Coverage Reports (JaCoCo)"
    echo ""

    # Try multiple possible paths for androidApp coverage
    JACOCO_ANDROID_PATHS=(
        "$PROJECT_ROOT/androidApp/build/reports/jacoco/jacocoTestDebugUnitTestReport/html/index.html"
        "$PROJECT_ROOT/androidApp/build/reports/jacoco/jacocoConnectedDebugAndroidTestReport/html/index.html"
        "$PROJECT_ROOT/androidApp/build/reports/coverage/debug/index.html"
    )

    JACOCO_ANDROID=""
    for path in "${JACOCO_ANDROID_PATHS[@]}"; do
        if [ -f "$path" ]; then
            JACOCO_ANDROID="$path"
            break
        fi
    done

    # Try multiple possible paths for shared coverage
    JACOCO_SHARED_PATHS=(
        "$PROJECT_ROOT/shared/build/reports/jacoco/jacocoTestReport/html/index.html"
        "$PROJECT_ROOT/shared/build/reports/jacoco/testAndroidHostTestCoverage/html/index.html"
        "$PROJECT_ROOT/shared/build/reports/coverage/androidMain/index.html"
    )

    JACOCO_SHARED=""
    for path in "${JACOCO_SHARED_PATHS[@]}"; do
        if [ -f "$path" ]; then
            JACOCO_SHARED="$path"
            break
        fi
    done

    COVERAGE_FOUND=false

    if [ -n "$JACOCO_ANDROID" ]; then
        printf "    📊 androidApp  →  file://%s\n" "$JACOCO_ANDROID"
        COVERAGE_FOUND=true
    else
        printf "    ⚠️  androidApp coverage report not found\n"
    fi

    if [ -n "$JACOCO_SHARED" ]; then
        printf "    📊 shared      →  file://%s\n" "$JACOCO_SHARED"
        COVERAGE_FOUND=true
    else
        printf "    ⚠️  shared coverage report not found\n"
    fi

    if [ "$COVERAGE_FOUND" = false ]; then
        echo ""
        echo "    💡 Tips to generate coverage reports:"
        echo "       1. Make sure JaCoCo is enabled in build.gradle"
        echo "       2. Run tests with coverage generation:"
        echo "          ./gradlew :androidApp:testDebugUnitTest --info"
        echo "          ./gradlew :shared:testAndroidHostTest --info"
        echo ""
        echo "       3. Look for 'Creating coverage report' in build output"
    fi
    echo ""
fi

# ─── Final Status ───────────────────────────────────────────────────────────────
if [ $BUILD_STATUS -eq 0 ]; then
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  ✨ SUCCESS - All tests executed without errors"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
else
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  ❌ FAILURE - Fix errors above and retry"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
fi

# ─── Usage Help ──────────────────────────────────────────────────────────────────
echo "📖 Usage:"
echo ""
echo "  ./extra/scripts/run-tests.sh [type]"
echo ""
echo "  Types:"
echo "    all              - Run unit, instrumentation, and coverage tests (default)"
echo "    unit             - Run unit tests only (shared + androidApp)"
echo "    instrumentation  - Run instrumentation tests (requires device/emulator)"
echo "    coverage         - Run unit tests with coverage reports (JaCoCo)"
echo ""
echo "  Examples:"
echo "    ./extra/scripts/run-tests.sh"
echo "    ./extra/scripts/run-tests.sh unit"
echo "    ./extra/scripts/run-tests.sh instrumentation"
echo "    ./extra/scripts/run-tests.sh coverage"
echo ""
echo "🔍 Debugging Options:"
echo ""
echo "  # Test only shared data layer:"
echo "  ./gradlew :shared:testAndroidHostTest"
echo ""
echo "  # Test only Android app:"
echo "  ./gradlew :androidApp:testDebugUnitTest"
echo ""
echo "  # Run instrumentation tests:"
echo "  ./gradlew :androidApp:connectedDebugAndroidTest"
echo ""
echo "  # Generate coverage reports:"
echo "  ./gradlew :androidApp:testCoverageReport"
echo "  ./gradlew :shared:testCoverageReport"
echo ""

exit $BUILD_STATUS
