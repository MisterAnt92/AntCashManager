#!/bin/bash

# Script per ottimizzare screenshot
# Ridimensiona, comprime e crea versioni per diverse risoluzioni

set -e

# Colori
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCREENSHOTS_DIR="$SCRIPT_DIR/screenshots"

echo -e "${BLUE}╔═══════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║ Screenshot Optimization Tool                           ║${NC}"
echo -e "${BLUE}╚═══════════════════════════════════════════════════════╝${NC}"
echo ""

# Verifica ImageMagick
if ! command -v convert &> /dev/null; then
    echo -e "${RED}ERROR: ImageMagick not installed${NC}"
    echo "Install with: sudo apt-get install imagemagick"
    exit 1
fi

# Funzione per ottimizzare un file
optimize_screenshot() {
    local input="$1"
    local lang="$2"

    if [ ! -f "$input" ]; then
        echo -e "${RED}✗ File not found: $input${NC}"
        return 1
    fi

    local filename=$(basename "$input")
    local dirname=$(dirname "$input")

    # Backup originale
    local backup="${dirname}/.originals/$filename"
    mkdir -p "${dirname}/.originals"
    cp "$input" "$backup"

    # Ottimizza: rimpicciolisci se troppo grande, comprimi
    # Phone screenshots: 1080x1920 px
    # Se il file è più grande, rimpicciolisci mantenendo aspect ratio

    echo -e "${YELLOW}  Optimizing: $filename${NC}"

    # Ridimensiona se necessario (max 1200 px larghezza)
    local width=$(identify -format "%w" "$input")
    if [ "$width" -gt 1200 ]; then
        convert "$input" -resize 1200x2000 "$input"
        echo -e "${BLUE}    → Resized from ${width}px${NC}"
    fi

    # Comprimi JPEG quality (per ridurre dimensione file)
    convert "$input" -quality 85 "$input"
    echo -e "${GREEN}    ✓ Compressed${NC}"
}

# Funzione per creare versione inline
create_inline_version() {
    local input="$1"
    local dir=$(dirname "$input")

    if [ ! -f "$input" ]; then
        return 1
    fi

    # Crea versione low-res per preview web (thumb)
    local thumb="${dir}/thumb_$(basename "$input")"
    convert "$input" -resize 400x600 "$thumb"
    echo -e "${GREEN}    ✓ Created thumb: $(basename "$thumb")${NC}"
}

echo -e "${YELLOW}Scanning for screenshots...${NC}"
echo ""

# Conta screenshot
total_screenshots=$(find "$SCREENSHOTS_DIR" -name "*.png" -type f | wc -l || echo 0)
echo -e "${BLUE}Found: $total_screenshots screenshots${NC}"
echo ""

if [ "$total_screenshots" -eq 0 ]; then
    echo -e "${YELLOW}⚠ No screenshots found in: $SCREENSHOTS_DIR${NC}"
    echo "Capture screenshots first using: bash capture_screenshots.sh"
    exit 1
fi

# Elabora per lingua
for lang_dir in "$SCREENSHOTS_DIR"/*/; do
    if [ ! -d "$lang_dir" ]; then
        continue
    fi

    lang=$(basename "$lang_dir")
    echo -e "${GREEN}Processing language: $lang${NC}"

    # Elabora phone screenshots
    if [ -d "$lang_dir/phone" ]; then
        echo -e "${YELLOW}  Phone screenshots:${NC}"
        for screenshot in "$lang_dir/phone"/*.png; do
            if [ -f "$screenshot" ]; then
                optimize_screenshot "$screenshot" "$lang"
                # create_inline_version "$screenshot"
            fi
        done
    fi

    # Elabora tablet screenshots (se presenti)
    if [ -d "$lang_dir/tablet" ]; then
        echo -e "${YELLOW}  Tablet screenshots:${NC}"
        for screenshot in "$lang_dir/tablet"/*.png; do
            if [ -f "$screenshot" ]; then
                optimize_screenshot "$screenshot" "$lang"
            fi
        done
    fi

    echo ""
done

# Statistiche finali
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}Optimization Summary${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo ""

total_size=$(du -sh "$SCREENSHOTS_DIR" | cut -f1)
echo -e "${GREEN}✓ Total size: $total_size${NC}"
echo ""

# Per lingua
for lang_dir in "$SCREENSHOTS_DIR"/*/; do
    if [ ! -d "$lang_dir" ]; then
        continue
    fi

    lang=$(basename "$lang_dir")
    count=$(find "$lang_dir" -name "*.png" -type f | wc -l || echo 0)
    size=$(du -sh "$lang_dir" | cut -f1)

    printf "  %-5s: %2d screenshots | %s\n" "$lang" "$count" "$size"
done

echo ""
echo -e "${YELLOW}Backup of original screenshots:${NC}"
for lang_dir in "$SCREENSHOTS_DIR"/*/; do
    if [ -d "${lang_dir}/.originals" ]; then
        lang=$(basename "$lang_dir")
        echo -e "  ${lang}: ${lang_dir}/.originals/"
    fi
done

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo ""

echo -e "${YELLOW}Next steps:${NC}"
echo "1. Review optimized screenshots:"
echo "   open $SCREENSHOTS_DIR"
echo ""
echo "2. Upload to Google Play Console:"
echo "   - Language: Italiano"
echo "   - Path: $SCREENSHOTS_DIR/it/phone/"
echo ""
echo "3. Repeat for other languages:"
echo "   - en (English)"
echo "   - de (Deutsch)"
echo "   - fr (Français)"
echo "   - es (Español)"
echo ""

echo -e "${GREEN}✓ Optimization complete!${NC}"

