#!/bin/bash

# Script per copiare gli icon nelle cartelle Android resources
# Copia gli icon generati nella struttura standard mipmap per Android

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$(dirname "$SCRIPT_DIR")")"

# Colori
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${YELLOW}=== AntCashManager Android Icon Installer ===${NC}"
echo ""

# Controlla che i PNG siano stati generati
if [ ! -f "$SCRIPT_DIR/app-icons/png/app-icon-512.png" ]; then
    echo -e "${RED}ERROR: app-icon-512.png not found!${NC}"
    echo "Run 'bash convert_icons.sh' first to generate PNG files."
    exit 1
fi

# Android resources directory
ANDROID_RES="$PROJECT_ROOT/androidApp/src/main/res"

echo -e "${YELLOW}Installing icons to Android resources...${NC}"
echo "Target: $ANDROID_RES"
echo ""

# Funzione per installare icon
install_icon() {
    local png_file="$1"
    local width="$2"
    local dpi="$3"

    local filename="$(basename "$png_file")"
    local dest_dir="$ANDROID_RES/mipmap-$dpi"
    local dest_file="$dest_dir/ic_launcher.png"

    mkdir -p "$dest_dir"

    # Ridimensiona se necessario
    convert "$png_file" -resize "${width}x${width}" "$dest_file" 2>/dev/null

    if [ -f "$dest_file" ]; then
        echo -e "${GREEN}✓ Installed: $dpi (${width}x${width})${NC}"
        return 0
    else
        echo -e "${RED}✗ Failed: $dpi${NC}"
        return 1
    fi
}

# Installa per le diverse densità DPI Android
install_icon "$SCRIPT_DIR/app-icons/png/app-icon-512.png" 192 mdpi
install_icon "$SCRIPT_DIR/app-icons/png/app-icon-512.png" 288 hdpi
install_icon "$SCRIPT_DIR/app-icons/png/app-icon-512.png" 384 xhdpi
install_icon "$SCRIPT_DIR/app-icons/png/app-icon-512.png" 512 xxhdpi
install_icon "$SCRIPT_DIR/app-icons/png/app-icon-512.png" 768 xxxhdpi

echo ""
echo -e "${YELLOW}Creating adaptive icon (if supported)...${NC}"

# Crea adaptive icon (Android 8.0+)
ADAPTIVE_DIR="$ANDROID_RES/mipmap-anydpi-v26"
mkdir -p "$ADAPTIVE_DIR"

cat > "$ADAPTIVE_DIR/ic_launcher.xml" << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
</adaptive-icon>
EOF

cat > "$ADAPTIVE_DIR/ic_launcher_round.xml" << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
</adaptive-icon>
EOF

echo -e "${GREEN}✓ Adaptive icon XML created${NC}"

# Crea il colore di sfondo
VALUES_DIR="$ANDROID_RES/values"
mkdir -p "$VALUES_DIR"

# Aggiungi il colore se non esiste
if ! grep -q "ic_launcher_background" "$VALUES_DIR/colors.xml" 2>/dev/null; then
    cat >> "$VALUES_DIR/colors.xml" << 'EOF'
    <color name="ic_launcher_background">#FFE0E6</color>
    <color name="ic_launcher_foreground">#FF8A95</color>
EOF
    echo -e "${GREEN}✓ Added launcher colors to colors.xml${NC}"
else
    echo -e "${YELLOW}⚠ Launcher colors already exist in colors.xml${NC}"
fi

echo ""
echo -e "${YELLOW}Creating round icon variants...${NC}"

# Crea icone rotonde per launcher che le supportano
ROUND_DIR="$ANDROID_RES/mipmap-mdpi"
mkdir -p "$ROUND_DIR"

convert "$SCRIPT_DIR/app-icons/png/app-icon-512.png" \
    -resize 192x192 \
    \( +clone -alpha extract -morphology EdgeOut Diamond:1 -negate -morphology Dilate Diamond:1:2 -negate \) \
    -compose CopyOpacity -composite "$ROUND_DIR/ic_launcher_round.png" 2>/dev/null && \
    echo -e "${GREEN}✓ Created round icon (mdpi)${NC}" || \
    cp "$SCRIPT_DIR/app-icons/png/app-icon-512.png" "$ROUND_DIR/ic_launcher_round.png" && \
    echo -e "${YELLOW}⚠ Using standard icon as round (EdgeOut not available)${NC}"

echo ""
echo -e "${GREEN}=== Installation Complete ===${NC}"
echo ""
echo "Icon files installed to:"
echo "  - $ANDROID_RES/mipmap-*/ic_launcher.png"
echo "  - $ANDROID_RES/mipmap-anydpi-v26/ (Adaptive icon configs)"
echo ""
echo "Your AndroidManifest.xml should have:"
echo '  android:icon="@mipmap/ic_launcher"'
echo ""

# Verifica che gli icon siano stati installati
ICON_COUNT=$(find "$ANDROID_RES/mipmap-"* -name "ic_launcher.png" 2>/dev/null | wc -l)
if [ "$ICON_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✓ Successfully installed $ICON_COUNT icon variants${NC}"
    echo ""
    echo "Next steps:"
    echo "  1. Rebuild the app: ./gradlew :androidApp:compileDebugKotlin"
    echo "  2. Verify icons in emulator/device"
    echo "  3. Test icon appearance in home screen"
else
    echo -e "${RED}✗ Warning: No icon files found after installation${NC}"
    exit 1
fi

