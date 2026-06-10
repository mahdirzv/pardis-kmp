# Pardis KMP — Project Review Audit (2026-06-10)

End-to-end review of the KMP app (shared logic + native Compose UI + native SwiftUI),
run with the same rigor as the pardis web review (which scored 7.5–8/10) so the two
reports are comparable. Review only — no code was changed.

**Overall: 6.5 / 10** (web sibling: 7.5–8). The shared-core architecture and the
Swift interop stack are the strongest parts; testing allocation, design-token
enforcement, and CI gating are what hold the score down. Most top fixes are S-effort.

| # | Axis | Score |
|---|------|-------|
| 1 | KMP architecture | **7.5/10** |
| 2 | Concurrency & data flow | **7.5/10** |
| 3 | iOS interop quality | **7/10** |
| 4 | Design system | **6/10** |
| 5 | Networking & persistence | **6/10** |
| 6 | Testing | **4/10** |
| 7 | CI/CD & build health | **6/10** |
| 8 | Docs & agent-navigability | **7/10** |

---

## Axis summaries

### 1. KMP architecture — 7.5/10

Textbook layering for an app this size: `core/model` is pure (serialization only),
`core/domain` holds interfaces and depends only on model, `core/data` implements
domain and maps DTO→domain so network rows never leak upward, `core/di` is a real
composition root. Only **two** expect/actual pairs exist — `provideSqlDriver`
(`core/database/.../Driver.kt:9`, right seam but weakly typed `context: Any?` with a
runtime cast in `AndroidDriver.kt:8`) and `SupabaseSecrets`
(`core/network/.../SupabaseClient.kt:16`, **wrong seam**: both actuals are
byte-identical literals — config masquerading as a platform abstraction). The real
platform seam (`OfflineAssetCache`) is correctly a domain interface with platform
impls wired via DI, which is better judgment than expect/actual there. ViewModels are
androidx-lifecycle KMP with a single immutable `UiState` StateFlow + sealed `onAction`
(MVI-lite), consumed via `koinViewModel()` + `collectAsStateWithLifecycle` on Android
and SKIE `for await` in `@Observable` adapters on iOS. DI is Koin with documented
override ordering and a **tested fail-fast guard** (`SharedInit.kt:56-64`) against the
NoOp-cache misconfiguration. Navigation is fully native per platform; shared VMs stay
navigation-agnostic. Deductions: iOS never clears factory-scoped Kotlin VMs, `app/`
depends on every core impl module (`app/build.gradle.kts:37-42`), dead service-locator
code (`object Supabase`, unused `platformContextQualifier`), and iOS is missing the
Detail/Finish VMs Android uses.

### 2. Concurrency & data flow — 7.5/10

No `GlobalScope`, no `runBlocking`, no background StateFlow writes anywhere in
production code. `OfflineDownloadManager` is the best class in the repo: injectable
supervised Main-scoped singleton with a documented single-threaded invariant,
job-identity guards (`if (jobs[slug] === job)`), `CancellationException` rethrown, and
real tests using `StandardTestDispatcher`. Flows reach Swift via **SKIE 0.10.12**
(`shared/build.gradle.kts`), collected with `for await … in .task {}` — the canonical
pattern, with correct cancellation of collection on disappear. Android collection is
lifecycle-correct in all screens. Deductions: blocking SQLDelight queries run on
`Dispatchers.Main` in `StoryRepositoryImpl` (incl. `saveProgress` on every page turn)
while the asset caches correctly hop to IO; side effects (`analytics.track`,
`launch { saveProgress }`) sit inside a `StateFlow.update {}` CAS lambda
(`ReaderViewModel.kt:44-57`) and would duplicate on contention; a `var done = 0`
counter is mutated across concurrent `async`s (`DownloadStoryAssetsUseCaseImpl.kt:48`),
safe only because callers happen to be Main-dispatched; and the iOS VM lifecycle gap
below.

### 3. iOS interop quality — 7/10

