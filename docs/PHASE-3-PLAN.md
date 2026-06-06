# Phase 3 Plan: Polish, Auth, Polish

**Progress note (as of latest continuation)**: Phase 1 video+audio+progress+offline-assets on feature/phase1-offline-page-assets (generalized OfflineAssetCache for video/illustration/narration, DownloadStoryAssetsUseCase for full page assets when caching video, local path resolution in VM/UiState for images + audio, library cached badges, polished "Cache video + assets" UX + indicators in shells). Builds on prior video UX + pages cache. ROADMAP updated. Fix for "Cache video + assets" triggering download fail + MediaCodec detach logs: rich cache select in getStories, try/catch+nonfatal feedback in VM, stable ExoPlayer+LaunchedEffect source switch (no recreate on local). Recent hardening: explicit OkHttp + long timeouts + detailed error logging in Android cache.  
**Comprehensive hands-off report** (what's done, what's left, current goal, how to verify/continue, risks, key files): `docs/PHASE1-OFFLINE-ASSETS-HANDOFF.md`. Next: diagnose remaining download failures via the new logs, streaming video download, full bundle (Phase 2).

**Goal**: Take the current working KMP reader (library list + basic pager with real Supabase data) and deliver production-ready polish, auth foundations, and supporting infrastructure while strictly following `docs/kmpSkill.md`, the Pardis KMP delivery skill, and using Beforely **only** as an internal reference for clean architecture (never copy code/names).

**Status at start of Phase 3**: Phase 0 foundations + basic Phase 1 reader experience complete (per updated ROADMAP). All changes committed and pushed.

