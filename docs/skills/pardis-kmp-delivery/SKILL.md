---
name: pardis-kmp-delivery
description: Build, review, and verify Pardis Kotlin Multiplatform content reader app. Shared logic (ViewModels, UiState, domain, data) + native Android Jetpack Compose + native iOS SwiftUI. Follows the KMP architecture in docs/kmpSkill.md. Use Pardis design tokens/palette only (saffron, indigo/lapis, mint, lilac, warm cream backgrounds from web src/lib/design/). 
---

# Pardis KMP Delivery Skill

## Overview

Pardis KMP is the mobile content reader for the Pardis Persian heritage stories/lullabies platform (bilingual FA/EN, MP4 video when available per `videoReady` + cues, per-page illustrations + narration audio fallback, vocab, games, offline, child profiles/PIN).

**Architecture (non-negotiable, from docs/kmpSkill.md):**
- Shared logic in `shared/` (feature VMs with single `uiState: StateFlow`, `onAction`, UiState/Action contracts, SharedInit) and `core/*` (model, domain, data, network, database, di).
- **Native UIs only**: Android Jetpack Compose in `app/`, iOS SwiftUI in `iosApp/`.
- No shared Compose/SwiftUI screens or theme code.
- DI: Koin, `SharedInit.init(platformModules)`, qualified platform bindings.
- Use SKIE in `shared` for iOS framework.
- Design: **Pardis palette only** (see web `src/lib/design/themes/neutral.ts` and tokens: saffron #F08A2D, indigo #2436A1, mint #34B57F, background #FAF6EE, ink #14111B, etc.). Tokenised via `design-system/`. Generate platform tokens. 

Reference:
- Parent Pardis web: `AGENTS.md` (top-level in ../pardis), CLAUDE.md, design tokens.
- This project's `docs/kmpSkill.md` (architecture bible, mirror of Codex cross-platform skill).
- `docs/code-rules.md` (cross-cutting: no magic, no error swallow, tokenised design, no platform leakage).
- `.github/instructions/kmp.instructions.md` (review projection).

## Required Context (read before any change)

1. `docs/kmpSkill.md` — full architecture, ViewModel rules, DI, iOS adapters, SKIE, error handling, no shared UI.
2. Pardis web design: `../pardis/src/lib/design/` (tokens, neutral theme for palette) — use these hex/semantic names for tokenisation.
3. This `SKILL.md` + `docs/code-rules.md` + severity.
4. Current data contracts: `core/model/Story.kt` etc. (must stay in sync with web `src/lib/content.ts`).
5. Supabase public data: stories, story_pages, vocab_terms are publicly readable (anon key ok for reads). Use for core/network.

## Workflow

1. `git status --short` first.
2. Put **all** business logic, VMs, use cases, repos, mappers in `shared/commonMain` or `core/*/commonMain`. `androidMain`/`iosMain` only for platform adapters/bridges.
3. Android UI **only** in `app/src/main/java/.../ui/` (Compose, native theme using generated Pardis tokens).
4. iOS UI **only** in `iosApp/iosApp/` (SwiftUI, native, adapters using @Observable + .task + Skie flows).
5. Design tokens: Edit in `design-system/` first (Pardis palette). Generate or manually keep `generated/android/PardisTokens.kt` and `generated/ios/PardisTokens.swift`. Use tokens in UI, not hex/dp literals.
6. For content features (library, reader):
   - Models in core/model.
   - Use cases in core/domain.
   - Fetch (Ktor to Supabase public REST) + mappers in core/data or network.
   - Offline (SQLDelight cache of stories + pages + assets) in core/database.
   - Feature VM in shared/.../ (single uiState, actions).
   - Wire in shared DI + SharedInit.
   - Consume in native shells (callbacks for nav, no shared nav ownership).
7. MP4 video: When story.videoReady and urls present, use native player (Media3 on Android, AVPlayer on iOS) + subtitle cues (build from narration durations + intro/outro, match web VideoReader logic). Fallback to image + per-page audio pager.
8. Secrets/config: Only in platform bootstrap (Android BuildConfig/local.properties or iOS xcconfig). Never in shared/commonMain.
9. Use only Pardis design tokens and palette in UI.

## Verification (run narrowest then broaden)

```bash
./gradlew test :PardisAndroidApp:assembleDebug
./gradlew :shared:iosSimulatorArm64Test :core:data:iosSimulatorArm64Test :core:domain:iosSimulatorArm64Test :core:network:iosSimulatorArm64Test
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'generic/platform=iOS Simulator' build
```

Add semantic snapshot or UI tests in shells as features grow.

When adding new feature (e.g. reader, games):
- Add to shared + core.
- Update native shells.
- Add to design-system tokens if new visual.
- Update roadmap.

## Pardis-specific

- Content is public + offline-first friendly (download full story bundle: prose, images, audio, optional MP4).
- Bilingual (FA primary in many places).
- Child profiles + PIN gate (Supabase Auth + local secure + RLS for family data).
- Lullabies separate but similar track.
- Align with web agent pipeline output (stories have illustration_bible, sound_plans, video urls when rendered).
- Use Pardis web palette exclusively: saffron, lapis/indigo, mint, lilac, warm cream #FAF6EE bg, ink #14111B. See neutral.ts for full list.
- Tokenise everything: no raw colors, no magic spacing in UI code.

## Common traps (from kmpSkill)

- Platform leakage into commonMain → P0.
- Shared VM doing nav or owning Compose/SwiftUI types → P1.
- Magic numbers/hex in UI instead of tokens → P1.
- Error swallowing in data/network → P0/P1.
- Inline strings instead of semantic in UiState + platform resolve → P1.
- Not using .task + for await for iOS flows, or not scoping VM lifetime → P1.
- Wrong DI: direct VM() construction in UI instead of koinViewModel or provider → P1 (leaks scope).

See full checklist in docs/kmpSkill.md §15.

## Integration with Pardis web

- Models must match web (update both when changing story schema).
- Public Supabase data is source of truth for reader content.
- Agents/MCP in web repo produce the stories; this app consumes.
- Reference top-level Pardis AGENTS.md for overall project rules; this skill is the KMP-track extension.

Never commit keys, local.properties with secrets, etc.

This skill is the delivery contract for the KMP reader track. Bind reviews and agents to it + the architecture doc.