This is *not* Kotlin through a keyhole: iOS-17 `@Observable` + `@State` adapters,
`NavigationStack`/`navigationDestination(item:)`, native `TabView`, sheets with
detents, AVPlayer via `UIViewRepresentable`. Kotlin bridging noise is confined to the
adapter layer with explanatory comments (`Int(truncating:)` for `[KotlinInt: String]`
in `ReaderSharedViewModel.swift:148-151`). Packaging is clean and reproducible:
XcodeGen (`project.yml`) + `embedAndSignAppleFrameworkForXcode`, gitignored
`.xcodeproj`, dynamic-debug/static-release framework split with commented rationale.
Deductions: a textbook **AVPlayer retain cycle** (`ContentView.swift:447-456`: the time
observer closure captures `context.coordinator`, the coordinator retains the player,
the player retains the observer — so the `deinit` that removes the observer and pauses
playback can never run); side-effectful `@State` default values
(`ContentView.swift:107, 196`) construct throwaway Kotlin VMs whose `init { refresh() }`
fires orphan network calls; `ViewModel.clear()` is never called from Swift (zero hits
repo-wide); `export(project(":core:data"))` inflates the ObjC header beyond what Swift
uses; `URL(string:)!` force unwrap at `ContentView.swift:430`; dead `FlowCollector.swift`.

### 4. Design system — 6/10 · claim verdict: **mostly false as stated**

The claim was "fully tokenised design using the Pardis palette only." What's true:
typography is genuinely fully tokenised on both platforms (zero literal `.sp` on
Android; 135/135 iOS font calls go through `PardisFonts`/`PardisTypography` with
registry-resolved families); the M3 theme is built 100% from `PardisColors`; the eight
main Android screens are hex-free; the 57 base color/spacing/radius/type values match
exactly everywhere they appear. What's false: **the "generated" pipeline has no
generator** (no script anywhere references `tokens.json`; `design-system/README.md:11`
admits "or manually sync"), and **Android doesn't consume `generated/` at all** — the
live token layer is a hand-maintained fork at `app/src/main/java/app/pardis/design/PardisTokens.kt`
that has drifted ahead of tokens.json (sunPale, violet, overlays, scrims, gradients).
**11 unique off-palette hexes** are used on both platforms (night blues `#0F1330`/
`#0A0E22`/`#171B3A`, `#5B47FF`, `#BCC8E8`, `#6AD0AB`, `#FFD9A8`…), ~94 hex-literal
occurrences total (most in scene-art, but plain UI too: `PardisLullabyScreen.kt:273`
sheet background, `PardisPrimitives.kt:195,209` progress track,
`LullabyView.swift:300`). Spacing is half-tokenised (55 Android + 32 iOS literal
paddings, off-scale values like 13/7/18/22/46dp). `PardisMotion` has **zero consumers**
on either platform. One real cross-platform value divergence: scrim 60%/40% on Android
(`PardisTokens.kt:82-83`) vs 55%/45% on iOS (`PardisTheme.swift:55-56`) under the same
token names.

### 5. Networking & persistence — 6/10

Hand-rolled Ktor client over Supabase PostgREST (OkHttp/Darwin engines), sensible
`Json { ignoreUnknownKeys; isLenient }`, DTOs confined to `core/network` with mapping
in `core/data`. SQLDelight cache (network-first, cache-fallback on every read), plus a
separate file asset cache where the Android impl is exemplary (streaming, partial-file
cleanup, `expectSuccess`) and the iOS impl buffers **entire videos into a ByteArray**
(`IosOfflineAssetCache.kt` — jetsam risk, admitted in its own comment). Secrets posture
is **correct and verified**: the embedded JWT decodes to `role:"anon"` (the same key
the web app ships), no service key anywhere, no tracked `.env`/keystores/`local.properties`;
but the anon key + URL are hardcoded in two byte-identical files named
`SupabaseSecrets.kt`, so rotation means a two-platform code change. Deductions: **no
typed error model anywhere** — repositories swallow `Throwable` and ViewModels render
raw `t.message` to users; `getStoryPages` failure collapses to `emptyList()`
(indistinguishable from an empty story); schema errors detected by
`msg.contains("column")` string-sniffing (`StoryRepositoryImpl.kt:160-164`);
`SupabaseClient` is a concrete class with no interface (blocks testing and auth
evolution); the `StoryRow→Story` mapper and select-column list are each duplicated;
cache validity is "exists and >1KB"; downloads live in OS-purgeable cache dirs; domain
models are `@Serializable` and persisted as JSON blobs, so renames silently invalidate
cache.

