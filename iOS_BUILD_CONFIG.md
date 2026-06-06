# iOS Build Configuration Summary

## Changes Made

Your KMP project has been configured with full iOS build support alongside the existing Android build.

### 1. Updated `settings.gradle.kts`

Added iOS app to the Gradle build system:
```kotlin
include(
    ":PardisAndroidApp",
    ":PardisiOSApp",           // ← NEW
    ":core:model",
    ":core:domain",
    ...
)

project(":PardisAndroidApp").projectDir = file("app")
project(":PardisiOSApp").projectDir = file("iosApp")  // ← NEW
```

### 2. Created `iosApp/build.gradle.kts`

New orchestration module that:
- Manages Shared.framework builds for iOS
- Provides convenient Gradle tasks for iOS development
- Integrates iOS builds into the Gradle system

### 3. Fixed Shared Module Configuration

Updated `shared/build.gradle.kts`:
- Changed deprecated `androidLibrary` to `android`
- Configured iOS targets: arm64, x86_64 (Intel), arm64 (Apple Silicon simulator)
- Set up framework generation for all iOS architectures

### 4. Created iOS Documentation

- `iosApp/README.md` — Complete iOS setup and development guide

## Build Configuration

### Directory Structure

```
pardis-kmp/
├── app/                      # Android app (existing)
├── iosApp/                   # iOS app (NEW)
│   ├── build.gradle.kts      # NEW - Gradle orchestration
│   ├── README.md             # NEW - iOS guide
│   └── iosApp/               # Swift source files
├── shared/                   # Shared KMP logic (updated)
├── core/*/                   # Core modules (no changes)
└── settings.gradle.kts       # UPDATED - Added iOS project
```

## Available Commands

### iOS-Specific

```bash
# View iOS project structure
./gradlew projects | grep -i ios

# See iOS build tasks
./gradlew :PardisiOSApp:tasks --group build

# Build Shared framework
./gradlew :PardisiOSApp:buildFrameworkDebug    # Debug variant
./gradlew :PardisiOSApp:buildFrameworkRelease  # Release variant

# Show iOS configuration
./gradlew :PardisiOSApp:printConfig

# Open Xcode project (when created)
./gradlew :PardisiOSApp:openXcode
```

### Android (Still Works)

```bash
# Android build unchanged
./gradlew :PardisAndroidApp:build
./gradlew :PardisAndroidApp:tasks --group build
```

### All Projects

```bash
# See all modules
./gradlew projects

# Build everything
./gradlew assemble
```

## iOS Targets

The shared KMP module now builds for:

| Target | Architecture | Use Case |
|--------|-------------|----------|
| `iosArm64` | ARM64 (physical device) | iPhone/iPad hardware |
| `iosX64` | x86_64 (Intel simulator) | Intel-based Mac simulator |
| `iosSimulatorArm64` | ARM64 (Apple Silicon simulator) | Apple Silicon Mac simulator |

All targets produce the `Shared.framework` that the iOS app consumes.

## Next Steps

1. **Create Xcode Project**
   - Create an iOS app project in Xcode
   - Place it in `iosApp/` or create as a workspace

2. **Add Shared Framework**
   - Build framework: `./gradlew :PardisiOSApp:buildFrameworkDebug`
   - Add to Xcode project's Build Phases
   - Link with SwiftUI views

3. **Implement UI**
   - Use existing adapters: `ReaderSharedViewModel.swift`, `LibrarySharedViewModel.swift`
   - Implement SwiftUI screens
   - Use `FlowCollector.swift` to observe Kotlin StateFlow

4. **Use Design Tokens**
   - Import `design-system/generated/ios/PardisTokens.swift`
   - Use Pardis palette: saffron, indigo, mint, lilac, cream, ink

## Verification

Verify the build configuration:

```bash
# List all projects
./gradlew projects

# You should see:
# Root project 'Pardis'
# +--- Project ':PardisAndroidApp'
# +--- Project ':PardisiOSApp'        ← iOS app is now registered
# +--- Project ':shared'
# └--- Project ':core:*'
```

## Architecture Compliance

✅ **Follows `docs/kmpSkill.md` (KMP Native UI pattern):**

- Shared logic: KMP modules in `core/*` and `shared/`
- Android UI: Native Compose (in `app/`)
- iOS UI: Native SwiftUI (in `iosApp/`)
- No shared UI layer
- Platforms consume shared state via adapters

✅ **Adheres to Pardis design system:**

- Uses Pardis tokens from `design-system/`
- Supports Pardis palette (saffron, indigo, mint, etc.)
- Ready for offline bundles and content fidelity

## Build Artifacts

After running `./gradlew :PardisiOSApp:buildFrameworkDebug`:

```
shared/build/frameworks/
└── Shared.framework/
    ├── Shared                                # Binary
    ├── Headers/                              # Swift bridging headers (SKIE-generated)
    ├── Modules/                              # Module map
    └── Info.plist
```

The framework is ready to be imported into the iOS app.

## Troubleshooting

### iOS project not showing up in Gradle?
```bash
# Verify it's included in settings.gradle.kts
grep -i "PardisiOSApp" settings.gradle.kts
```

### Can't find iOS build tasks?
```bash
# List all iOS tasks
./gradlew :PardisiOSApp:tasks
```

### Framework build fails?
```bash
# Check shared module configuration
./gradlew :shared:tasks | grep -i framework
```

## References

- **Architecture**: `docs/kmpSkill.md`
- **Pardis Delivery**: `docs/skills/pardis-kmp-delivery/SKILL.md`
- **Design System**: `design-system/README.md`
- **Code Rules**: `docs/code-rules.md`
- **iOS Setup**: `iosApp/README.md`
- **Top-level**: `../pardis/AGENTS.md`

---

**Status**: ✅ iOS build configuration complete. Ready for Xcode project setup and SwiftUI implementation.

