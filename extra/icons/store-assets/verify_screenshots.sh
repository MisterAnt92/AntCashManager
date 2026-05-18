#!/bin/bash

# Script per verificare e organizzare screenshot
# Verifica che tutti gli screenshot siano presenti nelle lingue corrette

set -e

# Colori
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCREENSHOTS_DIR="$SCRIPT_DIR/screenshots"

# Lingue supportate
LANGUAGES=(it en de fr es)

# Features da catturare
FEATURES=(
    "01_dashboard"
    "02_add_transaction"
    "03_categories"
    "04_charts"
    "05_scan_receipt"
    "06_settings"
    "07_backup_restore"
    "08_dark_mode"
)

echo -e "${BLUE}╔═══════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║ Screenshot Verification Tool                           ║${NC}"
echo -e "${BLUE}╚═══════════════════════════════════════════════════════╝${NC}"
echo ""

# Crea struttura cartelle se non esiste
echo -e "${YELLOW}Creating directory structure...${NC}"
for lang in "${LANGUAGES[@]}"; do
    mkdir -p "$SCREENSHOTS_DIR/$lang/phone"
    mkdir -p "$SCREENSHOTS_DIR/$lang/tablet"
done
echo -e "${GREEN}✓ Directory structure created${NC}"
echo ""

# Verifica screenshot
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}Verification Report${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo ""

TOTAL_EXPECTED=$((${#LANGUAGES[@]} * ${#FEATURES[@]}))
TOTAL_FOUND=0
MISSING_COUNT=0

# Per ogni lingua
for lang in "${LANGUAGES[@]}"; do
    echo -e "${GREEN}Language: $lang${NC}"
    echo "├─ Phone screenshots:"

    lang_count=0

    # Verifica ogni feature
    for feature in "${FEATURES[@]}"; do
        feature_name=$(echo "$feature" | sed 's/_/ /g')

        # Cerca il file (permette varianti nel nome)
        file_found=false
        for file in "$SCREENSHOTS_DIR/$lang/phone/${feature}"*.png; do
            if [ -f "$file" ]; then
                file_found=true
                ((TOTAL_FOUND++))
                ((lang_count++))
                break
            fi
        done

        if [ "$file_found" = true ]; then
            echo -e "│  ${GREEN}✓${NC} $feature_name"
        else
            echo -e "│  ${RED}✗${NC} $feature_name (missing)"
            ((MISSING_COUNT++))
        fi
    done

    # Tablet screenshots (opzionali)
    tablet_count=$(find "$SCREENSHOTS_DIR/$lang/tablet" -name "*.png" 2>/dev/null | wc -l || echo 0)
    if [ "$tablet_count" -gt 0 ]; then
        echo "└─ Tablet screenshots:"
        echo -e "   ${GREEN}✓${NC} $tablet_count tablet screenshot(s)"
    else
        echo "└─ Tablet screenshots:"
        echo -e "   ${YELLOW}⚠${NC} None (optional)"
    fi

    echo -e "   Total: ${BLUE}$lang_count/8${NC} phone screenshots"
    echo ""
done

echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}Summary${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo ""

echo "Expected: $TOTAL_EXPECTED screenshots"
echo "Found: $TOTAL_FOUND screenshots"
echo "Missing: $MISSING_COUNT screenshots"

echo ""

if [ "$MISSING_COUNT" -eq 0 ]; then
    echo -e "${GREEN}✓ All screenshots captured!${NC}"
else
    echo -e "${YELLOW}⚠ Missing screenshots:${NC}"

    for lang in "${LANGUAGES[@]}"; do
        for feature in "${FEATURES[@]}"; do
            file_found=false
            for file in "$SCREENSHOTS_DIR/$lang/phone/${feature}"*.png; do
                if [ -f "$file" ]; then
                    file_found=true
                    break
                fi
            done

            if [ "$file_found" = false ]; then
                echo "  - $lang/$feature"
            fi
        done
    done
fi

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}File Organization${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo ""

# Mostra struttura
echo "Directory structure:"
tree -L 2 "$SCREENSHOTS_DIR" 2>/dev/null || find "$SCREENSHOTS_DIR" -type d | sort | sed 's|^|  |' | head -20

echo ""
echo -e "${YELLOW}Total size:${NC}"
du -sh "$SCREENSHOTS_DIR"

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo ""

# Raccomandazioni
echo -e "${YELLOW}Recommendations:${NC}"
echo ""

if [ "$MISSING_COUNT" -gt 0 ]; then
    echo "1. Capture missing screenshots:"
    echo "   bash capture_screenshots.sh it  # for Italian"
    echo ""
fi

echo "2. Optimize screenshots (optional):"
echo "   bash optimize_screenshots.sh"
echo ""

echo "3. Upload to Google Play Console:"
echo "   - For each language, upload 8 phone screenshots from:"
echo "   - \$SCREENSHOTS_DIR/{language}/phone/"
echo ""

echo "4. Verify on Play Console preview before publishing"
echo ""

# File status report
echo -e "${YELLOW}File Status Report:${NC}"
echo ""

for lang in "${LANGUAGES[@]}"; do
    echo "$lang:"
    phone_count=$(find "$SCREENSHOTS_DIR/$lang/phone" -name "*.png" 2>/dev/null | wc -l || echo 0)
    tablet_count=$(find "$SCREENSHOTS_DIR/$lang/tablet" -name "*.png" 2>/dev/null | wc -l || echo 0)
    total_size=$(du -sh "$SCREENSHOTS_DIR/$lang" 2>/dev/null | cut -f1 || echo "0")

    printf "  Phone:  %2d files | " "$phone_count"
    printf "Tablet:  %2d files | " "$tablet_count"
    printf "Size: %s\n" "$total_size"
done

echo ""

# Exit code basato su completamento
if [ "$MISSING_COUNT" -eq 0 ]; then
    echo -e "${GREEN}✓ Ready for upload to Google Play Console!${NC}"
    exit 0
else
    echo -e "${YELLOW}⚠ Still missing $MISSING_COUNT screenshots${NC}"
    exit 1
fi

