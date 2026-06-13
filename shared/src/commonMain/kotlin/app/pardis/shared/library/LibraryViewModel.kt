package app.pardis.shared.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import app.pardis.core.domain.GetLocalAssetPathUseCase
import app.pardis.core.domain.GetStoriesUseCase
import app.pardis.core.model.Story
import app.pardis.shared.analytics.Analytics
import app.pardis.shared.offline.OfflineDownloadManager
import app.pardis.shared.offline.StoryDownloadState
import app.pardis.shared.offline.formatBytes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val getStoriesUseCase: GetStoriesUseCase,
    private val getLocalAssetPath: GetLocalAssetPathUseCase,
    private val analytics: Analytics,
    private val downloadManager: OfflineDownloadManager,
) : ViewModel() {

    private val stories = MutableStateFlow<List<Story>>(emptyList())
    private val isLoading = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)
    private val cachedSlugs = MutableStateFlow<Set<String>>(emptySet())
    private val searchQuery = MutableStateFlow("")
    private val showOnlyCached = MutableStateFlow(false)
    private val localCoverUrls = MutableStateFlow<Map<String, String>>(emptyMap())
    private val selectedAgeBand = MutableStateFlow<String?>(null)

    val uiState: StateFlow<LibraryUiState> = combine(
        stories,
        isLoading,
        error,
        cachedSlugs,
        searchQuery,
        showOnlyCached,
        localCoverUrls,
        selectedAgeBand,
        downloadManager.states,
    ) { args ->
        val currentStories = args[0] as List<Story>
        val loading = args[1] as Boolean
        val err = args[2] as String?
        val cached = args[3] as Set<String>
        val query = args[4] as String
        val showOnly = args[5] as Boolean
        val covers = args[6] as Map<String, String>
        val ageBand = args[7] as String?
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

        // Age bands available for filtering, derived from the data and ordered young -> old by the
        // leading number ("4–7", "7–10", "10–14"); avoids hardcoding bands that may change server-side.
        val ageBands = currentStories.map { it.ageBand }.distinct().sortedBy { band ->
            band.takeWhile { it.isDigit() }.toIntOrNull() ?: Int.MAX_VALUE
        }

        var filtered = if (query.isBlank()) currentStories else currentStories.filter {
            it.titleEn.contains(query, ignoreCase = true) ||
            it.titleFa.contains(query, ignoreCase = true) ||
            it.ageBand.contains(query, ignoreCase = true)
        }
        if (ageBand != null) {
            filtered = filtered.filter { it.ageBand == ageBand }
        }
        if (showOnly) {
            filtered = filtered.filter { cached.contains(it.slug) }
        }
        LibraryUiState(
            stories = filtered,
            isLoading = loading,
            errorMessage = err,
            // Union the disk-computed set with the manager's downloaded set so a story downloaded
            // from the library while it's open immediately satisfies the "show only cached" filter
            // (not just after the next refresh).
            cachedStorySlugs = cached + downloadedSizeLabels.keys,
            searchQuery = query,
            showOnlyCached = showOnly,
            localCoverUrls = covers,
            ageBands = ageBands,
            selectedAgeBand = ageBand,
            downloadProgress = downloadProgress,
            downloadedSizeLabels = downloadedSizeLabels,
            failedDownloads = failedDownloads,
            totalCachedLabel = totalCachedLabel,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState(isLoading = true),
    )

    init {
        refresh()
    }

    fun onAction(action: LibraryAction) {
        when (action) {
            is LibraryAction.Refresh -> refresh()
            is LibraryAction.Search -> searchQuery.value = action.query
            is LibraryAction.ToggleShowOnlyCached -> showOnlyCached.value = !showOnlyCached.value
            is LibraryAction.SetAgeBand -> selectedAgeBand.value = action.band
            is LibraryAction.DownloadStory -> downloadManager.download(action.slug)
            is LibraryAction.CancelDownload -> downloadManager.cancel(action.slug)
            is LibraryAction.RemoveDownload -> downloadManager.remove(action.slug)
            is LibraryAction.OpenStory -> {
                // Handled by native shell callback
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null
            try {
                val result = getStoriesUseCase()
                stories.value = result
                analytics.track("library_loaded", mapOf("count" to result.size))

                // Resolve local covers for offline library cards
                val covers = mutableMapOf<String, String>()
                result.forEach { story ->
                    getLocalAssetPath(story.slug, "cover", "")?.let { covers[story.slug] = it }
                }
                localCoverUrls.value = covers

                // A story counts as offline-cached if its video OR its cover is present. The cover is
                // always part of a cache run, so this matches the reader's "any local asset" badge and
                // stays correct for partial-success caches (assets saved, video failed).
                val cached = result.mapNotNull { story ->
                    val hasVideo = getLocalAssetPath(story.slug, "video", "fa") != null ||
                        getLocalAssetPath(story.slug, "video", "en") != null
                    if (hasVideo || covers.containsKey(story.slug)) story.slug else null
                }.toSet()
                cachedSlugs.value = cached
                downloadManager.refreshState(result.map { it.slug })
            } catch (t: Throwable) {
                error.value = t.message ?: "Failed to load stories"
            } finally {
                isLoading.value = false
            }
        }
    }

    /**
     * Cancels in-flight work, mirroring what Android's ViewModelStore does via the internal
     * `clear()`. iOS has no ViewModelStore, so the Swift adapter calls this from its `deinit`.
     * Not called on Android (its ViewModelStore clears the scope on its own).
     */
    fun dispose() {
        viewModelScope.cancel()
    }
}