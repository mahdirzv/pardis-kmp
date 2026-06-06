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