### 6. Testing — 4/10

| Source set | Files | Test cases |
|---|---|---|
| shared `commonTest` (runs as JVM host test) | 5 | 17 |
| androidUnitTest (app + all core modules) | 0 | 0 |
| androidInstrumentedTest | 0 | 0 |
| iOS unit tests (XCTest) | 0 | 0 |
| iosAppUITests (screenshot tour) | 1 | 1 |
| **Total** | **6** | **18** |

What exists is genuinely good — hand-rolled fakes with `awaitCancellation()` to
simulate hung downloads, `StandardTestDispatcher`, in-flight progress and cancellation
assertions, and a regression test pinning a previously-shipped cue-math bug
(`BuildVideoCueTimelineTest`). But the 17 tests cover the four easiest islands while
the riskiest code has zero coverage: **`StoryRepositoryImpl`** (the most logic-dense
class — and untestable as written because `SupabaseClient` has no interface),
**`ReaderViewModel`** (largest VM; only its extracted pure function is tested),
**`LibraryViewModel`** (9-flow `combine` with positional `args[n] as` casts — a
reorder compiles and crashes at runtime), both platform asset caches, and all Swift
interop. No DTO golden-JSON tests. And **CI never runs the suite** (see Axis 7), so
today the tests catch nothing automatically.

### 7. CI/CD & build health — 6/10

Strong skeleton: PR-gated three-job CI (Android assemble; K/N simulator framework
link; full no-signing `xcodebuild` simulator build **with a font-bundling assertion**,
`kmp-build.yml:76-84`) plus an on-demand screenshot tour with `~/.konan` caching.
Gradle 9.3.1, configuration cache + build cache on, repo content filtering, typesafe
accessors, foojay resolver. Deductions: **CI is compile-only** — the comment at
`kmp-build.yml:28` still says "Add tests here once a test suite exists" while 17 tests
exist; **zero lint** (no detekt/ktlint/spotless/SwiftLint) even though
`docs/code-rules.md:63` claims "Lint or review will catch"; the version set is
incoherent — bleeding-edge tooling (AGP 9.1.0, Gradle 9.3.1, compileSdk 37) over a
2024-vintage library set (Kotlin 2.1.20, Ktor 3.0.1, coroutines 1.9.0, media3 1.4.1,
SQLDelight 2.0.2), and the app hardcodes Compose UI **1.7.6**/M3 1.3.1
(`app/build.gradle.kts:59-62`) while the catalog declares an **unused**
`composeBom = 2025.10.00`; module config is copy-pasted ×7 with no convention plugin;
the fast macOS jobs lack konan caching, `concurrency`, and `timeout-minutes`;
`gradle.properties` carries a dead Compose-Multiplatform UIKit flag and
`kotlin.native.cacheKind=none`. (Dependency-currency judgments are as of the reviewer's
Jan-2026 knowledge cutoff.)

### 8. Docs & agent-navigability — 7/10

Unusually rich and mostly *true*: 6 of 8 spot-checked claims matched the code exactly
(module map, VM pattern, design-system wiring, handoff file lists, RivanaContent
hoisting). The handoff docs are best-in-class agent onboarding — exact commands, APK
paths, even an honest retraction of earlier false claims in the Phase-1 post-mortem —
and `tasks/lessons.md` captures real KMP gotchas. Drift lives at the **entry points**:
`README.md:64-66` omits the canonical `:shared:testAndroidHostTest` command and the
`xcodegen generate` step; `docs/skills/pardis-kmp-delivery/SKILL.md:57` invokes
`iosSimulatorArm64Test` on three modules that have no tests; `docs/ROADMAP.md:47`
claims CI runs tests (it doesn't) and calls the iOS job a "skeleton" (it isn't);
`AGENTS.md:6` and `docs/kmpSkill.md:5` point first at out-of-repo paths
(`../pardis/AGENTS.md`, `~/.codex/skills/...`) that are dead ends for anyone else;
`docs/code-rules.md:58` says mint `#34B57F` vs tokens.json `#2FA876`. A fresh agent
succeeds here, but only after learning the handoffs outrank the README.

