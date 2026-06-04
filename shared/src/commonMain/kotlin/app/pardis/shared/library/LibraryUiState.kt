package app.pardis.shared.library

import app.pardis.core.model.Story

data class LibraryUiState(
    val stories: List<Story> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface LibraryAction {
    data object Refresh : LibraryAction
    data class OpenStory(val slug: String) : LibraryAction  // handled via callback in UI
}