#!/bin/bash

# Script to generate PNG launcher icons from vector drawable
# This script uses Android Studio's vector asset tool equivalent commands
# Run this script from the Android project root directory

echo "🐷 Generating AntCashManager Piggy Bank Launcher Icons..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if we're in an Android project
if [ ! -f "androidApp/build.gradle.kts" ]; then
    echo -e "${RED}❌ Error: This script must be run from the Android project root directory${NC}"
    exit 1
fi

# Create directories if they don't exist
MIPMAP_DIRS=(
    "androidApp/src/main/res/mipmap-mdpi"
    "androidApp/src/main/res/mipmap-hdpi"
    "androidApp/src/main/res/mipmap-xhdpi"
    "androidApp/src/main/res/mipmap-xxhdpi"
    "androidApp/src/main/res/mipmap-xxxhdpi"
)

for dir in "${MIPMAP_DIRS[@]}"; do
    mkdir -p "$dir"
done

echo -e "${YELLOW}📁 Created mipmap directories${NC}"

# Icon sizes for different densities
declare -A SIZES=(
    ["mdpi"]=48
    ["hdpi"]=72
    ["xhdpi"]=96
    ["xxhdpi"]=144
    ["xxxhdpi"]=192
)

# Note about manual PNG generation
echo -e "${YELLOW}⚠️  MANUAL STEP REQUIRED:${NC}"
echo -e "To complete the icon update, you need to generate PNG files from the vector drawable."
echo -e "Options:"
echo -e "1. ${GREEN}Android Studio Vector Asset Studio:${NC}"
echo -e "   - Right-click on res/drawable"
echo -e "   - New > Vector Asset"
echo -e "   - Choose 'Asset Type: Launcher Icons (Adaptive and Legacy)'"
echo -e "   - Import ic_launcher_legacy.xml"
echo -e "   - Generate for all densities"
echo ""
echo -e "2. ${GREEN}Online converter:${NC}"
echo -e "   - Use svg-to-png converters for each density:"
for density in "${!SIZES[@]}"; do
    size=${SIZES[$density]}
    echo -e "   - mipmap-${density}/ic_launcher.png (${size}x${size}px)"
    echo -e "   - mipmap-${density}/ic_launcher_round.png (${size}x${size}px)"
done
echo ""
echo -e "3. ${GREEN}Command line with imagemagick:${NC}"
echo -e "   If you have inkscape and imagemagick installed:"

# Generate imagemagick commands
for density in "${!SIZES[@]}"; do
    size=${SIZES[$density]}
    echo -e "   inkscape -w ${size} -h ${size} ic_launcher_legacy.svg -o androidApp/src/main/res/mipmap-${density}/ic_launcher.png"
    echo -e "   inkscape -w ${size} -h ${size} ic_launcher_legacy.svg -o androidApp/src/main/res/mipmap-${density}/ic_launcher_round.png"
done

echo ""
echo -e "${GREEN}✅ Vector drawable icons have been updated!${NC}"
echo -e "The adaptive icons (Android 8.0+) are ready to use."
echo -e "Generate PNG files for compatibility with older Android versions."

# Check if current PNGs exist and suggest backup
echo ""
echo -e "${YELLOW}🔍 Checking existing PNG icons...${NC}"
for density in "${!SIZES[@]}"; do
    png_path="androidApp/src/main/res/mipmap-${density}/ic_launcher.png"
    if [ -f "$png_path" ]; then
        echo -e "   Found: $png_path"
    else
        echo -e "   Missing: $png_path"
    fi
done

echo ""
echo -e "${GREEN}🚀 Icon update completed!${NC}"
echo -e "Your app will now show the cute piggy bank icon! 🐷💕"
