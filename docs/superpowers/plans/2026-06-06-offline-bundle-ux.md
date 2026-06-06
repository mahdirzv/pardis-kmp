# Offline Bundle UX Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let users download a whole story for offline use directly from the library — with per-story state (download / progress / ✓ size / failed), cancel, remove, and a total cached size — built on the Phase 1 asset cache.

**Architecture:** Approach B — a shared `OfflineDownloadManager` singleton is the single source of truth for download state (own coroutine scope, seam for future background downloads). `LibraryViewModel` projects the manager's sealed states into flat, interop-safe fields on `LibraryUiState`; the sealed type never crosses to Swift. The reader's existing "Cache video + assets" button is unchanged and reflected via `refreshState` on library load.

**Tech Stack:** Kotlin Multiplatform (core/* + shared), Koin DI, kotlinx-coroutines/Flow, Jetbrains Compose (Android), SwiftUI (iOS).

**Verification approach (read first):** This repo has **no unit-test harness**, and the working environment has **no full Xcode** (Swift cannot be compiled here). So each task is verified by:
- **Gradle compile:** `./gradlew :PardisAndroidApp:assembleDebug` and `./gradlew :shared:compileKotlinIosArm64` (with `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`).
- **On-device Android** for UI tasks (adb install + manual check on the connected device).
- **iOS Swift** changes are compiled by the **user in Xcode** (called out per task).
Do NOT invent a test framework. Commit after each task.

---

## File Structure

**New files**
- `core/domain/src/commonMain/kotlin/app/pardis/core/domain/GetCachedSizeUseCase.kt` — size use-case interface.
- `core/data/src/commonMain/kotlin/app/pardis/core/data/GetCachedSizeUseCaseImpl.kt` — impl over the cache.
- `shared/src/commonMain/kotlin/app/pardis/shared/offline/StoryDownloadState.kt` — sealed state (Kotlin-internal).
- `shared/src/commonMain/kotlin/app/pardis/shared/offline/FormatBytes.kt` — `formatBytes(Long): String`.
- `shared/src/commonMain/kotlin/app/pardis/shared/offline/OfflineDownloadManager.kt` — the manager.
- `shared/src/commonMain/kotlin/app/pardis/shared/offline/OfflineModule.kt` — Koin `offlineModule`.

**Modified files**
- `core/domain/src/commonMain/kotlin/app/pardis/core/domain/OfflineAssetCache.kt` — add `getCachedSizeBytes`.
- `core/data/src/androidMain/kotlin/app/pardis/core/data/AndroidOfflineAssetCache.kt` — impl.
- `core/data/src/iosMain/kotlin/app/pardis/core/data/IosOfflineAssetCache.kt` — impl.
- `core/data/src/commonMain/kotlin/app/pardis/core/data/NoOpOfflineAssetCache.kt` — impl (= 0).
- `core/di/src/commonMain/kotlin/app/pardis/core/di/CoreModules.kt` — bind `GetCachedSizeUseCase`.
- `shared/src/commonMain/kotlin/app/pardis/shared/SharedInit.kt` — add `offlineModule`.
- `shared/src/commonMain/kotlin/app/pardis/shared/library/LibraryUiState.kt` — new fields + actions.
- `shared/src/commonMain/kotlin/app/pardis/shared/library/LibraryViewModel.kt` — inject manager, project state, actions.
- `shared/src/commonMain/kotlin/app/pardis/shared/library/LibraryModule.kt` — pass manager to VM.
- `app/src/main/java/app/pardis/android/ui/PardisApp.kt` — library card states + total.
- `iosApp/iosApp/LibrarySharedViewModel.swift` — fields + actions.
- `iosApp/iosApp/ContentView.swift` — library card states + total.
- `docs/ROADMAP.md` — mark the lean offline-bundle cut done.

**Interop note (refines the spec):** to avoid bridging Kotlin `Long` maps to Swift, the UI-facing
size fields on `LibraryUiState` are **pre-formatted strings** (`downloadedSizeLabels: Map<String,String>`,
`totalCachedLabel: String`) produced via `formatBytes` in the ViewModel. Swift only ever sees
`String`/`Set<String>` collections.

---

## Task 1: Cached-size capability on the asset cache

**Files:**
- Modify: `core/domain/src/commonMain/kotlin/app/pardis/core/domain/OfflineAssetCache.kt`
- Modify: `core/data/src/androidMain/kotlin/app/pardis/core/data/AndroidOfflineAssetCache.kt`
- Modify: `core/data/src/iosMain/kotlin/app/pardis/core/data/IosOfflineAssetCache.kt`
- Modify: `core/data/src/commonMain/kotlin/app/pardis/core/data/NoOpOfflineAssetCache.kt`
- Create: `core/domain/src/commonMain/kotlin/app/pardis/core/domain/GetCachedSizeUseCase.kt`
- Create: `core/data/src/commonMain/kotlin/app/pardis/core/data/GetCachedSizeUseCaseImpl.kt`
- Modify: `core/di/src/commonMain/kotlin/app/pardis/core/di/CoreModules.kt`

- [ ] **Step 1: Add the interface method.** In `OfflineAssetCache.kt`, add inside the interface (after `clearAssetsForStory`):

```kotlin
    /** Total bytes currently cached for a slug (sum of all asset files); 0 if nothing cached. */
    suspend fun getCachedSizeBytes(slug: String): Long
```

- [ ] **Step 2: Android impl.** In `AndroidOfflineAssetCache.kt`, add after `clearAssetsForStory`:

```kotlin
    override suspend fun getCachedSizeBytes(slug: String): Long = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "pardis/assets/$slug")
        if (!dir.exists()) 0L
        else dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }
