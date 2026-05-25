#!/bin/bash

# Script per catturare screenshot dell'app in diverse lingue
# Automatizza il processo di change lingua → cattura screenshot

set -e

# Colori
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Variabili
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$(dirname "$(dirname "$SCRIPT_DIR")")")"
SCREENSHOTS_DIR="$SCRIPT_DIR/screenshots"

# Mapping lingue
declare -A LOCALE_MAP=(
    [it]="it_IT"
    [en]="en_US"
    [de]="de_DE"
    [fr]="fr_FR"
    [es]="es_ES"
)

declare -A LANGUAGE_NAMES=(
    [it]="Italiano"
    [en]="English"
    [de]="Deutsch"
    [fr]="Français"
    [es]="Español"
)

# Sequence di feature da catturare
FEATURES=(
    "dashboard:Dashboard"
    "add_transaction:Aggiungi Transazione"
    "categories:Categorie"
    "charts:Grafici"
    "scan_receipt:Scansione Ricevute"
    "settings:Impostazioni"
    "backup_restore:Backup e Restore"
    "dark_mode:Dark Mode"
)

# Package e activity dell'app
APP_PACKAGE="com.sformica.ant_cashmanager"
APP_ACTIVITY="com.antcashmanager.android.MainActivity"

echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║ AntCashManager - Screenshot Capture Script (Multilang) ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""

# Verifica che ADB sia disponibile
if ! command -v adb &> /dev/null; then
    echo -e "${RED}ERROR: adb not found in PATH${NC}"
    echo "Install Android SDK Platform Tools: https://developer.android.com/studio/command-line/adb"
    exit 1
fi

# Verifica device connesso
DEVICE_COUNT=$(adb devices | grep -c "device$" || true)
if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo -e "${RED}ERROR: No Android device or emulator connected${NC}"
    echo ""
    echo "To connect an emulator:"
    echo "  emulator -avd Pixel_6_API_30"
    echo ""
    echo "To connect a device:"
    echo "  - Enable USB Debugging in Settings"
    echo "  - Connect via USB"
    echo "  - Run: adb devices"
    exit 1
fi

# Ottieni primo device
DEVICE=$(adb devices | grep "device$" | head -1 | awk '{print $1}')
echo -e "${GREEN}✓ Device found: $DEVICE${NC}"
echo ""

# Funzione per impostare la lingua
set_locale() {
    local lang=$1
    local locale=${LOCALE_MAP[$lang]}

    echo -e "${YELLOW}→ Setting language to: ${LANGUAGE_NAMES[$lang]} ($locale)${NC}"

    # Metodo 1: Usando settings
    adb -s "$DEVICE" shell settings put global prefs_locale "$locale" 2>/dev/null || true

    # Metodo 2: Usando adb setprop
    adb -s "$DEVICE" shell setprop persist.sys.locale "$locale" || true

    # Attendi che la lingua sia applicata
    sleep 2
}

# Funzione per catturare screenshot
capture_screenshot() {
    local lang=$1
    local feature=$2
    local num=$3

    local dest_dir="$SCREENSHOTS_DIR/$lang/phone"
    mkdir -p "$dest_dir"

    local filename="${num}_${feature}.png"
    local dest_file="$dest_dir/$filename"

    echo -e "${YELLOW}  Capturing: $filename${NC}"

    # Cattura screenshot su device
    adb -s "$DEVICE" shell screencap -p "/sdcard/$filename" || {
        echo -e "${RED}    ✗ Failed to capture screenshot${NC}"
        return 1
    }

    # Pull da device
    adb -s "$DEVICE" pull "/sdcard/$filename" "$dest_file" > /dev/null 2>&1 || {
        echo -e "${RED}    ✗ Failed to pull screenshot${NC}"
        return 1
    }

    # Cleanup
    adb -s "$DEVICE" shell rm "/sdcard/$filename" 2>/dev/null || true

    # Verifica file
    if [ -f "$dest_file" ]; then
        local size=$(du -h "$dest_file" | cut -f1)
        echo -e "${GREEN}    ✓ Saved: $dest_file ($size)${NC}"
        return 0
    else
        echo -e "${RED}    ✗ File not found: $dest_file${NC}"
        return 1
    fi
}

