#!/bin/bash

# Script di conversione SVG → PNG per risorse AntCashManager
# Converte tutti i file SVG in PNG con le dimensioni corrette per Google Play Store

set -e  # Exit on error

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colori per output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}=== AntCashManager Icon Conversion Script ===${NC}"
echo "Converting SVG files to PNG..."
echo ""

# Controlla se ImageMagick è installato
if ! command -v convert &> /dev/null; then
    echo -e "${RED}ERROR: ImageMagick not installed!${NC}"
    echo "Install with: sudo apt-get install imagemagick"
    exit 1
fi

# Controlla se Inkscape è installato (alternativa)
if ! command -v convert &> /dev/null && ! command -v inkscape &> /dev/null; then
    echo -e "${RED}ERROR: Neither ImageMagick nor Inkscape found!${NC}"
    exit 1
fi

# Funzione per convertire SVG a PNG
convert_svg_to_png() {
    local svg_file="$1"
    local png_file="$2"
    local width="$3"
    local height="$4"

    if [ ! -f "$svg_file" ]; then
        echo -e "${RED}✗ File not found: $svg_file${NC}"
        return 1
    fi

    # Crea directory se non esiste
    mkdir -p "$(dirname "$png_file")"

    # Conversione con ImageMagick
    if convert -density 150 "$svg_file" -resize "${width}x${height}" -background none "$png_file" 2>/dev/null; then
        echo -e "${GREEN}✓ Created: $png_file (${width}x${height})${NC}"
        return 0
    else
        echo -e "${YELLOW}⚠ Warning: Fallback needed for $png_file${NC}"

        # Fallback: Inkscape (se disponibile)
        if command -v inkscape &> /dev/null; then
            if inkscape --export-filename="$png_file" --export-width="$width" --export-height="$height" "$svg_file" 2>/dev/null; then
                echo -e "${GREEN}✓ Created (Inkscape): $png_file${NC}"
                return 0
            fi
        fi

        echo -e "${RED}✗ Failed to convert: $svg_file${NC}"
        return 1
    fi
}

# Contatore
TOTAL=0
SUCCESS=0

# 1. Convert App Icons
echo -e "${YELLOW}Processing App Icons...${NC}"

convert_svg_to_png "app-icons/svg/app-icon-192.svg" "app-icons/png/app-icon-192.png" 192 192 && ((SUCCESS++)) || true
((TOTAL++))

convert_svg_to_png "app-icons/svg/app-icon-512.svg" "app-icons/png/app-icon-512.png" 512 512 && ((SUCCESS++)) || true
((TOTAL++))

# Crea ulteriori risoluzioni da 512px
mkdir -p "app-icons/png/android-res"

if [ -f "app-icons/png/app-icon-512.png" ]; then
    convert "app-icons/png/app-icon-512.png" -resize 192x192 "app-icons/png/android-res/mdpi-app-icon-192.png" 2>/dev/null && echo -e "${GREEN}✓ Created: mdpi (192x192)${NC}" || true
    convert "app-icons/png/app-icon-512.png" -resize 288x288 "app-icons/png/android-res/hdpi-app-icon-288.png" 2>/dev/null && echo -e "${GREEN}✓ Created: hdpi (288x288)${NC}" || true
    convert "app-icons/png/app-icon-512.png" -resize 384x384 "app-icons/png/android-res/xhdpi-app-icon-384.png" 2>/dev/null && echo -e "${GREEN}✓ Created: xhdpi (384x384)${NC}" || true
    convert "app-icons/png/app-icon-512.png" -resize 512x512 "app-icons/png/android-res/xxhdpi-app-icon-512.png" 2>/dev/null && echo -e "${GREEN}✓ Created: xxhdpi (512x512)${NC}" || true
    convert "app-icons/png/app-icon-512.png" -resize 768x768 "app-icons/png/android-res/xxxhdpi-app-icon-768.png" 2>/dev/null && echo -e "${GREEN}✓ Created: xxxhdpi (768x768)${NC}" || true
fi

echo ""
echo -e "${YELLOW}Processing Store Assets...${NC}"

# 2. Convert Feature Graphics
convert_svg_to_png "store-assets/feature-graphics/feature-graphic.svg" "store-assets/feature-graphics/feature-graphic-1024x500.png" 1024 500 && ((SUCCESS++)) || true
((TOTAL++))

# 3. Convert Promo Graphics
convert_svg_to_png "store-assets/promo-graphics/promo-180x120.svg" "store-assets/promo-graphics/promo-180x120.png" 180 120 && ((SUCCESS++)) || true
((TOTAL++))

echo ""
echo -e "${YELLOW}Creating additional formats...${NC}"

# Crea JPEG versions (più piccoli per upload)
mkdir -p "store-assets/feature-graphics/jpeg"
if [ -f "store-assets/feature-graphics/feature-graphic-1024x500.png" ]; then
    convert "store-assets/feature-graphics/feature-graphic-1024x500.png" -quality 90 "store-assets/feature-graphics/jpeg/feature-graphic-1024x500.jpg" 2>/dev/null && echo -e "${GREEN}✓ Created: feature-graphic JPEG${NC}" || true
fi

mkdir -p "store-assets/promo-graphics/jpeg"
if [ -f "store-assets/promo-graphics/promo-180x120.png" ]; then
    convert "store-assets/promo-graphics/promo-180x120.png" -quality 90 "store-assets/promo-graphics/jpeg/promo-180x120.jpg" 2>/dev/null && echo -e "${GREEN}✓ Created: promo-graphic JPEG${NC}" || true
fi

echo ""
echo -e "${YELLOW}=== Conversion Summary ===${NC}"
echo -e "Total: ${TOTAL} conversions"
echo -e "Success: ${SUCCESS} conversions"

if [ "$SUCCESS" -eq "$TOTAL" ]; then
    echo -e "${GREEN}✓ All conversions completed successfully!${NC}"
    exit 0
else
    echo -e "${YELLOW}⚠ Some conversions failed. Check output above.${NC}"
    exit 1
fi

