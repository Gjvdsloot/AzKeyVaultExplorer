#!/bin/bash

SVG=icon.svg
ICONSET_DIR=icon.iconset
ICNS_FILE=icon.icns

# Create iconset folder
mkdir -p $ICONSET_DIR

# Convert SVG to required PNG sizes
for size in 16 32 64 128 256 512 1024; do
    convert -background none -resize ${size}x${size} $SVG $ICONSET_DIR/icon_${size}x${size}.png
done

# Create .icns from PNGs
png2icns "$ICNS_FILE" "$ICONSET_DIR"/icon_*.png

# Generate .ico file (Windows)
convert $SVG -define icon:auto-resize=256,128,64,48,32,16 icon.ico

# Generate high-res PNG (Linux)
convert -background none -resize 256x256 $SVG icon.png

# Clean up
rm -r $ICONSET_DIR