---

## Fix list (ordered for a fix-day)

Severity: 🔴 high · 🟡 medium · ⚪ low. Effort: S/M/L.

### Morning — small fixes, big payoff (all S)

- [ ] 🔴 **S** — Run the unit tests in CI. `.github/workflows/kmp-build.yml:28` still says
  "Add tests here once a test suite exists"; add `./gradlew :shared:testAndroidHostTest`
  to the `android` job. Until then a PR can break `OfflineDownloadManager` and merge green.
- [ ] 🔴 **S** — Break the AVPlayer retain cycle in `VideoPlayerView`
  (`iosApp/iosApp/ContentView.swift:447-456`): the periodic-time-observer closure
  captures `context.coordinator`; coordinator retains player; player retains the block —
  `Coordinator.deinit` (`:485-499`), the only place the observer is removed and playback
  paused, can never run. Capture the coordinator weakly.
- [ ] 🔴 **S** — Resolve the Compose version contradiction: `app/build.gradle.kts:59-62`
  hardcodes Compose UI 1.7.6 / M3 1.3.1 while `gradle/libs.versions.toml:14,52` declares
  an unused `composeBom = 2025.10.00`. Adopt the BOM (or delete it); also remove the dead
  `compose = "1.8.1"` entry.
- [ ] 🟡 **S** — Move SQLDelight calls off `Dispatchers.Main`: `getFromCache`,
  `saveProgress` (fires on every page turn), `getProgress`, `upsertToCache` in
  `core/data/.../StoryRepositoryImpl.kt` are blocking calls in suspend funs with no
  `withContext(Dispatchers.IO)` — the asset caches already do this correctly.
- [ ] 🟡 **S** — Pull side effects out of `_uiState.update {}` in
  `shared/.../reader/ReaderViewModel.kt:44-57`: `analytics.track` and
  `viewModelScope.launch { saveProgress }` inside the CAS lambda duplicate on retry.
- [ ] 🟡 **S** — Replace schema-error string sniffing (`StoryRepositoryImpl.kt:160-164`,
  `msg.contains("column")`) with Ktor status-code handling; set `expectSuccess = true`
  on the REST client so 4xx doesn't surface as a deserialization error.
- [ ] 🟡 **S** — Add a fail-fast guard for a missing SQL driver: `core/di/CoreModules.kt:57`
  (`PardisDatabaseHolder(getOrNull<SqlDriver>()…)`) silently runs with zero persistence,
  unlike the guarded asset cache (`SharedInit.kt:56-64`).
- [ ] 🟡 **S** — Replace `URL(string: videoUrl)!` (`ContentView.swift:430`) with a guard.
- [ ] 🟡 **S** — Fix the `done++` shared counter in
  `core/data/.../DownloadStoryAssetsUseCaseImpl.kt:48` (mutated from concurrent `async`s;
  safe only because callers are Main-dispatched today) and consider bounding download
  parallelism.
- [ ] 🟡 **S** — Reconcile the scrim divergence: Android 60%/40%
  (`app/.../design/PardisTokens.kt:82-83`) vs iOS 55%/45%
  (`iosApp/iosApp/PardisTheme.swift:55-56`) under identical token names. Also tokenise
  the night colors on Android (`PardisBedtimeScreen.kt:32`, `PardisLullabyScreen.kt:84`)
  to match iOS's `nightMid`/`nightDeep`.
- [ ] 🟡 **S** — De-duplicate Supabase config: `SupabaseSecrets.kt` is byte-identical in
  androidMain and iosMain (`core/network/src/{android,ios}Main/.../SupabaseSecrets.kt:9-11`);
  there's no platform variance, so move it to commonMain (or BuildKonfig) and rename —
  a file named "Secrets" that's intentionally committed invites future real secrets in.
