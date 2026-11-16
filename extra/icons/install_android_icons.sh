#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
SOURCE_ICON="$SCRIPT_DIR/app-icons/png/app-icon-512.png"
ANDROID_RES="$PROJECT_ROOT/androidApp/src/main/res"

if [[ ! -f "$SOURCE_ICON" ]]; then
    echo "Source icon not found: $SOURCE_ICON" >&2
    echo "Run extra/icons/convert_icons.sh first." >&2
    exit 1
fi

if command -v magick >/dev/null; then
    IMAGE_COMMAND=(magick)
elif command -v convert >/dev/null; then
    IMAGE_COMMAND=(convert)
else
    echo "ImageMagick is required to resize launcher icons." >&2
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
    "${IMAGE_COMMAND[@]}" "$SOURCE_ICON" -resize "${size}x${size}" "$output_dir/ic_launcher.png"
    cp "$output_dir/ic_launcher.png" "$output_dir/ic_launcher_round.png"
    echo "Installed $density launcher icons (${size}x${size})"
done

echo "Legacy launcher icons installed under $ANDROID_RES/mipmap-*/."
echo "Existing adaptive icon resources were left unchanged."
