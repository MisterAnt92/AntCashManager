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

# ─── Riepilogo per modulo ───────────────────────────────────────────────────────
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

# ─── Instrumentation tests ─────────────────────────────────────────────────────
INSTRUMENTATION_XML_DIR="$PROJECT_ROOT/androidApp/build/outputs/androidTest-results/connected/debug"
INSTRUMENTATION_HTML="$PROJECT_ROOT/androidApp/build/reports/androidTests/connected/debug/index.html"

if [ -d "$INSTRUMENTATION_XML_DIR" ] || [ -f "$INSTRUMENTATION_HTML" ]; then
    echo "  📱 Instrumentation Tests"
    echo ""
    print_module_summary ":androidApp:connectedDebugAndroidTest" "$INSTRUMENTATION_XML_DIR" "$INSTRUMENTATION_HTML"
    echo ""
fi

# ─── Coverage HTML reports (JaCoCo) ────────────────────────────────────────────
JACOCO_ANDROID="$PROJECT_ROOT/androidApp/build/reports/jacoco/jacocoTestDebugUnitTestReport/html/index.html"
JACOCO_SHARED="$PROJECT_ROOT/shared/build/reports/jacoco/testAndroidHostTestCoverage/html/index.html"

if [ -f "$JACOCO_ANDROID" ] || [ -f "$JACOCO_SHARED" ]; then
    echo "  📈 Coverage Reports (JaCoCo)"
    echo ""
    if [ -f "$JACOCO_ANDROID" ]; then
        printf "    📊 androidApp  →  file://%s\n" "$JACOCO_ANDROID"
    fi
    if [ -f "$JACOCO_SHARED" ]; then
        printf "    📊 shared      →  file://%s\n" "$JACOCO_SHARED"
    fi
    echo ""
fi

# ─── Esito finale ───────────────────────────────────────────────────────────────
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
