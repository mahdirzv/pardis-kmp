# Handoff — Native Theme, Primitives, and Root Shell

_Date: 2026-06-06_

## Current State

- Branch: `main`
- Local state: clean
- Remote state: `main` is synced with `origin/main`
- Current `HEAD`: `92b29eb Add native Today tab composite`

Recent commits on `main`:

- `92b29eb` Add native Today tab composite
- `f35b689` Add native root tab shell
- `1a19782` Add native font roles and icon primitives
- `f2f0cee` Build native theme and story composites
- `2006bef` Extract native reader and library primitives
- `ec3318b` Refresh native theme from Rivana tokens
- `04ecd4b` Fix iOS bootstrap and scope reader RTL

All work described below is committed and pushed to `origin/main`.

## Strategy

The attached standalone Rivana design has been treated as a visual/design reference, not as code to port directly.

Implementation rules remain:

- shared state and business logic stay in `shared/` and `core/*`
- Android UI stays native Compose in `app/`
- iOS UI stays native SwiftUI in `iosApp/`
- no shared UI layer
- all visuals use Pardis tokens and palette
- Persian RTL is scoped to Persian content only, not the whole app shell

## Completed

### iOS Bootstrap And RTL

- iOS platform DI provides the SQLDelight driver.
- shared framework links `sqlite3`.
- iOS startup no longer crashes on the missing DB path.
- Global RTL was removed from the app shell.
- Persian reader text, titles, and vocab blocks keep scoped RTL behavior.

Key files:

- `core/data/src/iosMain/kotlin/app/pardis/core/data/IosPlatformModule.kt`
- `core/di/src/commonMain/kotlin/app/pardis/core/di/CoreModules.kt`
- `shared/build.gradle.kts`
- `iosApp/iosApp/iosApp.swift`
- `app/src/main/java/app/pardis/android/ui/PardisApp.kt`
- `iosApp/iosApp/ContentView.swift`

### Theme And Token Sync

- `design-system/tokens.json` is the source of truth.
- Android and iOS token mirrors are synced.
- Native theme wrappers are in place.
- Font roles and icon primitives were added, but final branded font/icon assets are still pending.

Key files:

- `design-system/tokens.json`
- `design-system/generated/android/PardisTokens.kt`
- `design-system/generated/ios/PardisTokens.swift`
- `app/src/main/java/app/pardis/design/PardisTokens.kt`
- `app/src/main/java/app/pardis/design/PardisTheme.kt`
- `iosApp/iosApp/PardisTheme.swift`

### Native Primitive Layer

Android primitive file:

- `app/src/main/java/app/pardis/android/ui/PardisPrimitives.kt`

Android primitives include:

- `PardisScreenHeader`
- `PardisSectionHeader`
- `PardisMetricStrip`
- `PardisBottomTabBar`
- `PardisFilterPill`
- `PardisMetaPill`
- `PardisCard`
- `PardisPanel`
- `PardisRemoteImageFrame`
- `PardisFeaturedStoryCard`
- `PardisStoryCard`
- `PardisReaderHeaderBar`
- `PardisControlGroup`
- `PardisVocabChip`
- `PardisVocabSheet`
- `PersianReaderParagraph`
- `PersianReaderInline`

iOS primitive file:

- `iosApp/iosApp/PardisPrimitives.swift`

iOS primitives include:

- `PersianReaderText`
- `PardisScreenHeader`
- `PardisSectionHeader`
- `PardisMetricStrip`
- `PardisBottomTabBar`
- `PardisMetaPill`
- `PardisFilterPill`
- `PardisPanel`
- `PardisAsyncImageFrame`
- `PardisFeaturedStoryCard`
- `PardisStoryCard`
- `PardisReaderHeaderBar`
- `PardisControlGroup`
- `PardisVocabChipView`
- `PardisVocabSheetContent`

### Library And Reader Restyle

`Library` and `Reader` now use native primitive components on both platforms.

Library uses primitives for:

- screen header
- metrics
- featured story
- search panel
- age filter pills
- cached-only panel
- story cards
- offline controls

Reader uses primitives for:

- reader header
- page pill
- video/player surface
- text panels
- vocab chips
- transport controls
- vocab sheet

Key files:

- `app/src/main/java/app/pardis/android/ui/PardisApp.kt`
- `iosApp/iosApp/ContentView.swift`

### Root Tab Shell

The app now has a native root tab shell on Android and iOS:

- `Today`
- `Library`
- `Bedtime`
- `Rewards`
- `You`

`Library` remains fully wired to the existing shared `LibraryViewModel`.

`Bedtime`, `Rewards`, and `You` currently render tokenized native placeholder panels. They are ready for shared state contracts when those features are implemented.

### Today Composite

`Today` is no longer a placeholder. It is the first native child/home composite built from existing shared Library state.

It includes:

- daily header
- story/offline/age-band metrics
- continue-reading featured story
- short "For later" story list
- offline controls on story cards
- vocabulary-focus panel

Android:

- `RootShellRoute` owns the shared `LibraryViewModel`
- `TodayScreen` and `LibraryScreen` both render from the same `LibraryUiState`
- `TodayScreen` sends existing `LibraryAction` values for download/cancel/remove

iOS:

- `RootShellView` owns one `LibrarySharedViewModel`
- the model is activated once with `.task`
- `TodayScreen` and `LibraryScreen` both consume that same adapter

## Verification

Last verified after `92b29eb`:

```zsh
cd /Users/mahdi/pardis-kmp
./gradlew :PardisAndroidApp:compileDebugKotlin --no-daemon
```

Result: build succeeded.

```zsh
cd /Users/mahdi/pardis-kmp
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'generic/platform=iOS Simulator' build
```

Result: build succeeded.

## Next Work

Continue in this order:

### 1. Bedtime / Lullabies

Build the native Bedtime screen with:

- dark/night tokenized hero panel
- lullaby/loop-mode surface
- calm story or lullaby list
- shared state contract only if existing Library state is not enough

### 2. Rewards

Build native progress primitives:

- achievement card
- reading streak/status panel
- vocab recap surface
- collection progress tiles

This likely needs new shared progress/rewards state rather than local-only UI.

### 3. You / Child Profile

Build the profile area around the future child profile/PIN contract:

- profile summary card
- family settings entry points
- offline/manage-downloads entry point
- PIN/auth placeholders only until shared contracts exist

### 4. Assets

Fonts and final branded icons are not yet in the app. Current font roles and icon primitives are structural placeholders that compile and keep UI native.

## Resume Checklist

Start every follow-up with:

```zsh
cd /Users/mahdi/pardis-kmp
git status --short --branch
git log --oneline -5
```

Expected clean baseline at this handoff:

```text
## main...origin/main
92b29eb Add native Today tab composite
f35b689 Add native root tab shell
1a19782 Add native font roles and icon primitives
f2f0cee Build native theme and story composites
2006bef Extract native reader and library primitives
```

If `main` has moved, inspect the new commits before continuing.

## Rules To Keep

- Do not convert the standalone HTML/React design directly into Compose or SwiftUI.
- Extract tokens, primitives, then native composites.
- Do not put UI in `shared/`.
- Do not add platform types to shared `UiState`.
- Do not add navigation events to shared ViewModels.
- Use Pardis tokens only; no raw colors or ad hoc dimensions for new visuals.
- Keep Persian RTL scoped to Persian content.