- [ ] 🟡 **S** — Fix entry-point doc drift: `README.md:64-66` (add
  `:shared:testAndroidHostTest` + `xcodegen generate`), `docs/ROADMAP.md:47` (CI claims),
  `docs/skills/pardis-kmp-delivery/SKILL.md:57` (tests that don't exist),
  `docs/code-rules.md:58` (mint hex), and mark `../pardis/AGENTS.md` /
  `~/.codex/skills/...` references as optional/external.
- [ ] ⚪ **S** — Delete dead code: `object Supabase` (`SupabaseClient.kt:166-180`, second
  HttpClient outside DI, zero usages), `FlowCollector.swift` (never called),
  `platformContextQualifier` registration (`PardisApplication.kt:21`, no consumer),
  duplicate `single { SupabaseClient() }` (`CoreModules.kt:54` + `PardisApplication.kt:29`),
  dead `org.jetbrains.compose.experimental.uikit.enabled` flag (`gradle.properties:12`),
  and the nonexistent `composeApp/...` path in `.gitignore:41` (root `.playwright-mcp/`
  isn't actually covered).
- [ ] ⚪ **S** — CI hygiene: add `~/.konan` caching to `kmp-build.yml`'s macOS jobs (only
  `ios-screenshots.yml:27-32` has it), plus `concurrency:` cancel-in-progress and
  `timeout-minutes`; pin the xcodegen install.
- [ ] ⚪ **S** — De-duplicate the `StoryRow→Story` mapper (`StoryRepositoryImpl.kt:54-76`
  vs `:129-152`) and the select-column string (`SupabaseClient.kt:131` vs
  `StoryRepositoryImpl.kt:124`).

### Afternoon — medium efforts (M)

- [ ] 🔴 **M** — Fix the iOS Kotlin-VM lifecycle: side-effectful `@State` defaults
  (`ContentView.swift:107, 196`) construct throwaway `LibrarySharedViewModel`/
  `ReaderSharedViewModel` instances on every parent re-init, each resolving a fresh
  Kotlin VM whose `init { refresh() }` (`LibraryViewModel.kt:107-109`) fires an orphan
  network fetch; and `ViewModel.clear()` is never called from Swift (zero hits), so
  `viewModelScope` work outlives dismissed screens. Use `@State` + explicit
  init-in-`.task`/`.onAppear` or a holder, and call `clear()` from `deinit`/`onDisappear`.
- [ ] 🔴 **M** — Stream iOS asset downloads to disk: `IosOfflineAssetCache.kt`
  (`downloadAssetIfNeeded` does `http.get(remoteUrl).body()` into a `ByteArray`) buffers
  tens-of-MB videos in RAM — memory-pressure kill risk; mirror the Android streaming impl.
- [ ] 🔴 **M** — Extract an interface from `SupabaseClient` (`SupabaseClient.kt:105`) —
  it's the single concrete obstacle to testing the whole data layer, and the seam any
  future auth strategy needs.
- [ ] 🔴 **M** — Make tokens.json the actual source of truth: write a real generator
  (none exists; `design-system/README.md:11` says "or manually sync"), fold the
  hand-fork's extra tokens (`app/.../design/PardisTokens.kt`: sunPale, violet, overlays,
  scrims, gradients) back into tokens.json, point Android at the generated output
  (currently `design-system/generated/android/PardisTokens.kt` is dead code), and either
  tokenise or explicitly carve out the 11 off-palette hexes (scene-art exemption is a
  defensible policy — but write it down).
- [ ] 🟡 **M** — Introduce a typed error model (sealed `AppError`/`Result` at the
  repository boundary): today repos swallow `Throwable` (`StoryRepositoryImpl.kt:82-85,
  204-207`), `getStoryPages` failure collapses to `emptyList()`, and ViewModels render
  raw `t.message` to users.
- [ ] 🟡 **M** — Add the missing high-risk tests: `StoryRepositoryImpl` (after the
  interface extraction — network-first/cache-fallback, JSON round-trip, schema-error
  branch), `LibraryViewModel`'s 9-flow `combine` with positional `args[n] as` casts
  (replace with nested combines or an intermediate data class while there),
  `ReaderViewModel` orchestration, and DTO golden-JSON decode tests.
- [ ] 🟡 **M** — Add lint: detekt or ktlint for Kotlin, SwiftLint for iOS, wired into CI —
  `docs/code-rules.md:63` already promises "Lint or review will catch" token violations;
  make that true (a custom rule banning `Color(0x`/`Color(hex:` outside the token layer
  pays for itself).
