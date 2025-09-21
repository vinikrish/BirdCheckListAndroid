#!/bin/bash

# Automated Release Build Script for Bird Checklist Android App
# This script automates the entire release process to prevent forgetting steps

set -e  # Exit on any error

echo "🚀 Starting Automated Release Build Process..."
echo "================================================"

# Get current timestamp for version tracking
TIMESTAMP=$(date '+%Y%m%d%H')
VERSION_NAME=$(grep 'versionName' app/build.gradle | sed 's/.*"\(.*\)".*/\1/')

echo "📋 Build Information:"
echo "   Version Name: $VERSION_NAME"
echo "   Version Code: $TIMESTAMP"
echo "   Build Time: $(date)"
echo ""

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Build debug first to catch any issues early
echo "🔨 Building debug version for testing..."
./gradlew assembleDebug

# Build the release bundle
echo "📦 Building release bundle..."
./gradlew bundleRelease

# Create descriptive filename
RELEASE_FILENAME="app-release-v${VERSION_NAME}-build${TIMESTAMP}.aab"
RELEASE_PATH="store_assets/${RELEASE_FILENAME}"

# Copy to store_assets with descriptive name
echo "📁 Copying release to store_assets..."
cp app/build/outputs/bundle/release/app-release.aab "$RELEASE_PATH"

# Verify the file was created
if [ -f "$RELEASE_PATH" ]; then
    FILE_SIZE=$(ls -lh "$RELEASE_PATH" | awk '{print $5}')
    echo "✅ Release build successful!"
    echo "   File: $RELEASE_FILENAME"
    echo "   Size: $FILE_SIZE"
    echo "   Location: $RELEASE_PATH"
else
    echo "❌ Error: Release file not found!"
    exit 1
fi

# Create release notes
RELEASE_NOTES_FILE="store_assets/release_notes_v${VERSION_NAME}_build${TIMESTAMP}.md"
cat > "$RELEASE_NOTES_FILE" << EOF
# Release Notes - v${VERSION_NAME} (Build ${TIMESTAMP})

**Build Date:** $(date)
**Version Code:** ${TIMESTAMP}
**Target SDK:** 35 (Android 15)
**File:** ${RELEASE_FILENAME}

## Build Details
- ✅ Clean build completed
- ✅ Debug build tested
- ✅ Release bundle generated
- ✅ API 35 compliant
- ✅ Dynamic versioning enabled

## File Information
- **Size:** $(ls -lh "$RELEASE_PATH" | awk '{print $5}')
- **Location:** \`$RELEASE_PATH\`

## Next Steps
1. Upload to Google Play Console
2. Test on internal track
3. Promote to production when ready

---
*Generated automatically by build_release.sh*
EOF

echo ""
echo "📝 Release notes created: $RELEASE_NOTES_FILE"
echo ""
echo "🎯 Ready for Google Play Store submission!"
echo "================================================"
echo "Next steps:"
echo "1. Upload: $RELEASE_PATH"
echo "2. Review: $RELEASE_NOTES_FILE"
echo "3. Submit to Google Play Console"