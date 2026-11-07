#!/bin/bash

# Build a release app bundle and prepare its Play Console upload notes.
# Usage: ./extra/scripts/build_and_prepare_symbols.sh [full|lite]

set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
FLAVOR="${1:-full}"
case "$FLAVOR" in
    full|lite) ;;
    *)
        echo "Usage: $0 [full|lite]" >&2
        exit 2
        ;;
esac

SYMBOLS_OUTPUT="$PROJECT_ROOT/build/symbols"
BUILD_LOG="$SYMBOLS_OUTPUT/build_symbols.log"
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"

mkdir -p "$SYMBOLS_OUTPUT"

echo -e "${BLUE}=== AntCashManager Debug Symbols Build ===${NC}"
echo "Workspace: $PROJECT_ROOT"
echo "Flavor: $FLAVOR"
echo ""

# ============================================================================
# Fase 1: Clean & Build Release Bundle
# ============================================================================

echo -e "${YELLOW}[1/4] Cleaning previous builds...${NC}"
cd "$PROJECT_ROOT"
./gradlew clean --quiet

echo -e "${YELLOW}[2/4] Building release bundle (this may take 2-3 minutes)...${NC}"
TASK_FLAVOR="${FLAVOR^}"
if ./gradlew ":androidApp:bundle${TASK_FLAVOR}Release" --no-daemon > "$BUILD_LOG" 2>&1; then
    echo -e "${GREEN}✓ Bundle built successfully${NC}"
else
    echo -e "${RED}✗ Build failed. See $BUILD_LOG${NC}"
    tail -50 "$BUILD_LOG"
    exit 1
fi

# ============================================================================
# Fase 2: Verifica Bundle e Estrae Informazioni
# ============================================================================

echo -e "${YELLOW}[3/4] Verifying bundle and extracting debug info...${NC}"

OUTPUT_DIR="$PROJECT_ROOT/androidApp/build/outputs/bundle/${FLAVOR}Release"
if [ ! -d "$OUTPUT_DIR" ]; then
    echo -e "${RED}✗ Bundle output directory not found: $OUTPUT_DIR${NC}"
    exit 1
fi
BUNDLE_OUTPUT="$(find "$OUTPUT_DIR" -maxdepth 1 -type f -name '*.aab' -print -quit)"
if [ ! -f "$BUNDLE_OUTPUT" ]; then
    echo -e "${RED}✗ No app bundle found in: $OUTPUT_DIR${NC}"
    exit 1
fi

BUNDLE_SIZE=$(du -h "$BUNDLE_OUTPUT" | awk '{print $1}')
echo -e "${GREEN}✓ Bundle file found ($BUNDLE_SIZE)${NC}"

# Estrai il bundle temporaneamente per controllare i simboli
TEMP_EXTRACT=$(mktemp -d)
trap 'rm -rf "$TEMP_EXTRACT"' EXIT
if ! unzip -q "$BUNDLE_OUTPUT" -d "$TEMP_EXTRACT"; then
    echo -e "${RED}✗ Could not extract app bundle: $BUNDLE_OUTPUT${NC}" >&2
    exit 1
fi

echo -e "${YELLOW}   Checking for native libraries...${NC}"
NATIVE_LIBS=$(find "$TEMP_EXTRACT" -path '*/lib/*.so' -type f | wc -l)
if [ "$NATIVE_LIBS" -gt 0 ]; then
    echo -e "${GREEN}✓ Found $NATIVE_LIBS native libraries${NC}"
    find "$TEMP_EXTRACT" -path '*/lib/*.so' -type f -print | sed -n '1,5p' | xargs -r -n 1 basename | sed 's/^/     /'
else
    echo -e "${RED}⚠ No native libraries found${NC}"
fi

# Verifica ProGuard mapping
MAPPING_FILE="$PROJECT_ROOT/androidApp/build/outputs/mapping/${FLAVOR}Release/mapping.txt"
if [ -f "$MAPPING_FILE" ]; then
    echo -e "${GREEN}✓ ProGuard mapping file found${NC}"
else
    echo -e "${YELLOW}⚠ ProGuard mapping file not found (expected at build time)${NC}"
fi

# ============================================================================
# Fase 3: Genera Informazioni per l'Upload
# ============================================================================

echo -e "${YELLOW}[4/4] Generating upload instructions...${NC}"

mkdir -p "$SYMBOLS_OUTPUT"

# Crea il file di istruzioni
INSTRUCTIONS="$SYMBOLS_OUTPUT/UPLOAD_INSTRUCTIONS.txt"
cat > "$INSTRUCTIONS" <<'ENDFILE'
╔═══════════════════════════════════════════════════════════════════════════╗
║                    DEBUG SYMBOLS UPLOAD TO PLAY CONSOLE                   ║
╚═══════════════════════════════════════════════════════════════════════════╝

✅ BUNDLE READY: app-release.aab

📋 STEPS TO UPLOAD DEBUG SYMBOLS:

1. ACCEDI A GOOGLE PLAY CONSOLE
   → https://play.google.com/console
   → Seleziona AntCashManager

2. VAI A RELEASE
   → Release → Release Overview
   → Clicca su "Create New Release" oppure modifica release esistente

3. UPLOAD DEL BUNDLE
   → Carica: app-release.aab
   → Google Play Console estrae automaticamente i debug symbols

4. VERIFICA
   → Attendi 5-10 minuti per l'elaborazione
   → Vai a Quality → Crashes and ANRs
   → I crash dovranno mostrare stack trace DECODIFICATI

💡 AUTO-EXTRACTION:
   Google Play Console estrarrà automaticamente i debug symbols dal bundle.
   Non serve fare nulla di manuale.

❓ SE NON VEDI I SIMBOLI:
   → Verifica che il bundle sia stato uploadato completamente
   → Controlla che il version code sia diverso dalla release precedente
   → Attendi 15 minuti e ricarica la pagina

ENDFILE

echo -e "${GREEN}✓ Instructions saved to: $INSTRUCTIONS${NC}"

# Crea il file di riepilogo
SUMMARY="$SYMBOLS_OUTPUT/BUILD_SUMMARY.txt"
cat > "$SUMMARY" <<EOF
=== AntCashManager Debug Symbols Build Summary ===
Date: $(date)
Bundle: $BUNDLE_OUTPUT
Size: $BUNDLE_SIZE
Native Libraries Found: $NATIVE_LIBS

Next Step:
  Upload app-release.aab to Google Play Console
  Debug symbols will be extracted automatically

Documentation:
  See: $PROJECT_ROOT/.github/docs/deployment/DEBUG_SYMBOLS_GUIDE.md
EOF

echo -e "${BLUE}=== Build Complete ===${NC}"
echo ""
echo -e "${GREEN}✓ App Bundle ready:${NC}"
echo "  $BUNDLE_OUTPUT"
echo ""
echo -e "${GREEN}✓ Instructions:${NC}"
echo "  $INSTRUCTIONS"
echo ""
echo "Next: Upload app-release.aab to Play Console"
echo ""
