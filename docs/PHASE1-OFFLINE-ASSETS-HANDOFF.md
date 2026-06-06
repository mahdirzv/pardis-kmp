# Pardis KMP Reader – Phase 1 Offline Video + Assets: Hands-Off Status Report

**Branch:** `feature/phase1-offline-page-assets` (origin up-to-date as of last push)  
**Current HEAD (example):** `aff350c` – "fix(android cache): explicit OkHttp engine + long read timeouts..."  
**Date of this report:** 2026-06-05 (session context)  
**Maintainer note:** This is a self-contained handover document. Follow the parent Pardis `AGENTS.md` (at `../pardis/AGENTS.md`) + this project's `docs/kmpSkill.md` + `docs/skills/pardis-kmp-delivery/SKILL.md` + `.github/instructions/kmp.instructions.md` + `docs/code-rules.md` strictly.

## ✅ RESOLUTION UPDATE — 2026-06-05: "Download failed" root cause found & fixed

The "Cache video + assets → Download failed" blocker is **resolved**. Important correction to the
original report below: the real Android asset cache had in fact **never run** — so the "works
end-to-end on Android" claims in *Current State* were not actually true until these commits.

**Root cause (a Koin DI bug, masked twice):**
1. `SharedInit` loaded modules as `platformModules + pardisCoreModules + pardisSharedModules`.
   Koin resolves duplicate definitions **last-wins**, so `CoreModules`' `NoOpOfflineAssetCache`
   (whose `downloadAssetIfNeeded` is `return null`) **overrode** the real `AndroidOfflineAssetCache`.
   Every symptom matched: instant failure, **no `cache/pardis/assets` dir created**, **zero
   `AndroidOfflineAssetCache` logs**, while the story still loaded (a different binding).
2. The real binding was broken anyway: `AndroidOfflineAssetCache(get())` asked Koin for a
   `Context` that was never registered as a bare `single<Context>`. Dormant while the no-op won;
   it surfaced as a startup crash the instant #1 was fixed — proving the no-op had silently won
   all along (the real cache had never executed).

Network/URL/Supabase policy/file size/memory/timeouts were all measured and **ruled out** (a plain
GET pulls the full 33 MB in ~1.3 s, no auth).

**Fixes (commits `b90471d`, `9285cd4`):**
- `SharedInit`: load `platformModules` **last** so real impls override core no-op defaults (also fixes iOS).
- `PardisApplication`: `AndroidOfflineAssetCache(applicationContext)` (like `provideSqlDriver` above it).
- Hardening: `expectSuccess=true` on both caches (HTTP errors now throw + log); Android streams
  the video to disk (`bodyAsChannel().copyTo`) instead of a full `ByteArray`; iOS gains
  `HttpTimeout` + size guard + failure logging; `DownloadStoryAssetsUseCase` returns a
  `StoryAssetsResult` so partial success is reported honestly and offline images/audio survive a
  video failure; the ViewModel's download `catch` blocks now log instead of swallowing silently.

**Verified on-device (Samsung Galaxy A32):** `rostam-and-sohrab` caches all **68 assets (~109 MB)**
under `cache/pardis/assets/rostam-and-sohrab/` — `video-fa.mp4` byte-exact (33,025,448), 22
illustrations, 44 narrations (fa+en), cover. Android `assembleDebug` + iOS `compileKotlinIosArm64`
both green. The *What Is Left* items below for "diagnose download failed", "streaming download",
and "partial success UX" are now **done**.

---

## Goal of This Work

Deliver **foundational offline support** so that "videoReady" stories (e.g. Anahita, Rostam, Sohrab) can be fully consumed without network:

