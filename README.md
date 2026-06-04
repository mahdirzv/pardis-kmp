# Pardis KMP (Reader App) — Shared Logic + Native UIs

Pardis is a bilingual (Farsi/English) Persian heritage content platform for diaspora families. This is the Kotlin Multiplatform reader app for stories and lullabies.

## Architecture

- **Shared logic** in Kotlin Multiplatform (`shared/` + `core/*`): business rules, data contracts (Story, StoryPage, etc.), ViewModels (with `uiState: StateFlow` + `onAction`), use cases, repositories, DI (Koin), offline storage (SQLDelight), network.
- **Native UIs only**:
  - Android: Jetpack Compose in `app/` (the `:PardisAndroidApp` module).
  - iOS: SwiftUI in `iosApp/` (imports the `Shared` framework produced by the `shared` KMP module).

No shared UI code across platforms. Platform shells stay thin: they render shared `UiState` and forward `Action`s (plus their own navigation and theming).

## Structure

```
pardis-kmp/
├── app/                  # Android native shell (Compose UI, theme, nav, MainActivity, bootstrap)
├── iosApp/               # iOS native shell (SwiftUI, adapters for shared VMs)
├── shared/               # Feature-scoped ViewModels, UiState, Action, SharedInit, platform providers
├── core/
│   ├── model/            # Pure data contracts (Story, StoryPage, VocabItem, etc.)
│   ├── domain/           # Use cases + repository interfaces
│   ├── data/             # Repository implementations + mappers
│   ├── network/          # Ktor client for Supabase public data
│   ├── database/         # SQLDelight for offline caching
│   └── di/               # Core Koin modules
├── design-system/        # Tokenised design using Pardis palette
├── gradle/...
├── settings.gradle.kts   # Multi-module setup
└── ...
```

## Design System

Fully tokenised using the Pardis palette and tokens from the web project (`src/lib/design/`).

- `design-system/tokens.json`
- Generated platform tokens in `design-system/generated/`
- Use `PardisColors`, `PardisSpacing`, etc. in native UIs. No raw hex or magic values.

See `design-system/MDS.md` and the web design tokens for the full contract.

## Getting Started

See the parent Pardis web project `AGENTS.md` for overall conventions.

For KMP-specific:
- `docs/kmpSkill.md` — architecture and coding patterns.
- `docs/skills/pardis-kmp-delivery/SKILL.md` — delivery, review, and implementation guidance.
- `.github/instructions/kmp.instructions.md`

## Data Source

Content is fetched from the public Supabase instance used by the Pardis web project:

- URL: https://tpjgnlcporgnlrjwjufq.supabase.co
- Public tables (stories, story_pages, vocab_terms, etc.) are readable with the anon key.

The anon key and URL are configured per-platform (see platform bootstrap for how secrets/config are provided).

## Verification

```bash
./gradlew test :PardisAndroidApp:assembleDebug
# iOS: build via Xcode after generating framework
```

## Roadmap

See `docs/ROADMAP.md`.

## Contributing

Follow the rules in `docs/code-rules.md`, `docs/severity.md`, and the KMP skill docs. All shared logic must stay in `shared/` and `core/*`. UI lives only in the native shells.

Never commit secrets or keys.

This project shares data contracts with the main Pardis web app — keep models in sync with `src/lib/content.ts` in the web project.