```

- [ ] **Step 3: iOS impl.** In `IosOfflineAssetCache.kt`, add the imports `platform.Foundation.NSNumber` and `platform.Foundation.NSFileSize`, then add after `clearAssetsForStory`:

```kotlin
    override suspend fun getCachedSizeBytes(slug: String): Long = withContext(Dispatchers.IO) {
        val dir = assetsDir(slug)
        val fm = NSFileManager.defaultManager
        val names = fm.contentsOfDirectoryAtPath(dir, null) ?: return@withContext 0L
        var total = 0L
        for (name in names) {
            val attrs = fm.attributesOfItemAtPath("$dir/$name", null)
            total += (attrs?.get(NSFileSize) as? NSNumber)?.longLongValue ?: 0L
        }
        total
    }
```

- [ ] **Step 4: NoOp impl.** In `NoOpOfflineAssetCache.kt`, add to the class:

```kotlin
    override suspend fun getCachedSizeBytes(slug: String): Long = 0L
```

- [ ] **Step 5: Create the use case.** `GetCachedSizeUseCase.kt`:

```kotlin
package app.pardis.core.domain

/** Returns total bytes cached for a story (0 if none). */
interface GetCachedSizeUseCase {
    suspend operator fun invoke(slug: String): Long
}
```

- [ ] **Step 6: Create the impl.** `GetCachedSizeUseCaseImpl.kt`:

```kotlin
package app.pardis.core.data

import app.pardis.core.domain.GetCachedSizeUseCase
import app.pardis.core.domain.OfflineAssetCache

class GetCachedSizeUseCaseImpl(
    private val assetCache: OfflineAssetCache,
) : GetCachedSizeUseCase {
    override suspend fun invoke(slug: String): Long = assetCache.getCachedSizeBytes(slug)
}
```

- [ ] **Step 7: Bind it in DI.** In `CoreModules.kt`, after the `ClearStoryAssetsUseCase` binding (around line 63), add:

```kotlin
        single<app.pardis.core.domain.GetCachedSizeUseCase> { app.pardis.core.data.GetCachedSizeUseCaseImpl(get()) }
