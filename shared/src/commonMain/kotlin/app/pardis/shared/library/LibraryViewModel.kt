package app.pardis.shared.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pardis.core.domain.GetLocalAssetPathUseCase
import app.pardis.core.domain.GetStoriesUseCase
import app.pardis.core.model.Story
import app.pardis.shared.analytics.Analytics
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
) : ViewModel() {

    private val stories = MutableStateFlow<List<Story>>(emptyList())
    private val isLoading = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)
    private val cachedSlugs = MutableStateFlow<Set<String>>(emptySet())
    private val searchQuery = MutableStateFlow("")
    private val showOnlyCached = MutableStateFlow(false)
    private val localCoverUrls = MutableStateFlow<Map<String, String>>(emptyMap())

    val uiState: StateFlow<LibraryUiState> = combine(
        stories,
        isLoading,
        error,
        cachedSlugs,
        searchQuery,
        showOnlyCached,
        localCoverUrls,
    ) { args ->
        val currentStories = args[0] as List<Story>
        val loading = args[1] as Boolean
        val err = args[2] as String?
        val cached = args[3] as Set<String>
        val query = args[4] as String
        val showOnly = args[5] as Boolean
        val covers = args[6] as Map<String, String>
        var filtered = if (query.isBlank()) currentStories else currentStories.filter {
            it.titleEn.contains(query, ignoreCase = true) ||
            it.titleFa.contains(query, ignoreCase = true) ||
            it.ageBand.contains(query, ignoreCase = true)
        }
        if (showOnly) {
            filtered = filtered.filter { cached.contains(it.slug) }
        }
        LibraryUiState(
            stories = filtered,
            isLoading = loading,
            errorMessage = err,
            cachedStorySlugs = cached,
            searchQuery = query,
            showOnlyCached = showOnly,
            localCoverUrls = covers,
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
            } catch (t: Throwable) {
                error.value = t.message ?: "Failed to load stories"
            } finally {
                isLoading.value = false
            }
        }
    }
}