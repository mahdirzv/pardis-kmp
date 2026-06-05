package app.pardis.shared.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pardis.core.domain.GetStoryPagesUseCase
import app.pardis.core.model.StoryPage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReaderViewModel(
    private val getStoryPages: GetStoryPagesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState(isLoading = true))
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    fun onAction(action: ReaderAction) {
        when (action) {
            is ReaderAction.LoadStory -> loadStory(action.slug)
            is ReaderAction.NextPage -> {
                _uiState.update { current ->
                    val max = (current.pages.size - 1).coerceAtLeast(0)
                    current.copy(currentPage = (current.currentPage + 1).coerceAtMost(max))
                }
            }
            is ReaderAction.PrevPage -> _uiState.update { it.copy(currentPage = (it.currentPage - 1).coerceAtLeast(0)) }
            is ReaderAction.GoToPage -> _uiState.update { current ->
                current.copy(currentPage = action.page.coerceIn(0, (current.pages.size - 1).coerceAtLeast(0)))
            }
            is ReaderAction.ToggleVideo -> _uiState.update { it.copy(isVideoMode = !it.isVideoMode) }
            is ReaderAction.PlayNarration -> {
                // Native shell handles actual playback using current page's narration urls
            }
            is ReaderAction.ErrorDismissed -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadStory(slug: String) {
        val current = _uiState.value
        if (current.storySlug == slug && current.pages.isNotEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(storySlug = slug, isLoading = true, errorMessage = null, currentPage = 0) }
            try {
                val result = getStoryPages(slug)
                _uiState.update { it.copy(pages = result, isLoading = false) }
            } catch (t: Throwable) {
                _uiState.update { it.copy(pages = emptyList(), isLoading = false, errorMessage = t.message ?: "Failed to load story pages") }
            }
        }
    }
}