#!/bin/bash

# Script per built, verificare e preparare i debug symbols per il caricamento su Play Console
# Uso: bash build_and_prepare_symbols.sh [internal|closed-testing|production]

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUNDLE_OUTPUT="$PROJECT_ROOT/androidApp/build/outputs/bundle/release/app-release.aab"
SYMBOLS_OUTPUT="$PROJECT_ROOT/build/symbols"
BUILD_LOG="$PROJECT_ROOT/build_symbols.log"

echo -e "${BLUE}=== AntCashManager Debug Symbols Build ===${NC}"
echo "Workspace: $PROJECT_ROOT"
echo "Output: $BUNDLE_OUTPUT"
echo ""

# ============================================================================
# Fase 1: Clean & Build Release Bundle
# ============================================================================

echo -e "${YELLOW}[1/4] Cleaning previous builds...${NC}"
./gradlew clean --quiet || true

echo -e "${YELLOW}[2/4] Building release bundle (this may take 2-3 minutes)...${NC}"
if ./gradlew bundleRelease --no-daemon > "$BUILD_LOG" 2>&1; then
    echo -e "${GREEN}✓ Bundle built successfully${NC}"
else
    echo -e "${RED}✗ Build failed. See $BUILD_LOG${NC}"
    cat "$BUILD_LOG" | tail -50
    exit 1
fi

# ============================================================================
# Fase 2: Verifica Bundle e Estrae Informazioni
# ============================================================================

echo -e "${YELLOW}[3/4] Verifying bundle and extracting debug info...${NC}"

if [ ! -f "$BUNDLE_OUTPUT" ]; then
    echo -e "${RED}✗ Bundle not found: $BUNDLE_OUTPUT${NC}"
    exit 1
fi

BUNDLE_SIZE=$(du -h "$BUNDLE_OUTPUT" | awk '{print $1}')
echo -e "${GREEN}✓ Bundle file found ($BUNDLE_SIZE)${NC}"

# Estrai il bundle temporaneamente per controllare i simboli
TEMP_EXTRACT=$(mktemp -d)
unzip -q "$BUNDLE_OUTPUT" -d "$TEMP_EXTRACT" 2>/dev/null || true

echo -e "${YELLOW}   Checking for native libraries...${NC}"
NATIVE_LIBS=$(find "$TEMP_EXTRACT/base/lib" -name "*.so" 2>/dev/null | wc -l)
if [ "$NATIVE_LIBS" -gt 0 ]; then
    echo -e "${GREEN}✓ Found $NATIVE_LIBS native libraries${NC}"
    find "$TEMP_EXTRACT/base/lib" -name "*.so" | head -5 | xargs -I {} basename {} | sed 's/^/     /'
else
    echo -e "${RED}⚠ No native libraries found${NC}"
fi

# Verifica ProGuard mapping
MAPPING_FILE="$PROJECT_ROOT/androidApp/build/outputs/mapping/release/mapping.txt"
if [ -f "$MAPPING_FILE" ]; then
    echo -e "${GREEN}✓ ProGuard mapping file found${NC}"
else
    echo -e "${YELLOW}⚠ ProGuard mapping file not found (expected at build time)${NC}"
fi

rm -rf "$TEMP_EXTRACT"

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
  See: $PROJECT_ROOT/DEBUG_SYMBOLS_GUIDE.md
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