# Funzione per avviare l'app
launch_app() {
    echo -e "${YELLOW}  Launching app...${NC}"
    adb -s "$DEVICE" shell am start -n "$APP_PACKAGE/$APP_ACTIVITY" 2>/dev/null || {
        echo -e "${RED}    ✗ Failed to launch app${NC}"
        return 1
    }
    sleep 3  # Attendi caricamento
}

# Funzione main per catturare screenshot per una lingua
capture_screenshots_for_language() {
    local lang=$1

    echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}Capturing screenshots in ${LANGUAGE_NAMES[$lang]} ($lang)${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
    echo ""

    # Imposta lingua
    set_locale "$lang"

    # Avvia app
    launch_app

    # Cattura screenshot per ogni feature
    local counter=1
    for feature_spec in "${FEATURES[@]}"; do
        IFS=':' read -r feature_name feature_label <<< "$feature_spec"

        echo -e "${YELLOW}[${counter}/8] ${feature_label}${NC}"

        # Formatta numero con zero padding
        local num=$(printf "%02d" "$counter")

        # Cattura screenshot
        capture_screenshot "$lang" "$feature_name" "$num" || {
            echo -e "${YELLOW}  ⚠ Skipping manual navigation to next feature...${NC}"
        }

        # Attendi input per navigazione
        if [ "$counter" -lt 8 ]; then
            echo ""
            echo -e "${YELLOW}⏳ Navigate to next feature and press ENTER...${NC}"
            read -r
        fi

        ((counter++))
    done

    echo ""
    echo -e "${GREEN}✓ Screenshot capture complete for $lang${NC}"
    echo ""
}

# Parsing argomenti
if [ $# -eq 0 ]; then
    echo "Usage: $0 <language> [--all]"
    echo ""
    echo "Languages:"
    echo "  it - Italiano"
    echo "  en - English"
    echo "  de - Deutsch"
    echo "  fr - Français"
    echo "  es - Español"
    echo "  --all - Capture for all languages"
    echo ""
    echo "Examples:"
    echo "  $0 it           # Capture screenshots in Italian"
    echo "  $0 en           # Capture screenshots in English"
    echo "  $0 --all        # Capture for all 5 languages"
    exit 1
fi

# Determina quali lingue catturare
LANGUAGES_TO_CAPTURE=()

if [ "$1" = "--all" ]; then
    LANGUAGES_TO_CAPTURE=(it en de fr es)
    echo -e "${YELLOW}Mode: Capture all languages${NC}"
    echo ""
else
    LANGUAGES_TO_CAPTURE=("$1")
    if [ -z "${LOCALE_MAP[$1]}" ]; then
        echo -e "${RED}ERROR: Unknown language: $1${NC}"
        exit 1
    fi
fi

# Cattura screenshot
TOTAL_LANGS=${#LANGUAGES_TO_CAPTURE[@]}
for i in "${!LANGUAGES_TO_CAPTURE[@]}"; do
    lang=${LANGUAGES_TO_CAPTURE[$i]}
    current=$((i + 1))

    capture_screenshots_for_language "$lang"

    if [ "$current" -lt "$TOTAL_LANGS" ]; then
        echo ""
        echo -e "${YELLOW}Press ENTER to continue to next language...${NC}"
        read -r
    fi
done

# Riepilogo
echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║ Screenshot Capture Summary                              ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""

# Conta screenshot catturati
for lang in "${LANGUAGES_TO_CAPTURE[@]}"; do
    count=$(find "$SCREENSHOTS_DIR/$lang/phone" -name "*.png" 2>/dev/null | wc -l || echo 0)
    if [ "$count" -gt 0 ]; then
        echo -e "${GREEN}✓ $lang: $count screenshots${NC}"
    else
        echo -e "${YELLOW}⚠ $lang: 0 screenshots${NC}"
    fi
done

echo ""
echo -e "${YELLOW}Screenshots saved to:${NC}"
echo "  $SCREENSHOTS_DIR"
echo ""

# Suggerimenti
echo -e "${YELLOW}Next steps:${NC}"
echo "1. Review screenshots in file explorer"
echo "2. Run: bash optimize_screenshots.sh (optional compression)"
echo "3. Upload to Google Play Console:"
echo "   - Vai a Store Listing per ogni lingua"
echo "   - Carica screenshots da: $SCREENSHOTS_DIR/{lingua}/phone/"
echo ""

echo -e "${GREEN}✓ Script completed successfully!${NC}"

