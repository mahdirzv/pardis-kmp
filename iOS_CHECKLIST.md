# iOS Build Configuration - Complete Checklist

## ✅ Configuration Complete

Your Pardis KMP project now has a complete iOS build configuration alongside the existing Android build.

## What Was Added

### Files Created
- ✅ `iosApp/build.gradle.kts` — iOS app Gradle module for orchestrating framework builds
- ✅ `iosApp/README.md` — Comprehensive iOS setup and development guide
- ✅ `iOS_BUILD_CONFIG.md` — This configuration summary document

### Files Modified
- ✅ `settings.gradle.kts` — Added iOS app as `:PardisiOSApp` project
- ✅ `shared/build.gradle.kts` — Updated to use `android` instead of deprecated `androidLibrary`

## Current Build Structure

```
pardis-kmp/
├── app/              [Android App]        ← Unchanged
│   └── build.gradle.kts
├── iosApp/           [iOS App]            ← NEW
│   ├── build.gradle.kts                   ← NEW
│   ├── README.md                          ← NEW
│   └── iosApp/       [Swift source]       ← Existing
├── shared/           [Shared KMP Logic]   ← Updated
│   └── build.gradle.kts                     (deprecated API fixed)
├── core/*/           [Core Modules]       ← Unchanged
└── settings.gradle.kts                    ← Updated (added iOS)
```

## Available Commands Now

### View Project Structure
```bash
./gradlew projects
```
**Output includes:**
- `:PardisAndroidApp` ← Android
- `:PardisiOSApp` ← iOS (NEW)
- `:shared`, `:core:*` (shared logic)

### Android Build (Unchanged)
```bash
./gradlew :PardisAndroidApp:build
./gradlew :PardisAndroidApp:assemble
./gradlew :PardisAndroidApp:tasks --group build
```

### iOS Build (NEW)
```bash
# View iOS tasks
./gradlew :PardisiOSApp:tasks --group build

# Build Shared framework for debug
./gradlew :PardisiOSApp:buildFrameworkDebug

# Build Shared framework for release
./gradlew :PardisiOSApp:buildFrameworkRelease

# Show iOS configuration
./gradlew :PardisiOSApp:printConfig

# Open Xcode project (when created)
./gradlew :PardisiOSApp:openXcode

# Clean iOS artifacts
./gradlew :PardisiOSApp:cleanFrameworks
```

## iOS Build Targets

Shared KMP module builds for iOS on:

| Target | Architecture | Simulator |
|--------|-------------|-----------|
| `iosArm64` | ARM64 | Physical device only |
| `iosX64` | x86_64 | Intel Mac simulator |
| `iosSimulatorArm64` | ARM64 | Apple Silicon Mac simulator |

All produce `Shared.framework` for consumption by the iOS app.

## Verified Working

✅ Both Android and iOS projects are registered with Gradle
✅ Android build tasks unchanged and functional
✅ iOS build tasks available and functional
✅ Shared KMP module configured for iOS targets
✅ Framework generation ready

## Next Steps

### 1. Create Xcode Project (Choose One)

**Option A: Create new project in Xcode**
- File → New → Project
- iOS → App
- Save to `/Users/mahdi/pardis-kmp/iosApp/`

**Option B: Use xcodegen** (recommended for team consistency)
```bash
cd iosApp
xcodegen generate  # Requires project.yml
```

### 2. Build Shared Framework

```bash
./gradlew :PardisiOSApp:buildFrameworkDebug
```

Framework location: `shared/build/frameworks/Shared.framework/`

### 3. Link Framework in Xcode

1. Open project in Xcode
2. Select target
3. Build Phases → Link Binary With Libraries
4. Add `Shared.framework`

### 4. Implement SwiftUI UI

Use existing adapters in `iosApp/iosApp/`:
- `ReaderSharedViewModel.swift` — Reader screen logic
- `LibrarySharedViewModel.swift` — Library screen logic
- `FlowCollector.swift` — StateFlow observer

### 5. Use Design Tokens

Import and use Pardis tokens:
```swift
import PardisTokens

Color(uiColor: PardisTokens.Color.saffron)  // Primary color
```

Token file: `design-system/generated/ios/PardisTokens.swift`

## Architecture Compliance

✅ Follows **KMP Native UI pattern** (`docs/kmpSkill.md`)
- Shared: Domain, data, network, models in `core/*` and `shared/`
- Android: Native Compose UI in `app/`
- iOS: Native SwiftUI UI in `iosApp/`

✅ Follows **Pardis Delivery** (`docs/skills/pardis-kmp-delivery/SKILL.md`)
- Uses Pardis palette and tokens
- Ready for offline bundles (asset manifests, SQLDelight caching)
- Public Supabase read support via Ktor
- Family features with RLS support

✅ Follows **Code Rules** (`docs/code-rules.md`)
✅ Follows **KMP Review Rules** (`.github/instructions/kmp.instructions.md`)

## Troubleshooting

### iOS project not showing?
```bash
grep PardisiOSApp settings.gradle.kts
```
Should see: `include(":PardisiOSApp")`

### Can't find iOS tasks?
```bash
./gradlew :PardisiOSApp:tasks
```

### Framework build fails?
```bash
./gradlew :shared:tasks | grep -i framework
./gradlew :PardisiOSApp:buildFrameworkDebug --stacktrace
```

### Android broke?
Android build should be unaffected:
```bash
./gradlew :PardisAndroidApp:build
```

## Documentation

- **Main iOS Guide**: `iosApp/README.md`
- **Configuration Details**: `iOS_BUILD_CONFIG.md` (this file)
- **Architecture**: `docs/kmpSkill.md`
- **Project Setup**: `docs/skills/pardis-kmp-delivery/SKILL.md`
- **Code Rules**: `docs/code-rules.md` + `.github/instructions/kmp.instructions.md`
- **Design System**: `design-system/README.md`

## Summary

**Before:**
- ✅ Android app working
- ❌ iOS build not in Gradle
- ❌ No iOS orchestration

**After:**
- ✅ Android app working (unchanged)
- ✅ iOS build in Gradle
- ✅ iOS orchestration ready
- ✅ Framework generation configured
- ✅ Multiple iOS targets (device + simulators)
- ✅ Documentation complete

**Status**: Ready for Xcode project setup and SwiftUI implementation! 🎉

