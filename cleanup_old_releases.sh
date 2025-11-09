#!/bin/bash

# Cleanup Old Release Files Script
# Removes old AAB/APK files while keeping the latest versions

echo "🧹 Cleaning up old release files..."
echo "=================================="

# Create backup directory for important files
mkdir -p store_assets/archive

# Keep only the latest versions (v1.6 and current)
echo "📦 Keeping latest releases:"
echo "   - app-release-v1.6-api35-dynamic-versioning.aab (latest with dynamic versioning)"
echo "   - app-release-v1.6-build2025092111.aab (current build)"

# Move old versions to archive or delete them
echo ""
echo "🗑️  Removing old versions:"

# Remove very old versions (v1.2, v1.3, v1.4, v1.5)
OLD_FILES=(
    "store_assets/app-release-latest-fixed.aab"
    "store_assets/app-release-latest.aab"
    "store_assets/app-release-v1.2.aab"
    "store_assets/app-release-v1.3-build5-visible-version.aab"
    "store_assets/app-release-v1.3-build5.aab"
    "store_assets/app-release-v1.3-debug.aab"
    "store_assets/app-release-v1.3-final.aab"
    "store_assets/app-release-v1.3-with-login-version.aab"
    "store_assets/app-release-v1.4.aab"
    "store_assets/app-release-v1.5-api34-extension.aab"
    "store_assets/app-release-v1.5-api35-compliant.aab"
    "store_assets/app-release-v1.5.aab"
    "store_assets/app-release.aab"
)

TOTAL_FREED=0

for file in "${OLD_FILES[@]}"; do
    if [ -f "$file" ]; then
        SIZE=$(du -m "$file" | cut -f1)
        echo "   Removing $(basename "$file") (${SIZE}MB)"
        rm "$file"
        TOTAL_FREED=$((TOTAL_FREED + SIZE))
    fi
done

# Also clean up old APK files
echo ""
echo "🗑️  Removing old APK files:"
find store_assets/ -name "*.apk" -o -name "*.apks" | while read file; do
    if [ -f "$file" ]; then
        SIZE=$(du -m "$file" | cut -f1)
        echo "   Removing $(basename "$file") (${SIZE}MB)"
        rm "$file"
        TOTAL_FREED=$((TOTAL_FREED + SIZE))
    fi
done

# Clean Gradle cache
echo ""
echo "🧹 Cleaning Gradle cache..."
rm -rf ~/.gradle/caches/
rm -rf ~/.gradle/wrapper/dists/

# Clean Android build cache
echo "🧹 Cleaning Android build cache..."
rm -rf ~/.android/build-cache/

echo ""
echo "✅ Cleanup completed!"
echo "📊 Estimated space freed: ~${TOTAL_FREED}MB + Gradle cache"
echo ""
echo "📋 Remaining files:"
ls -lah store_assets/*.aab 2>/dev/null || echo "   No AAB files remaining"

echo ""
echo "🔍 Checking disk space now..."
df -h .