**Non-negotiables** (from kmpSkill + delivery skill):
- Shared logic ONLY in `shared/` + `core/*`.
- Native UI ONLY in `app/` (Compose) and `iosApp/` (SwiftUI). No shared UI.
- Exactly one `uiState: StateFlow` per VM.
- `onAction` for intent; navigation 100% platform-owned (callbacks or typed NavKeys).
- Design tokens from `design-system/` (Pardis palette only: saffron, indigo, mint, lilac, #FAF6EE cream, etc.). Add token first for any new visual.
- Secrets/config ONLY in platform bootstrap (never commonMain).
- iOS 17+: `@Observable` + `@State` + `.task` + `for await` (already aligned).
- Android: `koinViewModel()` only in Route; Screen receives state + onAction + callbacks. Prefer typed NavKeys + Navigation 3 where appropriate.
- Error handling: never swallow in data/network; VM owns mapping to UiState.
- Structure must stay aligned with kmpSkill at all times.
- Update ROADMAP, add to design-system if new visuals, run narrow verification builds.

**High-level approach**:
- Work in small, reviewable increments (one item or sub-item per PR).
- Always run `./gradlew ... assembleDebug` + iOS build verification after changes.
- For any new visual: edit `design-system/tokens.json` + regenerate + use tokens (no raw values).
- Keep Beforely patterns only where they match kmpSkill (e.g. clean thin adapters, feature packages in shared).
- Track progress in this plan + ROADMAP.md.

## 1. Proper Supabase client / enhanced Ktor for auth (user features)
Current: Raw Ktor object in common for public anon reads only (good for stories/pages).

**Steps**:
1.1. Evaluate options (respecting KMP + skill):
   - Option A (preferred for minimal change): Enhance existing Ktor client in `core/network`.
     - Create `SupabaseClient` class (injected) with config from platform.
     - Add support for user auth (e.g. via Supabase GoTrue / JWT in headers for protected tables like child progress).
     - Use platform-provided tokens (e.g. from Clerk or future Supabase Auth in shells).
   - Option B: Introduce official `io.supabase:supabase-kt` (or community KMP port) if stable and matches versions. Add as dependency only if it doesn't force shared UI or leak platform types.
   - Decision: Start with Option A (enhanced Ktor) unless research shows supabase-kt is clearly better and fits. Document choice in commit.

1.2. Refactor network:
   - Make `Supabase` injectable (class + interface or config-driven).
   - Add authenticated client path (e.g. `getAuthenticatedClient(token: String)` or header provider).
   - Keep public anon path for content.
   - Add methods for future user tables (e.g. `getChildProgress`, `upsertProgress`).
   - Mappers stay in `core/data`.

1.3. DI updates:
   - Provide client/config in platform modules (Android Application, iOS bootstrap).
   - Update `core/di` and shared modules.
   - Qualify if needed (public vs auth).

1.4. Shell integration (later for child profiles):
   - Provide auth token from platform (e.g. after login flow — stub for now).
   - Update SharedInit calls.

1.5. Verification:
   - Add simple test that public reads still work.
   - Ensure no secrets in commonMain (already using expect/actual for anon).

**Deliverable**: Auth-capable client ready for Phase 2 child data without breaking current public content flow.

## 2. Design system full (more tokens, components, motion, lints)
Current: Basic `PardisColors`, `PardisSpacing`, `PardisRadius`, `PardisMotion` (generated from tokens.json).

**Steps**:
2.1. Expand tokens (edit `design-system/tokens.json` first):
   - Add typography (font sizes, weights, line heights for FA/EN bilingual).
   - Add more colors if needed (e.g. error, success, overlays).
   - Add elevation/shadow tokens.
   - Add icon sizes, durations for media controls.
   - Add semantic component tokens (e.g. card padding, reader overlay alpha).

2.2. Regenerate platform files:
   - `PardisTokens.kt` (Android)
   - `PardisTokens.swift` (iOS)
   - Update any manual copies (e.g. the one in app/ for now).

2.3. Build reusable components in shells (no shared UI):
   - Android: `PardisCard`, `BilingualText`, `ReaderTransportBar`, `VocabChip` etc. in `app/src/main/java/.../ui/components/`.
   - iOS: SwiftUI equivalents in `iosApp/iosApp/Components/` or views.
   - Use only Pardis* tokens. Support RTL.

2.4. Motion:
   - Define spring/tween tokens in design-system.
   - Use in transitions (e.g. page swipe, vocab sheet).

2.5. Lints / generated:
   - Consider detekt or compose-lint rules for token enforcement (future).
   - Document in `design-system/MDS.md`.

2.6. Apply everywhere:
   - Update existing library cards, reader pager, etc. to use new components/tokens.
   - No inline hex/dp/strings.

**Deliverable**: Full token set + at least 3-4 reusable components used in current screens. RTL stubs in place.

## 3. Accessibility, RTL for FA, performance
**Steps**:
3.1. RTL (Farsi primary):
   - Android: Use `LocalLayoutDirection` in composables; test with `LayoutDirection.Rtl`.
   - iOS: `.environment(\.layoutDirection, .rightToLeft)` on views or root.
   - Ensure text alignment, icons, paging direction respect RTL (FA on top? per web reader).
   - Add to both library and reader.

3.2. Accessibility:
   - Content descriptions / labels using tokens or platform strings (no hardcoded in shared).
   - Semantic headings, buttons.
   - High contrast / large text support via system.
   - VoiceOver / TalkBack friendly (e.g. page announcements).

3.3. Performance:
   - Image loading: Add Coil (Android) + native (iOS SDWebImage or async image) with prefetch for next pages.
   - Media: Proper player lifecycle, release resources.
   - List: Use LazyColumn/LazyVStack with keys; pagination if needed later.
   - Prefetch illustrations/narration metadata.
   - Measure with Android Profiler / Instruments.

3.4. Testing: Manual on device + basic UI tests if time.

**Deliverable**: RTL working for Farsi text in library + reader. Basic a11y + image prefetch in reader.

## 4. Analytics / telemetry stubs
**Steps**:
4.1. Define simple interface in `core/domain` or `shared` (e.g. `Analytics` with `track(event: String, properties: Map)`).
4.2. No-op impl in common.
5. Platform impls:
   - Android: Firebase or simple Log (stub).
   - iOS: similar.
6. Wire via DI (platform modules).
7. Call from VMs or shells on key actions (library load, page turn, vocab tap, video toggle). Keep events semantic.
8. Respect privacy (no PII in stubs).

**Deliverable**: Injectable Analytics stub called from at least library + reader flows. Ready for real backend later.

## 5. CI for KMP builds
**Steps**:
5.1. Create `.github/workflows/` (if not present).
5.2. Job for Android: `./gradlew :PardisAndroidApp:assembleDebug` + test.
5.3. Job for iOS: build shared framework + `xcodebuild` for simulator.
5.4. Matrix for debug/release if needed.
5.5. Cache Gradle, Xcode derived data.
5.6. Fail on warnings if possible; upload artifacts.
5.7. Add to README verification section.

**Deliverable**: CI that passes on push/PR for both platforms (at least build).

## 6. App icons, launch, store metadata
**Steps**:
6.1. Design / generate proper Pardis-branded icons (use tokens colors).
6.2. Update all mipmap / appiconset (the res/ we have are placeholders).
6.3. Android:
   - `ic_launcher` + round + foreground.
   - Update `AndroidManifest.xml` (already partially done).
6.4. iOS:
   - Update Assets.xcassets/AppIcon.
6.5. Launch screens: simple branded (use design tokens).
6.6. Store metadata stubs: `fastlane` or manual `metadata/` dirs? (title "Pardis Reader", description from web, screenshots plan).
6.7. Versioning consistent with toml.

**Deliverable**: Branded icons + launch that match Pardis palette on both platforms. Basic store metadata files.

## Sequencing & Milestones
- Milestone 3.1: Auth-capable client + design tokens expansion (2-3 PRs).
- Milestone 3.2: Components + a11y/RTL + performance basics.
- Milestone 3.3: Analytics + CI. (done, with iOS placeholder)
- Milestone 3.4: Icons + launch + metadata. (in progress on branch; icons polished, metadata stubs, launch theme)
- After each: update ROADMAP checkboxes, run full verification builds, test on device/sim (RTL + FA text critical).
- Cross-check with parent Pardis AGENTS.md for any web alignment (e.g. event names).

## Risks & Gotchas
- Auth: Don't leak tokens into common; use qualified DI.
- RTL: Test bidirectional text (mixed FA/EN) carefully.
- Design tokens: Never bypass — add to tokens.json first.
- CI: macOS runners for iOS; use setup-java + gradle caching.
- Keep all new logic in shared/core; shells stay thin.
- If adding new deps (e.g. for images), add to `gradle/libs.versions.toml` first.

## Verification Commands (run narrow then broad)
```bash
./gradlew :PardisAndroidApp:assembleDebug
./gradlew :shared:iosSimulatorArm64Test :core:data:iosSimulatorArm64Test ... (add as we grow)
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'generic/platform=iOS Simulator' build
```

Update this plan + ROADMAP.md as items complete. Reference kmpSkill checklist in every PR description.

This plan ensures we deliver Phase 3 without deviating from the architecture contract. 

Next action after review/approval: pick first sub-task (recommend starting with design tokens + Supabase client refactor, as they unblock later items).
