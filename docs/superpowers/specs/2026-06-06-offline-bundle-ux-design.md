# Phase 2 — Offline Bundle UX (download stories from the library)

**Date:** 2026-06-06
**Branch:** `feature/phase1-offline-page-assets` (continuing)
**Status:** Design approved; ready for implementation plan.

## Goal

Let users download a whole story for offline use **directly from the library** (not only via the
reader's video-mode button), see per-story download state and size, cancel an in-progress
download, and remove a downloaded story. Builds on the asset cache fixed/hardened in Phase 1.

This is the "lean + size" cut of the roadmap's "Full offline: download story bundle … play from
cache." Background/resumable downloads and a dedicated manage-downloads screen are **explicitly
out of scope** for this cut (the architecture leaves room for them).

## Approved decisions

- **Scope:** lean + size — per-card Download → progress → ✓ Downloaded (size) + Remove; show
  per-story size + total cached. Foreground (download runs while the library VM is alive).
- **Coverage:** all `available` stories (downloads cover + page illustrations + FA/EN narration,
  plus video if the story has one). Offline reading works for every story, not just video ones.
- **Cancel:** yes — Cancel during download stops the job and deletes partial files.
- **Reader button:** keep the reader's "Cache video + assets" as-is; both write the same cache.
- **Architecture:** Approach B — a shared `OfflineDownloadManager` singleton (not just VM state),
  so download state is a single source of truth and there is a seam for background downloads later.

## Architecture

### 1. `OfflineDownloadManager` (new, `shared/…/offline/`, Koin single)

Single source of truth for per-story download state. Owns its **own** `CoroutineScope`
(`SupervisorJob() + Dispatchers.Default`) so downloads are not tied to a ViewModel lifecycle —
this is the seam for future background downloads.

State (sealed, **Kotlin-internal** — never crosses to Swift directly):

```kotlin
sealed interface StoryDownloadState {
    data object NotDownloaded : StoryDownloadState
    data class Downloading(val progress: String) : StoryDownloadState // e.g. "Downloaded 18/68 assets..."
    data class Downloaded(val sizeBytes: Long) : StoryDownloadState
    data class Failed(val message: String) : StoryDownloadState
}
```

Fields:
- `private val _states = MutableStateFlow<Map<String, StoryDownloadState>>(emptyMap())`
- `val states: StateFlow<Map<String, StoryDownloadState>>`
- `private val jobs = mutableMapOf<String, Job>()`

Dependencies: `DownloadStoryAssetsUseCase`, `ClearStoryAssetsUseCase`, `GetCachedSizeUseCase`.

API:
- `fun download(slug: String)` — if a job for `slug` is already active, no-op. Set `Downloading`,
  launch a tracked job that calls the use case with an `onProgress` that updates the
  `Downloading(progress)` state. On result: `anyCached → Downloaded(getCachedSize(slug))`, else
  `Failed`. Remove the job entry in a `finally`.
- `fun cancel(slug: String)` — cancel the job, `clearAssets(slug)` (remove partials), set
  `NotDownloaded`.
- `fun remove(slug: String)` — `clearAssets(slug)`, set `NotDownloaded`.
- `suspend fun refreshState(slugs: List<String>)` — for each slug, if cached size > 0 set
  `Downloaded(size)`, else (and not currently Downloading) `NotDownloaded`. Reflects
  reader-initiated caches. Does not clobber in-flight `Downloading` entries.

### 2. Cached size (new)

- `OfflineAssetCache.getCachedSizeBytes(slug: String): Long`
  - Android: sum `length()` of files under `cacheDir/pardis/assets/{slug}`.
  - iOS: enumerate the slug dir via `NSFileManager`, sum `NSFileSize` attributes.
  - NoOp: `0`.
- `GetCachedSizeUseCase` (domain interface + `…UseCaseImpl(assetCache)`), bound in `CoreModules`.

### 3. `LibraryViewModel`

- Inject `OfflineDownloadManager`.
- Add `manager.states` to the existing `combine`. **Project** the sealed map into flat,
  interop-safe fields on `LibraryUiState`:
  - `downloadProgress: Map<String, String>` (slug → progress text while downloading)
  - `downloadedSizes: Map<String, Long>` (slug → bytes)
  - `failedDownloads: Set<String>`
  - `totalCachedBytes: Long` (sum of downloaded sizes)
  - `cachedStorySlugs` becomes `downloadedSizes.keys` (badge + "show only cached" unchanged).
- `refresh()` additionally calls `manager.refreshState(stories.map { it.slug })`.
- New actions: `DownloadStory(slug)`, `CancelDownload(slug)`, `RemoveDownload(slug)` → delegate to
  the manager.

### 4. UI — Android Compose (`PardisApp.kt`) + iOS SwiftUI (`ContentView.swift`)

Per library card, derive state from the flat maps (precedence: downloading → downloaded → failed →
not-downloaded):
- **NotDownloaded** → `[ Download offline ]`
- **Downloading** → the progress string verbatim (the use case's `onProgress`, e.g.
  `"Downloaded 18/68 assets…"`; no parsing) + `[ Cancel ]`
- **Downloaded** → `"✓ Offline (109 MB)"` + `[ Remove ]`
- **Failed** → `"Download failed"` + `[ Retry ]`

Plus a **"Cached: 218 MB"** line near the "show only cached" toggle when `totalCachedBytes > 0`.

Shared `formatBytes(Long): String` helper (commonMain) so Android and iOS format identically
(e.g. `< 1 MB` shown as KB, otherwise `NNN MB` / `N.N GB`).

### 5. DI

- `OfflineDownloadManager` as a Koin `single` in a new `offlineModule` (shared), added to
  `pardisSharedModules`. Loaded after core modules (its deps live in core), consistent with the
  ordering fix from Phase 1.

## Error handling / partial success

Reuses `StoryAssetsResult` from Phase 1: `anyCached → Downloaded(size)` (offline reading survives a
failed video), nothing cached → `Failed(message)`. Cancel/remove always delete the slug dir so a
retry starts clean. A no-op cache (e.g. misconfigured DI) yields size 0 → the manager would surface
`Failed`, which is the correct loud behavior.

## Testing

- **Android:** `:PardisAndroidApp:assembleDebug`, install, device-verify the full lifecycle:
  download a story from the library → progress → ✓ + size → Remove; cancel mid-download (partials
  gone); reader "Cache video + assets" still reflects in the library after refresh; total size
  updates.
- **iOS:** `:shared:compileKotlinIosArm64` here; **Swift requires an Xcode build by the user**
  (no full Xcode in the working environment). Sealed `StoryDownloadState` stays Kotlin-internal so
  Swift only consumes flat maps/sets.
- No automated unit tests in this cut (project has none yet); a Koin/manager test is a tracked
  follow-up.

## Out of scope (future)

- Background / resumable downloads (WorkManager on Android, BGTaskScheduler on iOS) — the manager's
  own scope is the seam for this.
- Dedicated "Manage downloads" screen with bulk actions.
- Routing the reader's "Cache video + assets" through the manager (currently it writes the cache
  directly; the manager reflects it on `refreshState`).
- Per-asset numeric progress bar (we show the use case's "X/Y" text).

## Key files

- New: `shared/src/commonMain/kotlin/app/pardis/shared/offline/OfflineDownloadManager.kt`,
  `…/offline/OfflineModule.kt`, `…/offline/StoryDownloadState.kt`, a `formatBytes` util.
- New: `core/domain/…/GetCachedSizeUseCase.kt`, `core/data/…/GetCachedSizeUseCaseImpl.kt`.
- Edit: `core/domain/…/OfflineAssetCache.kt` (+ Android/iOS/NoOp impls) for `getCachedSizeBytes`.
- Edit: `core/di/…/CoreModules.kt` (bind size use case), `shared/…/SharedInit.kt` (add offlineModule).
- Edit: `shared/…/library/LibraryUiState.kt`, `LibraryViewModel.kt`.
- Edit: `app/…/ui/PardisApp.kt` (library card states + total).
- Edit: `iosApp/iosApp/ContentView.swift`, `LibrarySharedViewModel.swift`.