```

- [ ] **Step 8: Verify compile (both platforms).**

Run:
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :core:data:compileDebugKotlin :core:data:compileKotlinIosArm64 :core:di:compileKotlinIosArm64
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 9: Commit.**

```bash
git add core/domain core/data core/di
git commit -m "feat(offline): add getCachedSizeBytes to OfflineAssetCache + GetCachedSizeUseCase"
```

---

## Task 2: Download-state model + byte formatter

**Files:**
- Create: `shared/src/commonMain/kotlin/app/pardis/shared/offline/StoryDownloadState.kt`
- Create: `shared/src/commonMain/kotlin/app/pardis/shared/offline/FormatBytes.kt`

- [ ] **Step 1: Create the sealed state.** `StoryDownloadState.kt`:

```kotlin
package app.pardis.shared.offline

/**
 * Per-story download state. Kept internal to Kotlin (manager + LibraryViewModel); the ViewModel
 * projects this into flat String/Set fields on LibraryUiState so Swift never bridges the sealed type.
 */
sealed interface StoryDownloadState {
    data object NotDownloaded : StoryDownloadState
    data class Downloading(val progress: String) : StoryDownloadState
    data class Downloaded(val sizeBytes: Long) : StoryDownloadState
    data class Failed(val message: String) : StoryDownloadState
}
```

- [ ] **Step 2: Create the formatter.** `FormatBytes.kt`:

```kotlin
package app.pardis.shared.offline

/** Human-friendly size: "<NNN KB" under 1 MB, "NNN MB" under 1 GB, else "N.N GB". */
fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 MB"
    val mb = bytes / (1024.0 * 1024.0)
    return when {
        mb < 1.0 -> "${(bytes / 1024.0).toInt()} KB"
        mb < 1024.0 -> "${mb.toInt()} MB"
        else -> {
            val gbTenths = (mb / 1024.0 * 10).toInt()
            "${gbTenths / 10}.${gbTenths % 10} GB"
        }
    }
}
```

- [ ] **Step 3: Verify compile.**

Run:
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :shared:compileKotlinIosArm64
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit.**

```bash
git add shared/src/commonMain/kotlin/app/pardis/shared/offline
git commit -m "feat(offline): StoryDownloadState + formatBytes helper"
```

---

## Task 3: OfflineDownloadManager + Koin module + SharedInit wiring

**Files:**
- Create: `shared/src/commonMain/kotlin/app/pardis/shared/offline/OfflineDownloadManager.kt`
- Create: `shared/src/commonMain/kotlin/app/pardis/shared/offline/OfflineModule.kt`
- Modify: `shared/src/commonMain/kotlin/app/pardis/shared/SharedInit.kt`

- [ ] **Step 1: Create the manager.** `OfflineDownloadManager.kt`:

```kotlin
package app.pardis.shared.offline

import app.pardis.core.domain.ClearStoryAssetsUseCase
import app.pardis.core.domain.DownloadStoryAssetsUseCase
import app.pardis.core.domain.GetCachedSizeUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Single source of truth for per-story offline download state, shared across screens.
 * Owns its own scope on the main dispatcher (state/job-map mutations stay single-threaded;
 * the heavy IO happens inside the cache impls via withContext(IO)). The scope outliving any
 * ViewModel is the seam for future background downloads.
 */
