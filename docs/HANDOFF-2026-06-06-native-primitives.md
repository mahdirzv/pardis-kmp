# Handoff — Native Primitive Rollout

_Date: 2026-06-06_

## Summary

This handoff captures the current state of the Pardis KMP native UI rollout after:

1. fixing the iOS bootstrap / SQLDelight wiring,
2. scoping RTL correctly for Persian content,
3. syncing the mobile theme to the attached Rivana design tokens,
4. and beginning the next phase: extracting reusable **native primitives** and applying them to `Library` + `Reader`.

The design is implementable natively. The chosen implementation strategy is:

- keep **shared state and logic** in `shared/`
- keep **all UI native** in `app/` and `iosApp/`
- use the attached web design as a **visual/design spec**, not as code to mechanically convert
- build a reusable primitive layer first, then restyle screens with those primitives

---

## Current repo state

### Branch / commit

- Current branch: `main`
- Current `HEAD`: `ec3318b`

### Important distinction

`ec3318b` is the **last committed merged baseline** on `main`.

There is also **uncommitted working tree progress** on top of that baseline for the native primitive rollout.

### Current working tree status

Modified:
- `app/src/main/java/app/pardis/android/ui/PardisApp.kt`
- `iosApp/iosApp/ContentView.swift`
- `iosApp/iosApp/PardisTheme.swift`
- `tasks/lessons.md`
- `tasks/todo.md`

New, untracked:
- `app/src/main/java/app/pardis/android/ui/PardisPrimitives.kt`
- `iosApp/iosApp/PardisPrimitives.swift`

---

## What has already been completed and merged to `main`

These items are already part of the committed baseline:

### 1. iOS bootstrap / DI / SQLDelight fixes
- iOS platform DI now provides the SQLDelight driver
- shared framework links `sqlite3` correctly on iOS
- iOS startup path no longer crashes on the missing DB path

Primary files involved earlier:
- `core/data/src/iosMain/kotlin/app/pardis/core/data/IosPlatformModule.kt`
- `core/di/src/commonMain/kotlin/app/pardis/core/di/CoreModules.kt`
- `shared/build.gradle.kts`
- `iosApp/iosApp/iosApp.swift`

### 2. RTL fix
- global app-wide RTL was removed
- RTL was scoped only to Persian reader content
- titles / vocab / Persian blocks render RTL where appropriate
- shell/navigation remain LTR for English-first UX

Primary files involved earlier:
- `app/src/main/java/app/pardis/android/ui/PardisApp.kt`
- `iosApp/iosApp/ContentView.swift`

### 3. Theme/token sync from the attached Rivana design
- `design-system/tokens.json` updated as source of truth
- Android token mirror synced
- generated Android/iOS token snapshots synced
- native theme wrappers added

Primary files already committed:
- `design-system/tokens.json`
- `design-system/generated/android/PardisTokens.kt`
- `design-system/generated/ios/PardisTokens.swift`
- `app/src/main/java/app/pardis/design/PardisTokens.kt`
- `app/src/main/java/app/pardis/design/PardisTheme.kt`
- `iosApp/iosApp/PardisTheme.swift`

---

## What is being done now

## Goal of the current phase

Build a reusable native primitive layer for the two most important screens first:

- `Library`
- `Reader`

This is the first real screen-level application of the design system after token/theme alignment.

The purpose is to avoid ad hoc styling in each screen and instead establish reusable native building blocks for both platforms.

---

## What has been done in the current uncommitted primitive rollout

## Android

### New file created
- `app/src/main/java/app/pardis/android/ui/PardisPrimitives.kt`

### Primitives extracted there
- `PardisScreenHeader`
- `PardisFilterPill`
- `PardisMetaPill`
- `PardisCard`
- `PardisPanel`
- `PardisRemoteImageFrame`
- `PardisReaderHeaderBar`
- `PardisControlGroup`
- `PardisVocabChip`
- `PardisVocabSheet`
- `PersianReaderParagraph`
- `PersianReaderInline`

### Android screen refactor done
`app/src/main/java/app/pardis/android/ui/PardisApp.kt` now uses the primitive layer for:

- `LibraryScreen`
  - header
  - search panel
  - filter pills
  - cached-only panel
  - story card metadata pills
  - media frames
- `ReaderScreen`
  - reader header bar
  - page pill
  - video frame card
  - text panel
  - vocab chip list
  - transport panel
  - vocab sheet panel

### Android verification status
Verified successfully with:

```zsh
cd /Users/mahdi/pardis-kmp
./gradlew :PardisAndroidApp:compileDebugKotlin --no-daemon
```

Result: build succeeded.

---

## iOS

### New file created
- `iosApp/iosApp/PardisPrimitives.swift`

### Primitives extracted there
- `PersianReaderText`
- `PardisScreenHeader`
- `PardisMetaPill`
- `PardisFilterPill`
- `PardisPanel`
- `PardisAsyncImageFrame`
- `PardisReaderHeaderBar`
- `PardisControlGroup`
- `PardisVocabChipView`
- `PardisVocabSheetContent`

