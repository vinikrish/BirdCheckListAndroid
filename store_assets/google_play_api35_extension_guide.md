# Google Play Store API Level 35 Requirement - Extension Guide

## Current Situation

Your **Bird Checklist Android v1.5** app currently targets API level 34 (Android 14), but Google Play Store now requires new app updates to target API level 35 (Android 15) starting **August 31, 2025**.

## Available Options

### Option 1: Request Extension (Recommended for Now)
- **Deadline Extension**: You can request an extension until **November 1, 2025**
- **Current Release**: Use the generated `app-release-v1.5-api34-extension.aab` file
- **Status**: Your app will remain discoverable and installable on all devices

### Option 2: Update to API Level 35 (Future)
- **Technical Challenge**: Android 35 SDK platform installation issues encountered
- **Timeline**: Can be addressed before November 1, 2025 deadline

## How to Request Extension

1. **Access Google Play Console**
   - Go to your app in Google Play Console
   - Look for the API level notification/warning message
   - Click on the notification to access extension forms

2. **Submit Extension Request**
   - Extension forms will be available in Play Console
   - Request extension until November 1, 2025
   - Provide justification (technical SDK issues)

3. **Upload Current Release**
   - Use `app-release-v1.5-api34-extension.aab`
   - Version: 1.5 (Build 8)
   - Target SDK: 34 (Android 14)
   - This meets current requirements with extension

## Current Release Details

- **File**: `app-release-v1.5-api34-extension.aab`
- **Version Name**: 1.5
- **Version Code**: 8
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34 (Android 14)
- **Build Tools**: 35.0.0
- **Signed**: Yes (with release keystore)

## Key Features in v1.5

- ✅ Smart progress indicators for better user experience
- ✅ Enhanced loading states during data operations
- ✅ Improved reliability and performance
- ✅ Better visual feedback for user actions
- ✅ All existing functionality preserved

## Next Steps

1. **Immediate**: Submit extension request in Google Play Console
2. **Upload**: Use `app-release-v1.5-api34-extension.aab` for submission
3. **Timeline**: Address API 35 upgrade before November 1, 2025

## Technical Notes

- The Android 35 SDK platform had installation/corruption issues
- API 34 build is stable and fully functional
- Extension provides time to resolve technical challenges
- No functionality is lost with current API 34 configuration

## References

- [Google Play Target API Requirements](https://developer.android.com/google/play/requirements/target-sdk)
- [Play Console Help - Target API Requirements](https://support.google.com/googleplay/android-developer/answer/11926878)

---

**Generated**: September 21, 2025  
**App Version**: Bird Checklist Android v1.5  
**Build Configuration**: API 34 with Extension Request Strategy