class OfflineDownloadManager(
    private val downloadStoryAssets: DownloadStoryAssetsUseCase,
    private val clearStoryAssets: ClearStoryAssetsUseCase,
    private val getCachedSize: GetCachedSizeUseCase,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    private val _states = MutableStateFlow<Map<String, StoryDownloadState>>(emptyMap())
    val states: StateFlow<Map<String, StoryDownloadState>> = _states.asStateFlow()

    private val jobs = mutableMapOf<String, Job>()

    private fun setState(slug: String, state: StoryDownloadState) {
        _states.update { it + (slug to state) }
    }

    fun download(slug: String) {
        if (jobs[slug]?.isActive == true) return
        setState(slug, StoryDownloadState.Downloading("Starting download..."))
        jobs[slug] = scope.launch {
            try {
                val result = downloadStoryAssets(slug) { progress ->
                    setState(slug, StoryDownloadState.Downloading(progress))
                }
                if (result.anyCached) {
                    setState(slug, StoryDownloadState.Downloaded(getCachedSize(slug)))
                } else {
                    setState(slug, StoryDownloadState.Failed("Download failed (check connection)"))
                }
            } catch (e: CancellationException) {
                throw e // never swallow cancellation
            } catch (e: Exception) {
                setState(slug, StoryDownloadState.Failed(e.message ?: "Download failed"))
            } finally {
                jobs.remove(slug)
            }
        }
    }

    fun cancel(slug: String) {
        jobs.remove(slug)?.cancel()
        scope.launch {
            clearStoryAssets(slug)
            setState(slug, StoryDownloadState.NotDownloaded)
        }
    }

    fun remove(slug: String) {
        scope.launch {
            clearStoryAssets(slug)
            setState(slug, StoryDownloadState.NotDownloaded)
        }
    }

    /** Reflect on-disk reality (incl. reader-initiated caches). Skips in-flight downloads. */
    suspend fun refreshState(slugs: List<String>) {
        for (slug in slugs) {
            if (jobs[slug]?.isActive == true) continue
            val size = getCachedSize(slug)
            setState(
                slug,
                if (size > 0L) StoryDownloadState.Downloaded(size) else StoryDownloadState.NotDownloaded,
            )
        }
    }
}
```

- [ ] **Step 2: Create the Koin module.** `OfflineModule.kt`:

```kotlin
package app.pardis.shared.offline

import org.koin.dsl.module

val offlineModule = module {
    single { OfflineDownloadManager(get(), get(), get()) }
}
```

- [ ] **Step 3: Register the module.** In `SharedInit.kt`, add the import `import app.pardis.shared.offline.offlineModule` and add `offlineModule` to the list:

```kotlin
val pardisSharedModules: List<Module> = listOf(
    libraryModule,
    readerModule,
    analyticsModule,
    offlineModule,
)
```

- [ ] **Step 4: Verify compile (both platforms).**

Run:
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :shared:compileDebugKotlin :shared:compileKotlinIosArm64
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit.**

```bash
git add shared/src/commonMain/kotlin/app/pardis/shared/offline shared/src/commonMain/kotlin/app/pardis/shared/SharedInit.kt
git commit -m "feat(offline): OfflineDownloadManager single source of truth + DI wiring"
```

---

## Task 4: Library state, actions, and ViewModel projection

**Files:**
- Modify: `shared/src/commonMain/kotlin/app/pardis/shared/library/LibraryUiState.kt`
- Modify: `shared/src/commonMain/kotlin/app/pardis/shared/library/LibraryViewModel.kt`
- Modify: `shared/src/commonMain/kotlin/app/pardis/shared/library/LibraryModule.kt`

- [ ] **Step 1: Add UiState fields + actions.** In `LibraryUiState.kt`, add to the `LibraryUiState` data class (after `selectedAgeBand`):

```kotlin
    val downloadProgress: Map<String, String> = emptyMap(),     // slug -> progress text while downloading
    val downloadedSizeLabels: Map<String, String> = emptyMap(), // slug -> "109 MB"
    val failedDownloads: Set<String> = emptySet(),
    val totalCachedLabel: String = "",                          // "" when nothing cached
```

And add to the `LibraryAction` sealed interface:

```kotlin
    data class DownloadStory(val slug: String) : LibraryAction
    data class CancelDownload(val slug: String) : LibraryAction
    data class RemoveDownload(val slug: String) : LibraryAction
