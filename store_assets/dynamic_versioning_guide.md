# Dynamic Versioning System

## Overview
This project now uses a dynamic versioning system to automatically generate unique version codes for each build, preventing Google Play Store version conflicts.

## How It Works

### Version Code Generation
- **Format**: `YYYYMMDDHH` (Year + Month + Day + Hour)
- **Example**: `2025092111` (September 21, 2025, 11 AM)
- **Location**: `app/build.gradle` in the `defaultConfig` section

### Implementation
```gradle
versionCode Integer.parseInt(new Date().format('yyyyMMddHH'))
```

## Benefits

1. **No More Version Conflicts**: Each build gets a unique version code
2. **Automatic Increment**: No manual version management needed
3. **Chronological Order**: Version codes increase with time
4. **Easy Debugging**: Version code tells you when the build was created

## Version Naming
- Version names are still manually managed (e.g., "1.6", "1.7")
- Update `versionName` in `build.gradle` for major/minor releases

## Current Release
- **Version Name**: 1.6
- **Version Code**: 2025092111 (auto-generated)
- **Target SDK**: 35 (Android 15)
- **File**: `app-release-v1.6-api35-dynamic-versioning.aab`

## Future Releases
Simply run `./gradlew bundleRelease` and the system will automatically:
1. Generate a new unique version code
2. Build the release
3. No version conflicts with Google Play Store

## Notes
- The version code will always be higher than previous builds
- Maximum version code is 2,100,000,000 (Google Play limit)
- Current format supports builds until year 2099