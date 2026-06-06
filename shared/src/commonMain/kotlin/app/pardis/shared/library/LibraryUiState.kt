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
    val downloadProgress: Map<String, String> = emptyMap(),     // slug -> progress text while downloading
    val downloadedSizeLabels: Map<String, String> = emptyMap(), // slug -> "109 MB"
    val failedDownloads: Set<String> = emptySet(),
    val totalCachedLabel: String = "",                          // "" when nothing cached
)

sealed interface LibraryAction {
    data object Refresh : LibraryAction
    data class Search(val query: String) : LibraryAction
    data object ToggleShowOnlyCached : LibraryAction
    data class SetAgeBand(val band: String?) : LibraryAction  // null clears the age filter
    data class DownloadStory(val slug: String) : LibraryAction
    data class CancelDownload(val slug: String) : LibraryAction
    data class RemoveDownload(val slug: String) : LibraryAction
    data class OpenStory(val slug: String) : LibraryAction  // handled via callback in UI
}