### iOS theme adjustment for primitive support
- `iosApp/iosApp/PardisTheme.swift`
  - `pardisCardSurface(...)` now accepts configurable corner radius

### iOS screen refactor done
`iosApp/iosApp/ContentView.swift` now uses the primitive layer for:

- `LibraryScreen`
  - header
  - search panel
  - filter pills
  - cached-only panel
  - media frame
  - metadata pills
- `ReaderScreen`
  - reader header bar
  - page pill
  - framed video player surface
  - text panel
  - vocab chip list
  - transport panel
  - vocab sheet content

### iOS verification status
Verified successfully with:

```zsh
cd /Users/mahdi/pardis-kmp/iosApp
xcodegen generate

cd /Users/mahdi/pardis-kmp
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'generic/platform=iOS Simulator' build
```

Result: build succeeded.

---

## Current checklist state

From `tasks/todo.md`:

- [x] Define the native primitive set for `Library` + `Reader`
- [x] Extract reusable Android primitives and apply them to `Library` + `Reader`
- [x] Extract reusable iOS primitives and apply them to `Library` + `Reader`
- [x] Validate the edited files and run platform verification builds
- [ ] Roll the primitive layer out to the remaining native screens (`Today`, `Bedtime`, `Rewards`, `You`)

---

## Recommended next steps

## Immediate next step
Commit the current working tree once reviewed:

- `app/src/main/java/app/pardis/android/ui/PardisPrimitives.kt`
- `app/src/main/java/app/pardis/android/ui/PardisApp.kt`
- `iosApp/iosApp/PardisPrimitives.swift`
- `iosApp/iosApp/PardisTheme.swift`
- `iosApp/iosApp/ContentView.swift`
- `tasks/todo.md`
- `tasks/lessons.md`

## Next product/design rollout phase
Apply the same primitive system to the rest of the app in this order:

### 1. Home / Today
Likely primitives/composites to add:
- hero continue-reading card
- streak/stat strip
- section header with optional action
- horizontal shelf row
- collection tile
- word-of-the-day card

### 2. Bedtime / Lullaby
Likely primitives/composites to add:
- dark hero panel
- bedtime transport surface
- night-mode themed cards

### 3. Rewards / You
Likely primitives/composites to add:
- achievement card
- profile summary card
- stats row
- collection progress tiles

---

## Important implementation notes

### Design conversion rule
Do **not** try to convert the attached React/CSS code directly into Compose or SwiftUI.

Correct approach:
- extract tokens
- extract primitives
- build composites
- apply screen-by-screen

### Architecture rule
Do not move UI into `shared/`.

Keep:
- `shared/` = view models, state, actions, shared logic
- `app/` = Android native UI
- `iosApp/` = iOS native UI

### RTL rule
Do not reintroduce global RTL.
Only Persian text blocks and inline Persian elements should force RTL.

### Token rule
Any future palette/scale changes must start in:
- `design-system/tokens.json`

Then sync:
- `design-system/generated/android/PardisTokens.kt`
- `design-system/generated/ios/PardisTokens.swift`
- `app/src/main/java/app/pardis/design/PardisTokens.kt`

---

## Lessons captured during this phase

Key current lessons already tracked in `tasks/lessons.md`:

- nullable Koin singletons caused iOS startup failure
- iOS SQLDelight native framework requires `-lsqlite3`
- English-first shell should not be forced global RTL
- Persian RTL should be scoped only to Persian reader content
- design token source of truth must stay synchronized across generated/native mirrors
- native design rollout should start with primitives before screen restyling

---

## If resuming from this handoff

Start with:

1. inspect working tree status
2. review the new primitive files
3. commit the primitive rollout if it looks correct
4. continue the same pattern for `Today`, `Bedtime`, `Rewards`, `You`

Recommended verification after each screen batch:

```zsh
cd /Users/mahdi/pardis-kmp
./gradlew :PardisAndroidApp:compileDebugKotlin --no-daemon
```

```zsh
cd /Users/mahdi/pardis-kmp/iosApp
xcodegen generate

cd /Users/mahdi/pardis-kmp
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'generic/platform=iOS Simulator' build
```

---

## Files most relevant for the next person

### Design system
- `design-system/tokens.json`
- `app/src/main/java/app/pardis/design/PardisTheme.kt`
- `iosApp/iosApp/PardisTheme.swift`

### New primitive layer
- `app/src/main/java/app/pardis/android/ui/PardisPrimitives.kt`
- `iosApp/iosApp/PardisPrimitives.swift`

### Current screen usage
- `app/src/main/java/app/pardis/android/ui/PardisApp.kt`
- `iosApp/iosApp/ContentView.swift`

### Tracking
- `tasks/todo.md`
- `tasks/lessons.md`