```

- [ ] **Step 2: Inject the manager + project its state.** In `LibraryViewModel.kt`:

  (a) Add imports:
```kotlin
import app.pardis.shared.offline.OfflineDownloadManager
import app.pardis.shared.offline.StoryDownloadState
import app.pardis.shared.offline.formatBytes
```

  (b) Add the constructor param (after `analytics`):
```kotlin
    private val downloadManager: OfflineDownloadManager,
```

  (c) Add `downloadManager.states` as the 9th flow in `combine(...)` (add it right after `selectedAgeBand,`):
```kotlin
        selectedAgeBand,
        downloadManager.states,
```

  (d) Inside the transform, add (after `val ageBand = args[7] as String?`):
```kotlin
        @Suppress("UNCHECKED_CAST")
        val dlStates = args[8] as Map<String, StoryDownloadState>
        val downloadProgress = dlStates.mapNotNull { (s, st) ->
            (st as? StoryDownloadState.Downloading)?.let { s to it.progress }
        }.toMap()
        val downloadedSizeLabels = dlStates.mapNotNull { (s, st) ->
            (st as? StoryDownloadState.Downloaded)?.let { s to formatBytes(it.sizeBytes) }
        }.toMap()
        val failedDownloads = dlStates.filterValues { it is StoryDownloadState.Failed }.keys.toSet()
        val totalBytes = dlStates.values.filterIsInstance<StoryDownloadState.Downloaded>().sumOf { it.sizeBytes }
        val totalCachedLabel = if (totalBytes > 0L) formatBytes(totalBytes) else ""
```

  (e) In the returned `LibraryUiState(...)`, add the new fields:
```kotlin
            downloadProgress = downloadProgress,
            downloadedSizeLabels = downloadedSizeLabels,
            failedDownloads = failedDownloads,
            totalCachedLabel = totalCachedLabel,
```

  (f) In `refresh()`, after `cachedSlugs.value = cached` (the existing block), add:
```kotlin
                downloadManager.refreshState(result.map { it.slug })
```

  (g) In `onAction(...)`, add cases (before `is LibraryAction.OpenStory`):
```kotlin
            is LibraryAction.DownloadStory -> downloadManager.download(action.slug)
            is LibraryAction.CancelDownload -> downloadManager.cancel(action.slug)
            is LibraryAction.RemoveDownload -> downloadManager.remove(action.slug)
```

- [ ] **Step 3: Pass the manager in DI.** In `LibraryModule.kt`, update the viewModel definition to add a fourth `get()`:

```kotlin
    viewModel { LibraryViewModel(get(), get(), get(), get()) }
```

- [ ] **Step 4: Verify compile (both platforms).**

Run:
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :shared:compileDebugKotlin :shared:compileKotlinIosArm64
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit.**

```bash
git add shared/src/commonMain/kotlin/app/pardis/shared/library
git commit -m "feat(library): project download state into LibraryUiState + download/cancel/remove actions"
```

---

## Task 5: Android library card download controls + total

**Files:**
- Modify: `app/src/main/java/app/pardis/android/ui/PardisApp.kt`

- [ ] **Step 1: Show total cached near the toggle.** In `LibraryScreen`, immediately AFTER the "show only cached" `Button { ... }` block (before `Spacer(Modifier.height(PardisSpacing.md))`), add:

```kotlin
        if (state.totalCachedLabel.isNotEmpty()) {
            Spacer(Modifier.height(PardisSpacing.xs))
            Text(
                "Cached offline: ${state.totalCachedLabel}",
                style = MaterialTheme.typography.labelSmall,
                color = PardisColors.inkSoft
            )
        }