- [ ] 🟡 **M** — Coordinated dependency sweep: Kotlin 2.1.20 / Ktor 3.0.1 /
  coroutines 1.9.0 / serialization 1.7.3 / SQLDelight 2.0.2 / media3 1.4.1 /
  lifecycle 2.8.7 are all ~1+ year stale under bleeding-edge AGP 9.1/Gradle 9.3.1/SDK 37;
  Kotlin↔SKIE↔AGP must move together (per `.github/instructions/kmp.instructions.md`).
- [ ] 🟡 **M** — Harden the offline story cache: validity is "exists and >1024 bytes"
  (`AndroidOfflineAssetCache.kt`) / existence-only (`IosOfflineAssetCache.kt`), and
  assets live in OS-purgeable cache dirs while the UI says "Downloaded" — store expected
  size/hash and/or move to non-purgeable storage with launch-time reconciliation.
- [ ] 🟡 **M** — Slim the `app/` dependency surface (`app/build.gradle.kts:37-42` pulls
  `core:data`/`core:network`/`core:database` with a stale justification comment) by
  moving the Android platform Koin module into `shared`/androidMain or `core:di`.
- [ ] ⚪ **M** — Convention plugin for the ×7 copy-pasted module config (android
  compileSdk/minSdk block, iOS target list, KT-61096 `archives` workaround in every
  `core/*/build.gradle.kts` + `shared/build.gradle.kts:92-94`).

### Backlog (L)

- [ ] ⚪ **L** — iOS feature parity: `StoryDetailViewModel`/`StoryFinishViewModel` ship in
  `shared` but aren't exposed in `PardisViewModelProvider`; iOS routes Library→Reader
  directly (`ContentView.swift:129`) with no detail/finish flow.
- [ ] ⚪ **L** — Full spacing/motion tokenisation sweep: 55 Android + 32 iOS literal
  paddings, 328 raw `.dp` (off-scale values like 13/7/18/22/46), 5+2 literal corner
  radii, `PardisMotion` with zero consumers and easing tokens never ported, 10 iOS
  `.font(.system(design: .rounded))` sites bypassing brand families.
- [ ] ⚪ **L** — Before Phase 3 (server-side progress/profiles): verify RLS policies on
  `stories`/`story_pages`/`couplets`/`vocab_terms` in the web repo, and design token
  acquisition/storage — the `authToken` parameter is plumbed through every
  `SupabaseClient` method but nothing ever supplies it.

---

## Checked and found clean

Verified explicitly, not just "no findings":

- **Secrets**: the embedded JWT decodes to `role:"anon"` — not a service key; no
  service_role key anywhere in the repo; no tracked `local.properties`, `.env*`,
  `.xcconfig`, keystores, `.p12`/`.pem` (`git ls-files` checked); `Info.plist`,
  `project.yml`, `gradle.properties` clean; no `buildConfigField` secrets; HTTPS
  everywhere, no cert-validation overrides; the anon-key-in-client posture matches the
  web app and is documented as intentional in the file itself.
- **Dependency direction**: domain → model only; network/database have no project deps;
  DTO→domain mapping confined to `core/data`; `core/model` has no platform types;
  no expect/actual leaks platform types into common signatures.
- **Coroutines**: zero `GlobalScope`/`runBlocking` in production code; no StateFlow
  writes from background dispatchers; `CancellationException` correctly rethrown in
  `OfflineDownloadManager`; `ProfileViewModel` cancels in-flight selection on rapid taps.
- **Lifecycle (Android)**: all six screens use `koinViewModel()` +
  `collectAsStateWithLifecycle`; Koin started once with override ordering validated by
  a tested fail-fast (`SharedInit.kt:56-64`, `SharedInitTest.kt`).
- **iOS flow collection**: SKIE `for await` inside `.task` (auto-cancel on disappear),
  `@MainActor @Observable` adapters; `ReaderSharedViewModel.playAudio` uses `[weak self]`
  and idempotent teardown.
