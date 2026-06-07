package app.pardis.shared.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pardis.core.domain.GetProgressUseCase
import app.pardis.core.domain.GetStoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Loads a single story's metadata (+ saved reading progress) for the detail screen.
 * Navigation to the reader stays platform-owned via a callback; this VM only owns state.
 */
class StoryDetailViewModel(
    private val getStory: GetStoryUseCase,
    private val getProgress: GetProgressUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoryDetailUiState())
    val uiState: StateFlow<StoryDetailUiState> = _uiState.asStateFlow()

    fun onAction(action: StoryDetailAction) {
        when (action) {
            is StoryDetailAction.LoadStory -> loadStory(action.slug)
            is StoryDetailAction.Retry -> loadStory(_uiState.value.slug)
            is StoryDetailAction.ErrorDismissed -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadStory(slug: String) {
        if (slug.isEmpty()) return
        val current = _uiState.value
        if (current.slug == slug && current.story != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(slug = slug, isLoading = true, errorMessage = null) }
            try {
                val story = getStory(slug)
                if (story == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Story not found.") }
                    return@launch
                }
                val savedPage = getProgress(slug) ?: 0
                _uiState.update {
                    it.copy(story = story, savedPage = savedPage, isLoading = false, errorMessage = null)
                }
            } catch (e: Throwable) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Couldn't load this story.") }
            }
        }
    }
}
