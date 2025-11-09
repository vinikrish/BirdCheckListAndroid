# 🚀 Automated Release Process Guide

## Overview
This guide explains the automated release system that ensures you **never forget** to run `./gradlew bundleRelease` or any other release steps.

## 🎯 Problem Solved
- ❌ **Before**: Manual `./gradlew bundleRelease` - easy to forget
- ✅ **After**: Multiple automated options - impossible to forget

---

## 🛠️ Available Release Methods

### 1. **One-Command Release** (Recommended)
```bash
./build_release.sh
```
**What it does:**
- Cleans previous builds
- Builds debug for testing
- Builds release bundle
- Copies to `store_assets/` with descriptive name
- Creates release notes
- Verifies everything worked

### 2. **NPM Scripts** (Quick & Easy)
```bash
# Full automated release
npm run release

# Quick release (just build + copy)
npm run release:quick

# Individual commands
npm run build:debug
npm run build:release
npm run clean
```

### 3. **GitHub Actions** (Fully Automated)
```bash
# Create and push a version tag
git tag v1.7
git push origin v1.7
```
**What happens automatically:**
- GitHub builds the release
- Creates GitHub release with notes
- Uploads the AAB file
- No manual intervention needed!

### 4. **Manual Backup** (If needed)
```bash
./gradlew bundleRelease
cp app/build/outputs/bundle/release/app-release.aab store_assets/
```

---

## 📋 Release Workflow Options

### Option A: Local Development
1. Make your changes
2. Run: `./build_release.sh`
3. Upload the generated AAB to Google Play Console

### Option B: Git-Based (Recommended)
1. Make your changes
2. Commit and push
3. Create version tag: `git tag v1.7 && git push origin v1.7`
4. GitHub automatically builds and creates release
5. Download AAB from GitHub releases
6. Upload to Google Play Console

### Option C: NPM-Based
1. Make your changes
2. Run: `npm run release`
3. Upload the generated AAB to Google Play Console

---

## 🔄 Version Management

### Automatic Version Codes
- **Format**: `YYYYMMDDHH` (timestamp-based)
- **Example**: `2025092111` (Sept 21, 2025, 11 AM)
- **Benefit**: Never conflicts, always increasing

### Version Names
- Update manually in `app/build.gradle`
- Format: `"1.6"`, `"1.7"`, etc.
- Or use: `npm run version:bump` (auto-increments)

---

## 📁 File Organization

### Generated Files
```
store_assets/
├── app-release-v1.6-build2025092111.aab     # Release bundle
├── release_notes_v1.6_build2025092111.md    # Auto-generated notes
└── dynamic_versioning_guide.md              # Version system docs
```

### Scripts & Config
```
├── build_release.sh                         # Main release script
├── package.json                            # NPM scripts
├── .github/workflows/release.yml           # GitHub Actions
└── AUTOMATED_RELEASE_GUIDE.md              # This guide
```

---

## 🚨 Never Forget Checklist

### ✅ What's Automated
- [x] Clean builds
- [x] Debug testing
- [x] Release bundle creation
- [x] File copying with descriptive names
- [x] Version code generation
- [x] Release notes creation
- [x] File verification

### ⚠️ What You Still Need to Do
- [ ] Update version name in `app/build.gradle` (when needed)
- [ ] Upload AAB to Google Play Console
- [ ] Test the release on internal track
- [ ] Promote to production

---

## 🎯 Quick Reference

| Task | Command |
|------|---------|
| **Full Release** | `./build_release.sh` |
| **Quick Release** | `npm run release:quick` |
| **Auto Release** | `git tag v1.7 && git push origin v1.7` |
| **Debug Build** | `npm run build:debug` |
| **Clean Build** | `npm run clean` |

---

## 🔧 Troubleshooting

### If build_release.sh fails:
1. Check file permissions: `chmod +x build_release.sh`
2. Ensure Gradle wrapper works: `./gradlew --version`
3. Clean and retry: `./gradlew clean && ./build_release.sh`

### If GitHub Actions fails:
1. Check the Actions tab in your GitHub repo
2. Ensure you have proper Android SDK setup in the workflow
3. Verify the tag was pushed correctly

### If NPM scripts fail:
1. Install Node.js if not present
2. The scripts are just shortcuts to the shell commands

---

## 🎉 Success Indicators

When everything works, you'll see:
```
✅ Release build successful!
   File: app-release-v1.6-build2025092111.aab
   Size: 15.3M
   Location: store_assets/app-release-v1.6-build2025092111.aab

🎯 Ready for Google Play Store submission!
```

---

**🔥 Bottom Line**: You now have **4 different ways** to build releases automatically. You'll never forget `./gradlew bundleRelease` again!