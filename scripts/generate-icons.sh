#!/bin/bash
set -e

SVG="icon.svg"
OUTPUT_DIR="../assets"
ICONSET_DIR="icon.iconset"
ICNS_FILE="$OUTPUT_DIR/icon.icns"

mkdir -p "$ICONSET_DIR"
mkdir -p "$OUTPUT_DIR"

# Convert SVG to required PNG sizes
for size in 16 32 64 128 256 512 1024; do
    convert -alpha on -background none -resize ${size}x${size} "$SVG" "$ICONSET_DIR/icon_${size}x${size}.png"
done

# Create .icns
png2icns "$ICNS_FILE" "$ICONSET_DIR"/icon_*.png

# Create .ico (Windows)
convert -background transparent "$SVG" -define icon:auto-resize=256,128,64,48,32,16 "$OUTPUT_DIR/icon.ico"

# Create .png (Linux)
convert -alpha on -background none -resize 256x256 "$SVG" "$OUTPUT_DIR/icon.png"

# Clean up
rm -r "$ICONSET_DIR"