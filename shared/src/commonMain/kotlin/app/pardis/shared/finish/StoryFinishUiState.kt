package app.pardis.shared.finish

import app.pardis.core.model.VocabItem

/**
 * State for the end-of-story celebration. [words] are the real glossary terms
 * pulled from the story's pages; the streak/star counts shown on the screen are
 * decorative gamification the app does not yet track.
 */
data class StoryFinishUiState(
    val slug: String = "",
    val words: List<VocabItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface StoryFinishAction {
    data class Load(val slug: String) : StoryFinishAction
    data object Retry : StoryFinishAction
}
