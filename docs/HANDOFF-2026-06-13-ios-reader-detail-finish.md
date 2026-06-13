# Handoff — iOS Reader v2 + Detail + Finish, font root cause closed

_Date: 2026-06-13_

## Current State

- Branch: `main`, all work below merged via PR #8 (+ a one-line follow-up for the Detail hero corners).
- iOS now has **full screen parity** with Android: Onboarding, the 5 tabs, Lullaby, Character,
  and (new this session) **Detail → Reader v2 → Finish** — the last items from the 2026-06-09 handoff.

## "Fonts missing on iOS" — root cause, closed

Measured, not guessed:

- **Loading was already fixed** (PR #6): CI's bundling gate + the UI-test probe prove all four
  families resolve — `display=Bricolage Grotesque | body=Plus Jakarta Sans | persian=Vazirmatn |
  mono=JetBrains Mono`, zero MISSING (assertion re-ran green on PR #8).
- **The real remaining cause**: 10 call sites that never referenced brand fonts at all, hardcoding
  `.font(.system(..., design: .rounded))` — 6 in the old `ContentView` reader (the most text-heavy
  screen, so fonts looked "missing" exactly where users read) and the rest in
  `PardisVocabChipView`/`PardisVocabSheetContent`. Matches the audit finding in
  `docs/PROJECT_REVIEW_2026-06-10.md`.
- **Fix**: all 10 sites removed (vocab primitives remapped to Android's M3 roles; the reader rewrite
  routes everything through `PardisFonts`). Regression gate:
  `grep -rn "design: .rounded" iosApp/iosApp --include="*.swift"` must return nothing.

## Shipped this session (PR #8)

1. **shared**: `PardisViewModelProvider` exposes `storyDetailViewModel()` / `storyFinishViewModel()`
   (VMs/Koin modules already existed — closes the audit's iOS-parity backlog item).
2. **DetailScreen** (`DetailView.swift` + `DetailSharedViewModel`) ← `PardisDetailScreen`: hero with
   scrim + title overlay, meta chips, synopsis, narrator note, vocab preview, sticky
   Start/Continue CTA (`canResume`). New `PardisFlowLayout` = Compose `FlowRow` equivalent.
3. **Reader v2** (`ReaderView.swift`, extracted from `ContentView`) ← `PardisReaderScreen`:
   top bar (PAGE X OF N + bookmark), page dots, 290pt media box (AVPlayer / illustration + badge),
   EN/Both/فا prose with **tappable Farsi glossary words** (AttributedString links + OpenURLAction
   mirroring Android's LinkAnnotation), vocab help row, video/offline pills, dock (progress,
   language segmented control, prev/play/next→finish-check), v2 word-card sheet (Hear it /
   Add to garden). Adapter gained `storyTitle` + `isNarrating`; narration honors offline cache +
   playbackRate with auto-advance.
4. **FinishScreen** (`FinishView.swift` + `FinishSharedViewModel`) ← `PardisFinishScreen`: night
   gradient + radial glow, one-shot confetti (TimelineView port), rotating dashed-ring scene-art
   medallion with gold check, star row, stat tiles, garden chips, Next story / Done CTA.
5. **Navigation** ← `PardisApp.kt` semantics: Today **and** Library push Detail → Reader → Finish
   (single `onOpenStory` → detail, like Android); finish **replaces** the reader in the
   NavigationPath (back never re-enters the last page); Next story replaces finish with the next
   detail; Done pops to tab root. See `storyFlowDestinations` in `ContentView.swift`.
6. New icons: `bookmark`, `sparkle`; `PardisAsyncImageFrame` gained a `cornerRadius` override.

## Verification (per "build green is enough" — no screenshot eyeballing)

- `kmp-build.yml` (android / ios-framework / ios-app) green on the branch (dispatch run
  27452628176) and again on PR #8; screenshot tour green incl. launch + font assertion.
- `:shared:testAndroidHostTest` (17 tests) + `:PardisAndroidApp:compileDebugKotlin` green locally.
- Branch iteration ritual: push → `gh workflow run kmp-build.yml --ref <branch>` (fast ~3-min gate);
  the slow tour only auto-runs on PR-to-main. See the `verify-ios-without-xcode` skill.

## What's left (carried forward)

- **Pattern-motif overlays on iOS** (paisley/vine/rosette/nightsky at low alpha) — omitted on
  SceneArt, Onboarding, Detail hero, Reader media box, word card, Finish background. Needs
  asset-catalog images.
- **Evaluator #4**: `IosOfflineAssetCache` still buffers whole files in memory (stream like Android).
- **Evaluator #5**: cache eviction/quota — needs a product decision (quota size, LRU vs manual).
- **Feature backlog**: Parents' corner, dark-mode wiring (You toggle is decorative), "Add child"
  flow, reader bookmark button (decorative on both platforms).
- Audit fix-day checklist in `docs/PROJECT_REVIEW_2026-06-10.md` (CI doesn't run unit tests,
  AVPlayer retain cycle in `VideoPlayerView`, token-generator gaps, etc.).

## Rules to keep

Unchanged from `HANDOFF-2026-06-09-onboarding-native-nav-qa.md` (tokens only, Route→Screen→Component,
callback navigation, commonTest via `:shared:testAndroidHostTest`, branch + verify before merge).