- The polished tall fixed video player (with time-synced captions/scrollable bilingual text below) works from a local MP4 file.
- Per-page illustrations and narration audio (FA/EN) also resolve to local files for captions + the "Play Audio" controls (with lang + 0.5x–2x rate).
- Covers resolve locally for the library.
- Explicit user action ("Cache video + assets") + automatic trigger (when entering Video mode on a videoReady story that isn't cached yet).
- Library shows offline status, supports "Show only offline cached" filter + search, and uses local covers.
- Clear / reset per story.
- Progress feedback during download.
- Everything still works when the device is offline (after a successful cache).
- Follows the canonical architecture: heavy logic in `core/*` (domain + data + db + network) + `shared/` (VM + UiState + Actions), thin native shells (`app/` Compose + `iosApp/` SwiftUI) only for players, images, and UI.

This is the "Phase 1" foundation. True "one-tap story bundle + manifest + background + selective" download is Phase 2 (see ROADMAP).

**Non-negotiables observed:**
- No new design tokens without adding them first in `design-system/`.
- Public Supabase MP4s + assets only (no auth required for content in this phase).
- Builds + verification after every change (`:PardisAndroidApp:assembleDebug` + iOS `compileKotlin*`).
- Commits + pushes only on feature branches (main is protected).
- All new visuals / controls use existing Pardis palette (saffron, indigo, mint, etc.).

## What Has Been Done (Summary + Key Evidence)

### Core Offline Infrastructure
- Generalized `OfflineAssetCache` (interface in `core/domain`, Android impl using `cacheDir/pardis/assets/{slug}/...`, real posix-based iOS impl).
- `DownloadStoryAssetsUseCase` + impl: counts video + cover + all page illustrations + fa/en narrations; parallel best-effort downloads with `onProgress` callback; returns video local path (or sentinel) on success.
- DB: `cached_story` (rich metadata) + `cached_pages` (for offline reader + cues) + migration `1.sqm` + drivers updated to use `Pardis.Schema` (fixed the original "no such table" errors).
- `GetLocal*PathUseCase` + `ClearStoryAssetsUseCase`.
- StoryRepository now does rich upserts for video fields (library `getStories` select was expanded to include `video_url_*` + audio so cached entries are useful for fallbacks).

### Shared Layer (VM / State / Actions)
- `ReaderUiState`: `localVideoUrlFa/En`, `localIllustrationUrls: Map<Int,String>`, `localNarrationUrls`, `isDownloadingVideo`, `downloadProgress`, `preferredNarrationLang`, `playbackRate`.
- `ReaderAction`: `DownloadVideo`, `ClearAssets`, `SetNarrationLang`, `SetPlaybackRate`, `ToggleVideo`, `PlayNarration`.
- `ReaderViewModel`: auto-trigger (silent) on video enter, manual path with progress, success → `updateLocalsAfterSuccessfulDownload` + resolve page assets, clear resets all locals, rate/lang wired, try/catch + non-fatal error reporting via progress (not `errorMessage`).
- Library side: `cachedStorySlugs`, `localCoverUrls`, `showOnlyCached` filter + search; `LibraryViewModel` computes cached set + resolves covers.

### Android Shell (`app/`)
- Tall fixed player area (380dp) + dedicated scrollable captions below (huge UX win over cramped overlay).
- `ExoPlayer` now stable (one instance per reader session) + `LaunchedEffect(videoUrl)` to `setMediaItem`/`prepare` on remote→local switch or toggle (prevents repeated create/release cycles).
- Auto-pause of player while `isDownloadingVideo`.
- Transport split into two rows (main nav + video toggle + cache/clear; separate accessible audio row for lang/rate/play/clear in text mode) + labels + "✓ Offline" header badge.
- Coil `AsyncImage` prefers local illustration/cover.
- MediaPlayer for per-page narration with rate (API 23+) + auto-advance + completion handling.
- Clear button in both modes; error toast only for real load errors.

### iOS Shell (`iosApp/`)
- Real `IosOfflineAssetCache` (posix open/write/close + mkdir -p, matches Android paths exactly).
- Wired in `iosApp.swift` via `IosPlatformModuleKt.iosOfflineAssetCacheModule`.
- `ReaderSharedViewModel` + `LibrarySharedViewModel` bridge the locals + actions.
- `ContentView` + `VideoPlayerView` use local paths for `AVPlayer`, show cache button + progress + clear + "✓ Offline" badges, lang/rate controls, split layout mirroring Android.
- AVPlayer rate support for narration.

### UX / Feedback / Robustness (Recent Focus)
- Real progress ("Downloaded X/Y assets...") + "Download complete!" flash.
- "Cache video + assets" only shown in video mode for video stories; "✓ Video + assets cached" + Clear when done.
- Auto cache on video mode entry (silent fail, no error spam).
- Clear works in video mode too.
- Reliability fixes for the exact symptom the user reported ("click cache → detachBuffer/cancelBuffer spam + download faild"):
  - Rich cache select.
  - try/catch + progress-area error (never kicks the player UI).
  - Explicit OkHttp engine + 15-minute read timeouts + `Log.e("AndroidOfflineAssetCache", ..., t)` on any asset failure (with URL, kind, subKey).
  - Relaxed pages-empty early return in use case.
- Player no longer recreated on every source change → far less MediaCodec/BufferQueue noise.

### Docs & Process
- ROADMAP.md and PHASE-3-PLAN.md kept up to date (feature marked done + reliability notes added).
- All changes on feature branch, verified builds (Android assembleDebug + iOS compileKotlin*), committed + pushed.
- No design token violations.

**Evidence:** See the commit list on the branch (aff350c, 79d3f37, 3e682ef, 99c4c68, 93ce4f3, 04ed056 for real iOS posix cache, many UX split commits, etc.).

## Current State (What Actually Works End-to-End)

- Open library → see stories (some with "✓ Offline" if previously cached).
- Toggle "Show only offline cached".
- Search.
- Open a videoReady story → see tall player + large captions (or text mode + illustration).
- "Video mode" / "Text mode" toggle.
- In video mode (uncached): "Cache video + assets" button.
- Click it → progress updates in the button, parallel download of video + cover + all page assets.
- On success → player seamlessly switches to local file (via the LaunchedEffect), locals resolved, "✓ Video + assets cached" + Clear appears, header "✓ Offline" badge.
- Go offline (airplane mode) → video still plays, captions have local images, Play Audio works with local narration + rate/lang.
- Library cards use local covers when available.
- Clear removes everything for the slug and resets UI state.
- Auto path on video enter (for convenience on videoReady stories).
- Progress save/resume still works.
- Both platforms have parity for the above (modulo native player differences).

The "Cache video + assets" flow is the main explicit user gesture for offline.

## What Is Left / Open / Next (Prioritized)

### Immediate / Polish (still on this branch or quick follow-up)
- ✅ **DONE — Diagnose "download failed".** Root cause was the Koin DI module-order bug (no-op cache
  overrode the real one) + an unresolvable `Context` in the real binding. Not network/URL/size. See the
  Resolution Update at the top. Fixed in `b90471d`.
- ✅ **DONE — True streaming video download** (`bodyAsChannel().copyTo(OutputStream)` on Android),
  eliminating the full-ByteArray memory spike. `9285cd4`.
- ✅ **DONE — Partial success.** `DownloadStoryAssetsUseCase` returns `StoryAssetsResult`; the ViewModel
  resolves locals on partial success and shows "Video unavailable offline — saved N/M other assets"
  instead of a flat failure. `9285cd4`.
- ✅ **DONE — Loud failures.** `expectSuccess=true` (4xx/5xx now throw + log) on both platforms; the
  ViewModel download `catch` blocks log instead of swallowing. `9285cd4`.
- Surface download errors more visibly / persistently in the UI (currently flashes in the button area).
- iOS: stream the video to disk too (still buffers a ByteArray — fine for ~30 MB, but match Android);
  add equivalent "pause during download" logic if AVPlayer keeps resources.
- Make `DownloadVideo(lang)` actually respect the lang parameter (currently use case always prefers Fa).
- Test edge cases: very large video, no cover, story with only one lang narration, clear while playing, rapid toggle + cache clicks, reinstall (migration state).

### Phase 2 / Bigger Offline
- Full "bundle" experience (one action or background job that downloads a manifest + everything needed for a story so the whole reader works offline without per-asset thinking).
- Selective download (video only, audio only, specific languages, "lite" vs full).
- Background / resumable downloads, progress in library list, storage management UI ("X MB cached").
- Integration with future user accounts (per-child caches?).

### Broader / Later Phases (from ROADMAP)
- Lullabies loop mode.
- Progress sync (requires auth work from Phase 3 plan).
- Child profiles / PIN (big Phase 2 item).
- Games, streaks, etc.
- Real CI for both platforms on every push.
- Auth-capable Supabase client (already partially planned in PHASE-3-PLAN).
- Icons/launch polish (mentioned as in-progress in the plan).
- Full a11y/RTL/performance pass.

### Process / Tech Debt
- Add unit tests for the use case / cache logic (currently very manual on-device).
- Consider making `DownloadStoryAssetsUseCase` return richer result (success with which assets, failure reason) instead of just `String?`.
- iOS asset cache could benefit from better error surfacing (currently silent).
- Monitor the `cached_*` tables growth (no eviction policy yet).
- Keep following "no bare TODO" rule (use tickets).

## How to Build, Verify, and Continue Work

**Android (always with JBR from Android Studio):**
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :PardisAndroidApp:assembleDebug
# or for forced recompile
./gradlew :PardisAndroidApp:compileDebugKotlin --rerun-tasks
adb install -r app/build/outputs/apk/debug/PardisAndroidApp-debug.apk
```

**iOS / KMP shared:**
```bash
./gradlew :shared:compileKotlinIosSimulatorArm64 --rerun-tasks
./gradlew :shared:compileKotlinIosArm64 --rerun-tasks
# Then open iosApp/iosApp.xcodeproj and build/run in Xcode (or xcodebuild)
```

**After any change:**
1. Run the above narrow compiles.
2. `git status` must be clean or intentionally dirty only for the PR.
3. Commit on the feature branch.
4. Push.
5. Update `docs/ROADMAP.md` + this handoff doc + PHASE-3-PLAN if relevant.
6. Test the flow on device: library → story → video mode → cache button (with airplane mode after) → clear.

**Debugging the cache button:**
- Filter logcat for `AndroidOfflineAssetCache`.
- Check `adb shell ls /data/data/app.pardis.reader/cache/pardis/assets/<slug>/` (or equivalent on emulator).
- Use the Clear button + re-trigger.
- Look at `cached_story` and `cached_pages` rows via DB browser if needed.

## Key Files to Know

**Core contracts:**
- `core/domain/.../OfflineAssetCache.kt`, `DownloadStoryAssetsUseCase.kt`
- `core/data/.../AndroidOfflineAssetCache.kt` (and Ios...), `DownloadStoryAssetsUseCaseImpl.kt`
- `core/data/.../StoryRepositoryImpl.kt` (the rich cache + fallback paths)

**State & logic:**
- `shared/.../reader/ReaderUiState.kt`, `ReaderAction.kt`, `ReaderViewModel.kt` (the `downloadVideoForCurrent` and `updateLocals...` methods are central)
- `shared/.../library/LibraryUiState.kt`, `LibraryViewModel.kt`

**Shells:**
- `app/src/main/java/app/pardis/android/ui/PardisApp.kt` (ReaderScreen, the player remember + LaunchedEffects, transport rows, asset resolution in Coil + narration MediaPlayer)
- `iosApp/iosApp/ContentView.swift` + `ReaderSharedViewModel.swift` + `VideoPlayerView.swift`

**DB:**
- `core/database/src/commonMain/sqldelight/.../Pardis.sq` + `migrations/1.sqm`
- Drivers in `core/database/src/{android,ios}Main`

**Docs:**
- `docs/ROADMAP.md`
- `docs/PHASE-3-PLAN.md` (broader context)
- `docs/kmpSkill.md` (the architecture bible – do not deviate)
- This handoff doc

## Risks & Gotchas (from experience on this branch)

- **Koin module order = last-wins.** `SharedInit` must load `platformModules` LAST so real platform
  impls (e.g. `AndroidOfflineAssetCache`) override the core no-op defaults. Loading platform first
  silently hands resolution to the no-op — the asset cache does nothing and "Download failed" with no
  logs and no files. This was THE root cause of the long-standing download failure.
- **Don't `get()` an Android `Context` in a platform module** unless a bare `single<Context>` is
  registered — only a qualified `single<Any>(named(platformContextQualifier))` exists. Use
  `applicationContext` directly in the module lambda (as `provideSqlDriver` does).
- A no-op fallback binding can mask a broken real binding: the real `AndroidOfflineAssetCache(get())`
  would have crashed on `Context`, but the no-op (no deps) won and hid it. Prefer failing loud.
- Stale `cached_story` JSON from the library list path was the #1 source of "video toggle missing" and "download failed" (fixed by expanding the select + relying on the detailed `getStory` path).
- `errorMessage` in reader state is **fatal** (replaces the whole content area and disposes the player) – never use it for download problems.
- `remember(videoUrl)` on the player = repeated release cycles = scary log spam. We moved to stable instance.
- Full ByteArray for video is fragile for memory + large files.
- iOS posix cache had to be written carefully for K/N interop.
- Always verify both platforms after shared changes.
- Main is protected – everything goes through feature branches + (eventually) PRs.

## Recommended Next Actions (for the person taking this over)

1. Pull the branch, build both platforms, install on a real Android device.
2. Pick a videoReady story, click "Cache video + assets", go offline, verify full experience (video + captions images + Play Audio with rate/lang).
3. Reproduce any "still download failed", grab the `AndroidOfflineAssetCache` logs, and fix the root cause (most likely streaming + URL/header handling or timeout tuning).
4. Once stable, consider the streaming download improvement + better partial-success UX.
5. Update this document + ROADMAP with the resolution.
6. When ready for the "full bundle" experience, start the Phase 2 items (manifest, UI for "Download whole story for offline", etc.).

This work took the offline story from "basic metadata cache" to "real video + page assets + nice UX + reliable button" while staying inside the project contract.

If you have questions, the commit messages on the branch + the detailed comments in `ReaderViewModel`, the cache impls, and the use case are the best primary sources.

Good luck – the foundation is solid now. The scary logs + "download failed" on the cache button were the last major user-facing blocker for this phase.

---

*End of hands-off report. Everything above is the current truth as of the last commits on the branch.*