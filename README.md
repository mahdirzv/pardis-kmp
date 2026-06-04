# Pardis KMP (Reader App) — Native UI + Shared Logic

This KMP project follows the **Beforely architecture** (see https://github.com/pooyanmajd/Beforely and its `docs/kmpSkill.md` + `docs/skills/beforely-kmp-delivery/SKILL.md`):

- **Shared logic** in Kotlin Multiplatform (`shared/` + `core/*`): business rules, data contracts, ViewModels (with `uiState: StateFlow` + `onAction`), use cases, repositories, DI (Koin), offline (SQLDelight), network.
- **Native UIs only**:
  - Android: Jetpack Compose in `app/` (the `:PardisAndroidApp` module).
  - iOS: SwiftUI in `iosApp/` (imports the `Shared` framework produced by the `shared` KMP module, using SKIE for great interop).

No shared Compose or shared SwiftUI screen code. Platform shells stay thin: they render shared `UiState` and forward `Action`s (plus own navigation/theme).

## Structure (matching Beforely)

```
pardis-kmp/
├── app/                  # Android native shell (Compose UI, theme, nav, MainActivity, Application bootstrap)
├── iosApp/               # iOS native shell (SwiftUI, xcodeproj, adapters for Shared VMs)
├── shared/               # Feature-scoped VMs, UiState, Action, SharedInit, iOS ViewModelProvider
├── core/
│   ├── model/            # Pure data contracts (Story, StoryPage, VocabItem...)
│   ├── domain/           # Use case interfaces + repo interfaces
│   ├── data/             # Repo impls + mappers
│   ├── network/          # Ktor / Supabase client
│   ├── database/         # SQLDelight for offline stories/pages/vocab/progress
│   └── di/               # Core Koin modules
├── gradle/...
├── settings.gradle.kts   # Includes :PardisAndroidApp (maps to app/), core/*, :shared
└── ...
```

See the Beforely files for the full contract:
- `docs/kmpSkill.md` (architecture rules)
- `AGENTS.md` style operating model (Pardis has its own in the web repo)
- How `SharedInit.init(platformModules)` + Koin assembles everything
- iOS adapters using `@Observable` + `.task` + Skie flows + `apply(state)`
- Android: `koinViewModel()`, `collectAsStateWithLifecycle()`, Route/Screen split with callbacks for nav

## Current Scaffold State

Basic library feature wired end-to-end:
- Shared `LibraryViewModel` + `LibraryUiState` + `LibraryAction`
- Android Compose screen observing it
- iOS SwiftUI + Observable adapter observing the Skie flow

Real content fetching (public Supabase stories + pages), MP4 video player (per your clarification: only pre-rendered MP4s + cues), audio page reader, offline downloads, child profiles/PIN, etc. to be layered on top following the same boundaries.

## Setup (see previous requirements check)

1. Complete JDK link + PATH (from earlier).
2. Open this folder in Android Studio (install KMP plugin if prompted).
3. For iOS: full Xcode + the Shared framework will be produced on build.

Run checks (once set up):
```bash
./gradlew test :PardisAndroidApp:assembleDebug
# iOS build via Xcode or xcodebuild after framework
```

Follow the same rules as Beforely for any changes: shared logic here, native UI in the shells, no platform leakage into commonMain, etc.

Update this README + add project-specific docs as the reader features grow.

This structure keeps the Pardis web (authoring, admin, agents) and the mobile reader cleanly separated while sharing the canonical data contracts.
