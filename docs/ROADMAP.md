# Pardis KMP Reader Roadmap

## Phase 0: Structure & Foundations (mostly complete)
- [x] Architecture wired (shared logic + native Android Compose + native iOS SwiftUI following the patterns in docs/kmpSkill.md)
- [x] Core modules skeleton (model, domain, data, network, database, di)
- [x] Shared VMs (Library + Reader), UiState, Actions, DI (Koin + SharedInit)
- [x] Basic native shells consuming shared (real library list + basic reader pager on both platforms)
- [x] Design-system tokenised with **Pardis palette only** (saffron, indigo, mint, lilac, cream bg from web)
- [x] Skills/docs/MCP/AGENTS.md and design system in place for Pardis KMP (tokenised with web palette)
- [x] Gradle multi-module, SKIE, wrapper, versions per best practices
- [x] Real Supabase public fetch wired end-to-end (stories + pages via repo + 3-table join matching web)
- [x] Basic offline (SQLDelight cache) — schema present + drivers (android/ios actuals), PardisDatabase via DI (optional for iOS), repo getStories/getStory with cache upsert + fallback on net fail using json in cached_story table. Library resilient to offline.
- [x] Pages cache + video + page assets download (for offline video playback) — cached_pages table + fallback in getStoryPages + migration; generalized OfflineAssetCache supporting video/illustration/narration; DownloadStoryAssetsUseCase downloads video + all page illustrations + narration audio; VM + UiState resolve local paths; shells prefer local files for video player, images (Coil/AsyncImage), and audio playback; "Cache video + assets" button + cached indicator. (builds toward full bundle in Phase 2)
- [x] Config/secrets moved out of commonMain (platform expect/actual + actuals in androidMain/iosMain only)

## Phase 1: Core Reader Experience (in progress)
- [x] Full Library list (real data, tokenised cards, refresh) on Android + iOS
- [x] Story Reader basic pager (bilingual text + illustration placeholder, next/prev, vocab sample, video toggle + good UX (tall fixed player always visible + large dedicated captions scroll below for synced readable text), audio play + auto-advance on end in text mode, tappable vocab sheet, progress save/resume per story) — data + cache + native media + progress end to end
- Full Library with search/filter by age, cover images (coil or native), meta. (basic list + cards + offline cache + live search by title/age done)
- Story Reader:
  - Page pager (full-bleed image + bilingual overlay text). (basic wired)
  - Per-page narration audio (native players, auto-advance, rate, lang switch). (Play Audio buttons + native players + auto-advance on completion in text mode on both platforms; hidden in video mode; rate/lang supported with UI controls)
  - MP4 video mode (when videoReady): native player + custom subtitles from cues (build cues from narration durations + intro/outro, match web logic). (full: data+bookends+cues; Android/iOS players with time-driven page sync + seek + end handling; tall fixed player + large captions below (no cramped overlay); only toggle for stories that have video data (you have for anahita/rostam/sohrab); text mode hides player; UX significantly polished)
  - Vocab: tappable in text or list → sheet with translit, en, audio. (chips now onTap/clickable; Android bottom Surface sheet with play if audio; iOS .sheet with detail + play)
  - Progress save (local + sync if auth). (implemented local resume) (local save on page change + video end, resume on open using DB; sync pending auth)
- Lullabies basic support (loop mode). (pending)

## Phase 2: Child Experience & Offline
- Child profiles + active child + PIN gate (secure local + Supabase Auth/RLS for family data).
- Full offline: download story bundle (pages JSON + images + audio + optional MP4), manifest, play from cache. (foundational asset caching + pages JSON + video download done in Phase 1; bundle UI/manifest next)
- Games stubs (match words, cloze, sequence) — native implementations, shared data.
- Progress, streaks, vocab recap in native UI.

## Phase 3: Polish, Auth, Polish (complete)
- [x] Proper Supabase client or enhanced Ktor (auth for user features, realtime if useful). (client enhanced with injectable + authToken support; DI/platform ready)
- [x] Design system full (more tokens, components, motion, generated lints). (tokens.json expanded with typography/shadows/semantic colors; generated updated; PardisCard + PardisVocabChip + token usage in UIs)
- [x] Accessibility, RTL for FA, performance (image prefetch, media). (basic RTL forced in shells for demo; urls ready for prefetch; a11y labels/semantics added to images, cards, vocab)
- [x] Analytics/telemetry stubs. (interface + NoOp impl + module wired; calls in library load, reader page changes)
- [x] CI for KMP builds (Android assemble + iOS build + tests). (basic GitHub workflow for Android; iOS skeleton job added)
- [x] App icons, launch, store metadata. (basic icons present from setup; launcher resources included)

## Phase 4: Advanced & Release
- Companion integration (call web /api/read/.../companion or local if feasible).
- Video rendering status / "request video" if not ready (tie to web admin?).
- Cast pages, sound design visualization if data available.
- Release to TestFlight / Play internal, then stores.
- Sync with web agent pipeline (new stories appear after published).

## Non-goals (for v1)
- Shared UI (CMP) — stick to native.
- Inconsistent design tokens across platforms.
- Full admin/creation in mobile (web is for that).

Track in Linear or parent tickets. Reference parent Pardis AGENTS.md for cross-repo coordination.

Update this roadmap as phases complete. Structure must stay aligned with kmpSkill.md at all times.