# Handoff — Onboarding, Native Tab Bars, QA Hardening, PardisApp Split

_Date: 2026-06-09_

## Current State

- Branch: `main`, clean, synced with `origin/main`.
- `HEAD`: `d70121b refactor(android): prune split fallout — dead placeholder + unused imports`
- All work below is committed and pushed. Only untracked path is `.playwright-mcp/` (gitignored scratch).

## Environment / Build Facts (this Mac)

- **No full Xcode** (only Command Line Tools): no iOS SDK, no `simctl`. The iOS app and the
  `:shared` iOS test targets (`iosSimulatorArm64Test`/`iosX64Test`) **cannot build/link/run here** —
  they need an Xcode machine.
- **Shared/common unit tests run on the JVM** via `withHostTest` on `:shared`:
  `./gradlew :shared:testAndroidHostTest` (no Xcode). Put shared/core tests in
  `shared/src/commonTest/kotlin/...`. (The old `app/src/test` workaround was removed.)
- **Build env:** `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`.
- **Android compile:** `./gradlew :PardisAndroidApp:compileDebugKotlin --no-daemon`
- **Debug APK:** `app/build/outputs/apk/debug/PardisAndroidApp-debug.apk` (NOT `app-debug.apk`).
- **Module/appId:** Gradle module `:PardisAndroidApp` (dir `app`), appId `app.pardis.reader`,
  MainActivity `app.pardis.android.MainActivity`.
- **Device:** serial `RFCR11CB9JM`, screen 720×1600 (adb tap coords are device-space; bottom tab
  centers ~y=1488: Today≈102, Library≈230, Bedtime≈359, Rewards≈488, You≈617). adb at
  `~/Library/Android/sdk/platform-tools/adb`. NOTE: the device intermittently drops off adb.
- **Canonical design:** v2 handoff at `~/Downloads/design_handoff_rivana_kmp 2/` (note trailing " 2").

## Shipped This Session (all on `main`)

1. **Lullaby sleep-timer `BackHandler`** — system back closes the sheet, not the route.
2. **Onboarding profile picker (Android)** — "Who's reading tonight?" gate. New shared contract:
   `ChildProfile`/`ProfileTone` (`core/model`) → `ProfileRepository` + use cases (`core/domain`/`data`)
   → `ProfileViewModel` (`shared/profile`) → Koin wired (`CoreModules`, `SharedInit`,
   `PardisViewModelProvider`). Persists selection via SQLDelight `app_setting` table (in-memory
   fallback when no DB). Gates launch, switch-profile route from You, drives Today greeting + You card.
   Spec: `docs/superpowers/specs/2026-06-07-onboarding-profile-picker-design.md`;
   plan: `docs/superpowers/plans/2026-06-07-onboarding-profile-picker.md`. **iOS: shared contract only,
   no SwiftUI screen yet.**
3. **Native bottom navigation** — Android: hand-rolled bar → **Material 3 `NavigationBar`** (brand-tinted,
   labels kept), hosted via `Scaffold`. iOS: custom ZStack bar → **native `TabView`** (Liquid Glass on
   iOS 26, brand `.tint`, per-tab `NavigationStack`, SF-Symbol tab items).
4. **commonTest migration** — enabled `withHostTest` on `:shared`; profile tests moved to `commonTest`,
   run on JVM (no Xcode).
5. **Subtitle cue fix** — `ReaderViewModel` no longer averages Fa/En durations; pure
   `buildVideoCueTimeline()` syncs to the single playing track (Fa if present, else En). Tested.
6. **Offline test coverage** — `OfflineDownloadManagerTest` (success/failure/progress/cancel/refresh).
7. **Koin fail-fast** — `SharedInit.doInit` throws if `OfflineAssetCache` resolved to `NoOp` despite
   platform modules being loaded (`validateOfflineCacheWiring`, tested).
8. **PardisApp.kt split** — 1500 → 307 lines. New files: `PardisTodayScreen`, `PardisBedtimeScreen`,
   `PardisRewardsScreen`, `PardisYouScreen`, `PardisLibraryScreen`, `PardisRivanaData`; `ReaderRoute`
   moved into `PardisReaderScreen`; tone helpers → `PardisPrimitives`. Then pruned 279 unused imports +
   removed dead `PardisPlaceholderTabScreen`/`else`/`PardisRootTab.subtitle`.

Tests: `:shared:testAndroidHostTest` green — 17 tests (Profile 3+2, BuildVideoCueTimeline 4,
OfflineDownloadManager 5, SharedInit 3).

## Verification Status

- ✅ **iOS native TabView + offline caching** — user verified on device (2026-06-09).
- ⏳ **Android M3 NavigationBar** — compiles, but on-device look/insets NOT yet eyeballed (device had
  dropped off adb). Quick visual check needed when connected.

## What's Left

### Evaluator report (2 of 6 remain; see report text in session)
- **#4 iOS streaming cache** — `IosOfflineAssetCache` buffers whole files in memory (`http.get().body()`);
  stream chunk-by-chunk like Android (`ByteReadChannel`). Code can be written here but needs Xcode to verify.
- **#5 cache eviction / quota** — no LRU/size cap; assets grow unbounded under `cache/pardis/assets/`.
  **Needs a product decision: quota (e.g. 500 MB?) and LRU vs manual "manage downloads".**
- ✅ Done: #1 Koin hardening, #2 commonTest, #3 offline tests, #6 subtitle cues.

### iOS parity (largest chunk) — these shipped Android-only, need SwiftUI equivalents
- **Onboarding profile picker** + launch gate (iOS has the `ProfileViewModel` contract via
  `PardisViewModelProvider.profileViewModel()`, no screen).
- **Lullaby player**, **Character**, **reader v2 rework**, **Finish** celebration.

### Feature backlog (named earlier, not started)
- **Parents'/Parent corner** ("I'm a parent" + Settings entries are stubs).
- **Dark-mode wiring** (You-tab toggle is decorative).
- "Add child" creation flow (intentional stub).

### Optional polish
- `PardisPrimitives.kt` (~1089 lines) could be grouped (icons/cards/pills/reader-bits). Low priority.

## Rules To Keep

- Native UI per platform; **no shared UI** (no Compose/SwiftUI in `shared`).
- **kmpSkill.md** (`docs/kmpSkill.md`): design tokens only — no raw colors/dims; route through
  `PardisColors`/`PardisGradients`/`PardisSpacing`/`PardisRadius`. Route → Screen → Component split;
  `koinViewModel()` only in Routes; navigation is callback-based, never via shared events.
- No platform types in shared `UiState`; one `uiState: StateFlow` per screen VM.
- Persian RTL scoped to Persian content only (`PersianReaderInline`/`Paragraph`, `PersianReaderText`).
- Branch for feature work (don't commit straight to `main`); verify before merge; ship verified work
  to `main` and push.
- Shared/core tests live in `commonTest`, run via `:shared:testAndroidHostTest` (no Xcode here).