```

- [ ] **Step 2: Pass per-story download info into the card.** In `LibraryScreen`'s `items(...) { story -> StoryCard(...) }`, replace the `StoryCard(...)` call with:

```kotlin
                StoryCard(
                    titleEn = story.titleEn,
                    titleFa = story.titleFa,
                    ageBand = story.ageBand,
                    minutes = story.minutes,
                    vocabCount = story.vocabCount,
                    coverUrl = state.localCoverUrls[story.slug] ?: story.coverUrl,
                    downloadProgress = state.downloadProgress[story.slug],
                    downloadedSizeLabel = state.downloadedSizeLabels[story.slug],
                    isFailed = state.failedDownloads.contains(story.slug),
                    onClick = { onOpenStory(story.slug) },
                    onDownload = { onAction(LibraryAction.DownloadStory(story.slug)) },
                    onCancel = { onAction(LibraryAction.CancelDownload(story.slug)) },
                    onRemove = { onAction(LibraryAction.RemoveDownload(story.slug)) },
                )
```

- [ ] **Step 3: Rewrite `StoryCard` with a download-control row.** Replace the entire `StoryCard` composable with:

```kotlin
@Composable
private fun StoryCard(
    titleEn: String,
    titleFa: String,
    ageBand: String,
    minutes: Int,
    vocabCount: Int,
    coverUrl: String?,
    downloadProgress: String?,
    downloadedSizeLabel: String?,
    isFailed: Boolean,
    onClick: () -> Unit,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onRemove: () -> Unit,
) {
    PardisCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        onClick = onClick
    ) {
        Column(Modifier.padding(PardisSpacing.md)) {
            Row {
                if (coverUrl != null) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = "Cover image for story: $titleEn in $ageBand age band",
                        modifier = Modifier
                            .size(60.dp)
                            .padding(end = PardisSpacing.sm)
                    )
                }
                Column {
                    Text(
                        titleEn,
                        style = MaterialTheme.typography.titleMedium,
                        color = PardisColors.ink,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(titleFa, style = MaterialTheme.typography.bodyLarge, color = PardisColors.indigo)
                    Spacer(Modifier.height(PardisSpacing.xs))
                    Row(horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
                        Text("$ageBand • ${minutes}m", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkSoft)
                        Text("• $vocabCount words", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkMuted)
                    }
                }
            }
            Spacer(Modifier.height(PardisSpacing.sm))
            // Offline download control: reflects OfflineDownloadManager state for this story.
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm)) {
                when {
                    downloadProgress != null -> {
                        Text(downloadProgress, style = MaterialTheme.typography.labelSmall, color = PardisColors.inkSoft)
                        Spacer(Modifier.weight(1f))
                        OutlinedButton(onClick = onCancel) { Text("Cancel", style = MaterialTheme.typography.labelSmall) }
                    }
                    downloadedSizeLabel != null -> {
                        Text("✓ Offline ($downloadedSizeLabel)", style = MaterialTheme.typography.labelSmall, color = PardisColors.mint)
                        Spacer(Modifier.weight(1f))
                        OutlinedButton(onClick = onRemove) { Text("Remove", style = MaterialTheme.typography.labelSmall) }
                    }
                    isFailed -> {
                        Text("Download failed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.weight(1f))
                        Button(onClick = onDownload) { Text("Retry", style = MaterialTheme.typography.labelSmall) }
                    }
                    else -> {
                        Spacer(Modifier.weight(1f))
                        Button(onClick = onDownload, colors = ButtonDefaults.buttonColors(containerColor = PardisColors.saffron)) {
                            Text("Download offline", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 4: Build the APK.**

Run:
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :PardisAndroidApp:assembleDebug
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Install + device-verify the full lifecycle.**

Run (USB serial `RFCR11CB9JM`; use `adb devices` if it differs):
```bash
ADB="$HOME/Library/Android/sdk/platform-tools/adb"
"$ADB" -s RFCR11CB9JM install -r app/build/outputs/apk/debug/PardisAndroidApp-debug.apk
```
On the device, in the Library: each card shows **Download offline**. Tap it on a story →
progress text ("Downloaded X/Y assets…") + **Cancel** → on completion **✓ Offline (NN MB)** +
**Remove**, and "Cached offline: NN MB" appears near the toggle. Tap **Cancel** mid-download on
another story → it returns to **Download offline** (verify partials gone:
`"$ADB" -s RFCR11CB9JM shell run-as app.pardis.reader ls cache/pardis/assets/<slug>` → no such dir).
Tap **Remove** → returns to **Download offline**, total updates. Confirm a story cached from the
reader's "Cache video + assets" shows **✓ Offline** in the library after pulling Refresh.

- [ ] **Step 6: Commit.**

```bash
git add app/src/main/java/app/pardis/android/ui/PardisApp.kt
git commit -m "feat(library/android): per-card offline download controls + total cached size"
```

---

## Task 6: iOS library card download controls + total

**Files:**
- Modify: `iosApp/iosApp/LibrarySharedViewModel.swift`
- Modify: `iosApp/iosApp/ContentView.swift`

- [ ] **Step 1: Expose the new fields + actions on the iOS VM.** In `LibrarySharedViewModel.swift`, add properties (after `selectedAgeBand`):

```swift
    var downloadProgress: [String: String] = [:]
    var downloadedSizeLabels: [String: String] = [:]
    var failedDownloads: Set<String> = []
    var totalCachedLabel: String = ""
```

Add methods (after `setAgeBand`):

```swift
    func downloadStory(_ slug: String) {
        viewModel.onAction(action: LibraryActionDownloadStory(slug: slug))
    }

    func cancelDownload(_ slug: String) {
        viewModel.onAction(action: LibraryActionCancelDownload(slug: slug))
    }

    func removeDownload(_ slug: String) {
        viewModel.onAction(action: LibraryActionRemoveDownload(slug: slug))
    }
```

In `apply(_:)`, add (after `self.selectedAgeBand = state.selectedAgeBand`):

```swift
        self.downloadProgress = state.downloadProgress
        self.downloadedSizeLabels = state.downloadedSizeLabels
        self.failedDownloads = Set(state.failedDownloads)
        self.totalCachedLabel = state.totalCachedLabel
```

- [ ] **Step 2: Total cached label.** In `ContentView.swift` `LibraryScreen`, after the "show only cached" `Button { ... }` block, add:

```swift
            if !model.totalCachedLabel.isEmpty {
                Text("Cached offline: \(model.totalCachedLabel)")
                    .font(.caption)
                    .foregroundStyle(PardisColors.inkSoft)
            }
```

- [ ] **Step 3: Per-card download controls.** In the `List(model.stories ...)` row, inside the inner `VStack(alignment: .leading, spacing: 4)`, REPLACE the existing `if model.cachedStorySlugs.contains(story.slug) { Text("✓ Offline") ... }` block with:

```swift
                        if let progress = model.downloadProgress[story.slug] {
                            HStack {
                                Text(progress).font(.caption).foregroundStyle(PardisColors.inkSoft)
                                Spacer()
                                Button("Cancel") { model.cancelDownload(story.slug) }
                                    .buttonStyle(.bordered).controlSize(.small)
                            }
                        } else if let size = model.downloadedSizeLabels[story.slug] {
                            HStack {
                                Text("✓ Offline (\(size))").font(.caption).foregroundStyle(PardisColors.mint)
                                Spacer()
                                Button("Remove") { model.removeDownload(story.slug) }
                                    .buttonStyle(.bordered).controlSize(.small)
                            }
                        } else if model.failedDownloads.contains(story.slug) {
                            HStack {
                                Text("Download failed").font(.caption).foregroundStyle(.red)
                                Spacer()
                                Button("Retry") { model.downloadStory(story.slug) }
                                    .buttonStyle(.borderedProminent).controlSize(.small).tint(PardisColors.saffron)
                            }
                        } else {
                            Button("Download offline") { model.downloadStory(story.slug) }
                                .buttonStyle(.borderedProminent).controlSize(.small).tint(PardisColors.saffron)
                        }
```

Note: the buttons live inside the row that also has `.onTapGesture { onSelect(story.slug) }`. Add
`.buttonStyle(.bordered)`/`.borderedProminent` (above) so taps register on the buttons; if taps
still fall through to the row during testing, wrap each button with `.onTapGesture {}` is NOT the
fix — instead the user should confirm button hit-testing in Xcode (documented as a manual check).

- [ ] **Step 4: Verify the Kotlin framework still compiles.**

Run:
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :shared:compileKotlinIosArm64
```
Expected: `BUILD SUCCESSFUL`. (This validates the `LibraryAction*` symbols the Swift calls exist; the Swift itself is built by the user in Xcode.)

- [ ] **Step 5: USER verifies Swift in Xcode.** Ask the user to open `iosApp/iosApp.xcodeproj`, build, and confirm: the library cards show Download/Progress+Cancel/✓size+Remove/Failed+Retry, the total label shows, and the download buttons are tappable without triggering row navigation.

- [ ] **Step 6: Commit.**

```bash
git add iosApp/iosApp/LibrarySharedViewModel.swift iosApp/iosApp/ContentView.swift
git commit -m "feat(library/ios): per-card offline download controls + total cached size"
```

---

## Task 7: Docs + roadmap

**Files:**
- Modify: `docs/ROADMAP.md`

- [ ] **Step 1: Update the Phase 2 offline bullet.** In `docs/ROADMAP.md`, replace the Phase 2 line:

```
- Full offline: download story bundle (pages JSON + images + audio + optional MP4), manifest, play from cache. (foundational asset caching + pages JSON + video download done in Phase 1; bundle UI/manifest next)
```

with:

```
- Full offline: download story bundle (pages JSON + images + audio + optional MP4), manifest, play from cache. (foundational asset caching + pages JSON + video download done in Phase 1; **library "Download offline" per story via shared OfflineDownloadManager — progress/cancel/remove + per-story & total size, all stories — done (lean cut)**; background/resumable downloads + manifest + manage-downloads screen still next)
```

- [ ] **Step 2: Commit.**

```bash
git add docs/ROADMAP.md
git commit -m "docs: mark lean offline-bundle (library download) done in roadmap"
```

---

## Self-Review

- **Spec coverage:** manager (Task 3) ✓, cached size (Task 1) ✓, state model + formatter (Task 2) ✓,
  LibraryViewModel projection + actions (Task 4) ✓, Android UI (Task 5) ✓, iOS UI (Task 6) ✓,
  DI/SharedInit (Tasks 1,3,4) ✓, partial-success → Downloaded/Failed (Task 3 `result.anyCached`) ✓,
  reader unchanged + reflected via refreshState (Task 3 `refreshState`, Task 4 step 2f) ✓, total
  size (Tasks 4–6) ✓, docs (Task 7) ✓.
- **Spec refinement:** size fields surface as pre-formatted `String` labels (not `Long`) for Swift
  interop — documented in the File Structure interop note and used consistently in Tasks 4–6.
- **Type consistency:** `StoryDownloadState` variants (`NotDownloaded`/`Downloading(progress)`/
  `Downloaded(sizeBytes)`/`Failed(message)`) used identically across Tasks 2–4. `formatBytes(Long)`
  defined Task 2, used Tasks 3? (no — manager stores raw bytes) / 4 (yes). Manager stores
  `Downloaded(sizeBytes)`; ViewModel formats. `LibraryAction.DownloadStory/CancelDownload/
  RemoveDownload` defined Task 4, consumed Tasks 5–6. `getCachedSizeBytes` defined Task 1, used by
  `GetCachedSizeUseCaseImpl` (Task 1) → manager (Task 3). Consistent.
- **No placeholders:** all steps contain concrete code/commands.
