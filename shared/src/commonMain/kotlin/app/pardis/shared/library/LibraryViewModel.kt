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

    val uiState: StateFlow<LibraryUiState> = combine(
        stories,
        isLoading,
        error,
        cachedSlugs,
        searchQuery,
    ) { currentStories, loading, err, cached, query ->
        val filtered = if (query.isBlank()) currentStories else currentStories.filter {
            it.titleEn.contains(query, ignoreCase = true) ||
            it.titleFa.contains(query, ignoreCase = true) ||
            it.ageBand.contains(query, ignoreCase = true)
        }
        LibraryUiState(
            stories = filtered,
            isLoading = loading,
            errorMessage = err,
            cachedStorySlugs = cached,
            searchQuery = query,
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

                // Check which stories have local video assets cached (for offline badge in list)
                val cached = result.mapNotNull { story ->
                    val hasFa = getLocalAssetPath(story.slug, "video", "fa") != null
                    val hasEn = getLocalAssetPath(story.slug, "video", "en") != null
                    if (hasFa || hasEn) story.slug else null
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