package app.pardis.shared.detail

import app.pardis.core.model.Story
import app.pardis.core.model.VocabItem

/**
 * Screen state for the story detail page (hero, synopsis, meta, vocab preview, CTA).
 * Platform UIs resolve raw values into native chrome.
 */
data class StoryDetailUiState(
    val slug: String = "",
    val story: Story? = null,
    val words: List<VocabItem> = emptyList(),
    val savedPage: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    /** True when the reader has prior progress, so the CTA reads "Continue" instead of "Start". */
    val canResume: Boolean get() = savedPage > 0
}

sealed interface StoryDetailAction {
    data class LoadStory(val slug: String) : StoryDetailAction
    data object Retry : StoryDetailAction
    data object ErrorDismissed : StoryDetailAction
}