- **Design tokens (where it works)**: Android typography has zero literal `.sp`; all
  135 iOS font calls go through the token layer with registry-resolved families; M3
  schemes are 100% token-built; the 8 main Android screens are hex-free; the 57 base
  values match exactly across tokens.json, both generated files, and the Android fork;
  no `Color(red:)`/`UIColor` literals or `.foregroundColor(.white)` outside the iOS
  theme layer.
- **Persistence**: all SQL is SQLDelight-generated parameterized queries — no
  string-built SQL; serialization configured defensively (`ignoreUnknownKeys`) on both
  network and cache paths; the Android asset downloader streams, cleans up partial
  files, and sets bounded timeouts.
- **Tests that exist are real**: fakes with `awaitCancellation()`, test dispatchers,
  boundary assertions, and a regression test pinning a fixed cue-math bug — not
  checkbox tests.
- **CI/build hygiene**: no committed build artifacts; reproducible iOS project
  (gitignored `.xcodeproj` from XcodeGen, in CI and locally); PR-gating on `main`
  matches AGENTS.md's "PRs only"; workflow action majors current; configuration cache +
  build cache enabled; the font-bundling CI assertion (`kmp-build.yml:76-84`) is a
  genuinely good guard.
- **Docs**: 6 of 8 spot-checked claims matched the code exactly; `tasks/todo.md` /
  `tasks/lessons.md` are current and consistent with the latest handoff; the Phase-1
  post-mortem honestly retracts earlier false claims.
- **Privacy**: `Analytics` is a println stub tracking slugs/counts only — no PII.

---

## Calibration note (solo, mid-level, built with Claude Code)

Comparable overall to the web sibling's 7.5–8 minus about a point, and the gap is
specific, not general.

**Above the senior bar** — these are decisions, not luck: choosing a domain
`OfflineAssetCache` interface over expect/actual abuse while keeping expect/actual to
two pairs; the tested fail-fast DI wiring guard (`SharedInit.kt:56-64`) — most senior
KMP engineers don't think to guard Koin override ordering, let alone unit-test the
guard; `OfflineDownloadManager`'s documented single-threaded invariant with
job-identity checks (`OfflineDownloadManager.kt:21-31, 55-57`); SKIE + `@Observable` +
`for await` in `.task` as the interop stack (the current best practice, not the
five-year-old callback-wrapper pattern); and the CI font-bundling assertion
("measure, don't eyeball").

**At the bar**: the layered core-module split, MVI-lite single-StateFlow ViewModels
consumed natively on both platforms, native-per-platform navigation with
navigation-agnostic shared VMs, XcodeGen packaging, and the handoff-doc discipline.

**Below the bar** — three clusters, all citable:

1. **iOS lifecycle hygiene.** The AVPlayer retain cycle (`ContentView.swift:447`), the
   side-effectful `@State` defaults (`ContentView.swift:107, 196`), and the absence of
   any `clear()` path for Kotlin VMs are the classic mistakes of someone whose home
   platform is Android. A senior iOS engineer doesn't write a `deinit` that can't run.
   The Kotlin side of the same boundary (the adapters, the SKIE collection) is fine —
   it's specifically UIKit/SwiftUI object-lifetime instinct that's missing.
2. **Claims outrunning enforcement.** "Fully tokenised, Pardis palette only" is
   falsifiable in one grep (11 off-palette hexes, dead `PardisMotion`, no generator,
   Android not consuming `generated/`), and `code-rules.md` promises lint that doesn't
   exist while CI's own comment denies the test suite that does
   (`kmp-build.yml:28`). The senior habit isn't writing the rule — it's wiring the
   machine that makes the rule impossible to break, and that machine is absent.
3. **Test allocation by ease rather than risk.** The 17 tests are well-built but sit on
   the four easiest islands; the highest-risk code (`StoryRepositoryImpl`,
   `LibraryViewModel`'s positional casts, the interop layer) has zero coverage, and the
   one concrete class blocking data-layer testability (`SupabaseClient`, no interface)
   was never extracted. A senior engineer tests where it hurts, and notices when a
   design choice is the reason they can't.

None of the below-bar items are architectural — the structure would carry a team. They
are review-discipline gaps: every one would be caught by the careful self-review pass
this report is standing in for.

*Dependency-currency assessments reflect the reviewer's January 2026 knowledge cutoff.*
