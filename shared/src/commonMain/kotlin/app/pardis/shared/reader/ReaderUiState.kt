package app.pardis.shared.reader

import app.pardis.core.model.StoryPage

data class ReaderUiState(
    val storySlug: String = "",
    val pages: List<StoryPage> = emptyList(),
    val currentPage: Int = 0,
    val isVideoMode: Boolean = false,
    val videoUrl: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface ReaderAction {
    data class LoadStory(val slug: String) : ReaderAction
    data object NextPage : ReaderAction
    data object PrevPage : ReaderAction
    data class GoToPage(val page: Int) : ReaderAction
    data object ToggleVideo : ReaderAction
    data object PlayNarration : ReaderAction // per page or current
    data object ErrorDismissed : ReaderAction
}