package app.pardis.shared.finish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pardis.core.domain.GetStoriesUseCase
import app.pardis.core.domain.GetStoryPagesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Loads the glossary words for a finished story (for the "garden" chips) plus the
 * story's title and the next story in the library (for the "Next story" CTA).
 * Navigation stays platform-owned.
 */
class StoryFinishViewModel(
    private val getStoryPages: GetStoryPagesUseCase,
    private val getStories: GetStoriesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoryFinishUiState())
    val uiState: StateFlow<StoryFinishUiState> = _uiState.asStateFlow()

    fun onAction(action: StoryFinishAction) {
        when (action) {
            is StoryFinishAction.Load -> load(action.slug)
            is StoryFinishAction.Retry -> load(_uiState.value.slug)
        }
    }

    private fun load(slug: String) {
        if (slug.isEmpty()) return
        val current = _uiState.value
        if (current.slug == slug && current.words.isNotEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(slug = slug, isLoading = true, errorMessage = null) }
            try {
                val words = getStoryPages(slug)
                    .flatMap { it.vocabulary }
                    .distinctBy { it.fa }
                val stories = getStories()
                val index = stories.indexOfFirst { it.slug == slug }
                val title = stories.getOrNull(index)?.titleEn ?: ""
                val nextSlug = if (stories.size > 1 && index >= 0) {
                    stories[(index + 1) % stories.size].slug
                } else null
                _uiState.update {
                    it.copy(words = words, title = title, nextSlug = nextSlug, isLoading = false)
                }
            } catch (e: Throwable) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Couldn't load words.") }
            }
        }
    }
}
