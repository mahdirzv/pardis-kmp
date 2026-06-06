package app.pardis.shared.library

import app.pardis.core.model.Story

data class LibraryUiState(
    val stories: List<Story> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val cachedStorySlugs: Set<String> = emptySet(),  // slugs with local video/assets cached for offline
    val searchQuery: String = "",
    val showOnlyCached: Boolean = false,
    val localCoverUrls: Map<String, String> = emptyMap(), // slug -> local cover path if cached
    val ageBands: List<String> = emptyList(),   // distinct age bands present in the library, ordered young->old
    val selectedAgeBand: String? = null,        // null = all ages
)

sealed interface LibraryAction {
    data object Refresh : LibraryAction
    data class Search(val query: String) : LibraryAction
    data object ToggleShowOnlyCached : LibraryAction
    data class SetAgeBand(val band: String?) : LibraryAction  // null clears the age filter
    data class OpenStory(val slug: String) : LibraryAction  // handled via callback in UI
}