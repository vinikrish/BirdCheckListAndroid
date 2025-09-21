#!/bin/bash

# Build Diagnostics Script - Check for common build failure causes
# Run this before building to catch issues early

echo "🔍 Android Build Diagnostics"
echo "============================"

# Check disk space
echo "💾 Disk Space Check:"
DISK_USAGE=$(df -h . | tail -1 | awk '{print $5}' | sed 's/%//')
AVAILABLE_GB=$(df -h . | tail -1 | awk '{print $4}')

if [ "$DISK_USAGE" -gt 95 ]; then
    echo "❌ CRITICAL: Disk usage is ${DISK_USAGE}% (Available: ${AVAILABLE_GB})"
    echo "   Android builds need ~2-5GB free space"
    echo "   Please free up disk space before building"
    echo ""
    echo "🧹 Quick cleanup suggestions:"
    echo "   - Clean Gradle cache: ./gradlew clean"
    echo "   - Clear build folders: rm -rf app/build/ build/"
    echo "   - Empty trash"
    echo "   - Remove old AAB files from store_assets/"
    exit 1
elif [ "$DISK_USAGE" -gt 90 ]; then
    echo "⚠️  WARNING: Disk usage is ${DISK_USAGE}% (Available: ${AVAILABLE_GB})"
    echo "   Consider freeing space soon"
else
    echo "✅ Disk space OK: ${DISK_USAGE}% used (Available: ${AVAILABLE_GB})"
fi

# Check Gradle daemon
echo ""
echo "🔧 Gradle Daemon Check:"
DAEMON_COUNT=$(./gradlew --status | grep -c "IDLE\|BUSY" || echo "0")
if [ "$DAEMON_COUNT" -gt 0 ]; then
    echo "✅ Gradle daemon running ($DAEMON_COUNT processes)"
else
    echo "⚠️  No Gradle daemon found - will start new one"
fi

# Check Java/JDK
echo ""
echo "☕ Java Environment:"
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    echo "✅ Java found: $JAVA_VERSION"
else
    echo "❌ Java not found in PATH"
    exit 1
fi

# Check Android SDK
echo ""
echo "📱 Android SDK Check:"
if [ -d "$HOME/Library/Android/sdk" ]; then
    echo "✅ Android SDK found at $HOME/Library/Android/sdk"
else
    echo "❌ Android SDK not found at expected location"
fi

# Check keystore
echo ""
echo "🔐 Keystore Check:"
if [ -f "app/release-key.keystore" ]; then
    echo "✅ Release keystore found"
else
    echo "❌ Release keystore missing: app/release-key.keystore"
    exit 1
fi

# Check gradle.properties
echo ""
echo "⚙️  Build Configuration:"
if [ -f "gradle.properties" ]; then
    echo "✅ gradle.properties found"
else
    echo "❌ gradle.properties missing"
fi

# Memory check
echo ""
echo "🧠 Memory Check:"
MEMORY_GB=$(sysctl hw.memsize | awk '{print int($2/1024/1024/1024)}')
echo "✅ System RAM: ${MEMORY_GB}GB"

if [ "$MEMORY_GB" -lt 4 ]; then
    echo "⚠️  Low RAM - builds may be slow"
fi

echo ""
echo "🎯 Diagnostic Summary:"
if [ "$DISK_USAGE" -gt 95 ]; then
    echo "❌ BUILD WILL LIKELY FAIL - Free up disk space first!"
    echo ""
    echo "🚨 IMMEDIATE ACTION NEEDED:"
    echo "1. Run: ./gradlew clean"
    echo "2. Delete old files from store_assets/"
    echo "3. Empty trash"
    echo "4. Try build again"
else
    echo "✅ System looks ready for building"
    echo ""
    echo "🚀 Safe to proceed with:"
    echo "   ./gradlew bundleRelease"
    echo "   OR"
    echo "   ./build_release.sh"
fi