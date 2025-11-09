#!/bin/bash

# Script to prepare assets for Google Play Store
# Uses your existing logo.png and converts SVG feature graphic

echo "Preparing Bird Life List store assets..."

# Create output directory
mkdir -p png_assets

# Check if ImageMagick is available for resizing PNG
if command -v convert &> /dev/null; then
    echo "Using ImageMagick to resize logo..."
    # Resize your logo to 512x512 for Google Play Store
    echo "Resizing your logo to 512x512..."
    convert original_logo.png -resize 512x512 png_assets/app_icon_512.png
    
    # Create additional icon sizes from your logo
    echo "Creating additional icon sizes from your logo..."
    convert original_logo.png -resize 192x192 png_assets/app_icon_192.png
    convert original_logo.png -resize 144x144 png_assets/app_icon_144.png
    convert original_logo.png -resize 96x96 png_assets/app_icon_96.png
    convert original_logo.png -resize 72x72 png_assets/app_icon_72.png
    convert original_logo.png -resize 48x48 png_assets/app_icon_48.png
    
elif command -v sips &> /dev/null; then
    echo "Using macOS sips to resize logo..."
    # Use macOS built-in sips command
    echo "Resizing your logo to 512x512..."
    sips -z 512 512 original_logo.png --out png_assets/app_icon_512.png
    
    # Create additional icon sizes
    echo "Creating additional icon sizes from your logo..."
    sips -z 192 192 original_logo.png --out png_assets/app_icon_192.png
    sips -z 144 144 original_logo.png --out png_assets/app_icon_144.png
    sips -z 96 96 original_logo.png --out png_assets/app_icon_96.png
    sips -z 72 72 original_logo.png --out png_assets/app_icon_72.png
    sips -z 48 48 original_logo.png --out png_assets/app_icon_48.png
    
else
    echo "Warning: Neither ImageMagick nor sips found. Copying original logo..."
    cp original_logo.png png_assets/app_icon_512.png
    echo "Note: You may need to manually resize to exactly 512x512 if needed"
fi

# Convert feature graphic to PNG (requires librsvg)
if command -v rsvg-convert &> /dev/null; then
    echo "Converting feature graphic..."
    rsvg-convert -w 1024 -h 500 feature_graphic_1024x500.svg -o png_assets/feature_graphic_1024x500.png
else
    echo "Warning: rsvg-convert not found. Install librsvg to convert feature graphic:"
    echo "  brew install librsvg"
    echo "Then run this script again to convert the feature graphic."
fi

echo ""
echo "Conversion complete! Files saved in png_assets/ directory:"
echo "  - app_icon_512.png (your logo resized for Google Play Store)"
if [ -f "png_assets/feature_graphic_1024x500.png" ]; then
    echo "  - feature_graphic_1024x500.png (for Google Play Store)"
fi
echo "  - Additional icon sizes for other uses"

echo ""
echo "Next steps:"
echo "1. Take screenshots of your app using the screenshot guide"
echo "2. Upload these assets to Google Play Console"
echo "3. Use the descriptions from app_descriptions.txt"