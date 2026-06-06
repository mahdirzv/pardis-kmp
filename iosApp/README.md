# Pardis iOS App

Native iOS app built with SwiftUI that consumes the shared KMP logic from the `Shared.framework`.

## Architecture

- **UI**: SwiftUI (native iOS interface)
- **Logic**: Shared KMP module compiled as `Shared.framework`
- **Build System**: Gradle orchestrates framework builds + Xcode manages app

## Prerequisites

- Xcode 15+
- iOS 17+ (for `@Observable` support)
- Kotlin JVM 17+
- Gradle 9.3+

## Project Setup

### 1. Build the Shared Framework

The Shared framework must be built before opening the iOS app in Xcode.

**For development (debug):**
```bash
cd /Users/mahdi/pardis-kmp
./gradlew :PardisiOSApp:buildFrameworkDebug
```

**For production (release):**
```bash
cd /Users/mahdi/pardis-kmp
./gradlew :PardisiOSApp:buildFrameworkRelease
```

### 2. Framework Location

After building, the framework is available at:
```
shared/build/frameworks/
├── Shared.framework/        (debug)
└── Shared.framework/        (release)
```

### 3. Xcode Project Setup

Once an Xcode project is created, set up the framework dependency:

1. Open the iOS project in Xcode
2. Add the generated `Shared.framework` to your target's Build Phases
3. Ensure "Copy Bundle Resources" includes the framework

## Development Workflow

### Quick Start

```bash
# 1. Build framework
./gradlew :PardisiOSApp:buildFrameworkDebug

# 2. Open Xcode project (when created)
./gradlew :PardisiOSApp:openXcode

# 3. Build & run in Xcode (Cmd + R)
```

### After Shared Changes

If you modify code in `shared/` or any `core/` module:

```bash
# Rebuild the framework
./gradlew :PardisiOSApp:buildFrameworkDebug

# In Xcode: Product → Clean Build Folder (Cmd + Shift + K)
# Then rebuild in Xcode
```

## Available Gradle Tasks

View all iOS tasks:
```bash
./gradlew :PardisiOSApp:tasks --group build
```

**Key tasks:**
- `:PardisiOSApp:buildFrameworkDebug` — Build Shared.framework for iOS debug
- `:PardisiOSApp:buildFrameworkRelease` — Build Shared.framework for iOS release
- `:PardisiOSApp:buildDebug` — Full debug build
- `:PardisiOSApp:buildRelease` — Full release build
- `:PardisiOSApp:cleanFrameworks` — Remove built frameworks
- `:PardisiOSApp:printConfig` — Show iOS app configuration
- `:PardisiOSApp:openXcode` — Open Xcode project

## Directory Structure

```
iosApp/
├── build.gradle.kts         # Gradle orchestration (builds Shared framework)
├── iosApp/                  # SwiftUI app source
│   ├── iosApp.swift         # App entry point
│   ├── ContentView.swift     # Main UI
│   ├── ReaderSharedViewModel.swift
│   ├── LibrarySharedViewModel.swift
│   ├── FlowCollector.swift   # StateFlow → SwiftUI bridge
│   └── Info.plist
└── README.md                # This file
```

## iOS Architecture Rules

Per `docs/kmpSkill.md`:

- ✅ Native SwiftUI UI only
- ✅ No Compose Multiplatform UI
- ✅ Consume shared state via `@Observable` adapters
- ✅ Use `StateFlow` and `for await` for reactive updates
- ✅ Navigation is native to iOS

### Key Files

- `FlowCollector.swift` — Bridge from Kotlin `StateFlow` to SwiftUI
- `ReaderSharedViewModel.swift` — iOS adapter for shared ViewModel
- `LibrarySharedViewModel.swift` — iOS adapter for shared ViewModel

## Troubleshooting

### Framework Not Building

```bash
# Clean and rebuild
./gradlew :PardisiOSApp:cleanFrameworks
./gradlew :PardisiOSApp:buildFrameworkDebug
```

### Xcode Can't Find Framework

1. Check framework exists: `ls -la shared/build/frameworks/`
2. In Xcode: File → Project Settings → Locations → Set Derived Data if needed
3. Clean build folder in Xcode: Cmd + Shift + K

### Kotlin Changes Not Reflected

1. Rebuild framework: `./gradlew :PardisiOSApp:buildFrameworkDebug`
2. Clean Xcode: Cmd + Shift + K
3. Rebuild in Xcode: Cmd + B

## Design Tokens

iOS uses Pardis design tokens from `design-system/generated/ios/PardisTokens.swift`.

Available tokens:
- Colors: saffron, indigo, mint, lilac, cream, ink, etc.
- Spacing: xs, sm, md, lg, xl
- Typography: title, heading, body, caption, etc.

See `design-system/README.md` for full token reference.

## Next Steps

1. [ ] Create Xcode project (`.xcodeproj`) or use `xcodegen` with `project.yml`
2. [ ] Add `Shared.framework` to build phases
3. [ ] Set up app icons and launch screen
4. [ ] Configure signing & capabilities
5. [ ] Implement Reader UI with SwiftUI
6. [ ] Implement Library UI with SwiftUI
7. [ ] Test on simulator and physical device

## References

- KMP Architecture: `docs/kmpSkill.md`
- Pardis Delivery: `docs/skills/pardis-kmp-delivery/SKILL.md`
- Code Rules: `docs/code-rules.md`
- Design System: `design-system/README.md`
- Top-level AGENTS.md: `../pardis/AGENTS.md`

