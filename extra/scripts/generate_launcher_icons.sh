#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
SOURCE_SVG="$PROJECT_ROOT/extra/ic_launcher_piggy_bank.svg"
ANDROID_RES="$PROJECT_ROOT/androidApp/src/main/res"

if [[ ! -f "$SOURCE_SVG" ]]; then
    echo "Launcher icon SVG not found: $SOURCE_SVG" >&2
    exit 1
fi

if ! command -v inkscape >/dev/null && ! command -v magick >/dev/null && ! command -v convert >/dev/null; then
    echo "Install Inkscape or ImageMagick to render launcher icons." >&2
    exit 1
fi

declare -A SIZES=(
    [mdpi]=48
    [hdpi]=72
    [xhdpi]=96
    [xxhdpi]=144
    [xxxhdpi]=192
)

for density in "${!SIZES[@]}"; do
    size="${SIZES[$density]}"
    output_dir="$ANDROID_RES/mipmap-$density"
    mkdir -p "$output_dir"

    if command -v inkscape >/dev/null; then
        inkscape "$SOURCE_SVG" --export-filename="$output_dir/ic_launcher.png" \
            --export-width="$size" --export-height="$size"
    elif command -v magick >/dev/null; then
        magick "$SOURCE_SVG" -resize "${size}x${size}" "$output_dir/ic_launcher.png"
    else
        convert "$SOURCE_SVG" -resize "${size}x${size}" "$output_dir/ic_launcher.png"
    fi

    cp "$output_dir/ic_launcher.png" "$output_dir/ic_launcher_round.png"
    echo "Generated $density launcher icons (${size}x${size})"
done

echo "Launcher icons generated under $ANDROID_RES/mipmap-*/."
