package app.pardis.shared.finish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pardis.core.domain.GetStoryPagesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Loads the glossary words for a finished story so the celebration can show the
 * "words added to your garden" chips. Navigation stays platform-owned.
 */
class StoryFinishViewModel(
    private val getStoryPages: GetStoryPagesUseCase,
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
                _uiState.update { it.copy(words = words, isLoading = false) }
            } catch (e: Throwable) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Couldn't load words.") }
            }
        }
    }
}
