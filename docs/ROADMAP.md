# Pardis KMP Reader Roadmap

## Phase 0: Structure & Foundations (current)
- [x] Beforely-style architecture wired (shared logic + native Android Compose + native iOS SwiftUI)
- [x] Core modules skeleton (model, domain, data, network, database, di)
- [x] Shared VMs (Library + Reader stubs), UiState, Actions, DI (Koin + SharedInit)
- [x] Basic native shells consuming shared (library list demo on both platforms)
- [x] Design-system tokenised with **Pardis palette only** (saffron, indigo, mint, lilac, cream bg from web)
- [x] Skills/docs/MCP/AGENTS.md imported + adapted from Beforely + Pardis web
- [x] Gradle multi-module, SKIE, wrapper, versions per best practices
- [ ] Real Supabase public fetch wired end-to-end (stories + pages)
- [ ] Basic offline (SQLDelight cache)

## Phase 1: Core Reader Experience
- Full Library with search/filter by age, cover images (coil or native), meta.
- Story Reader:
  - Page pager (full-bleed image + bilingual overlay text).
  - Per-page narration audio (native players, auto-advance, rate, lang switch).
  - MP4 video mode (when videoReady): native player + custom subtitles from cues (build cues from narration durations + intro/outro, match web logic).
  - Vocab: tappable in text or list → sheet with translit, en, audio.
  - Progress save (local + sync if auth).
- Lullabies basic support (loop mode).

## Phase 2: Child Experience & Offline
- Child profiles + active child + PIN gate (secure local + Supabase Auth/RLS for family data).
- Full offline: download story bundle (pages JSON + images + audio + optional MP4), manifest, play from cache.
- Games stubs (match words, cloze, sequence) — native implementations, shared data.
- Progress, streaks, vocab recap in native UI.

## Phase 3: Polish, Auth, Polish
- Proper Supabase client or enhanced Ktor (auth for user features, realtime if useful).
- Design system full (more tokens, components, motion, generated lints).
- Accessibility, RTL for FA, performance (image prefetch, media).
- Analytics/telemetry stubs.
- CI for KMP builds (Android assemble + iOS build + tests).
- App icons, launch, store metadata.

## Phase 4: Advanced & Release
- Companion integration (call web /api/read/.../companion or local if feasible).
- Video rendering status / "request video" if not ready (tie to web admin?).
- Cast pages, sound design visualization if data available.
- Release to TestFlight / Play internal, then stores.
- Sync with web agent pipeline (new stories appear after published).

## Non-goals (for v1)
- Shared UI (CMP) — stick to native.
- Beforely design language.
- Full admin/creation in mobile (web is for that).

Track in Linear or parent tickets. Reference parent Pardis AGENTS.md for cross-repo coordination.

Update this roadmap as phases complete. Structure must stay aligned with kmpSkill.md at all times.