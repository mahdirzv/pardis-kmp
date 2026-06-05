package app.pardis.shared.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pardis.core.model.Story
import app.pardis.shared.analytics.Analytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val getStoriesUseCase: app.pardis.core.domain.GetStoriesUseCase,
    private val analytics: Analytics,
) : ViewModel() {

    private val stories = MutableStateFlow<List<app.pardis.core.model.Story>>(emptyList())
    private val isLoading = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<LibraryUiState> = combine(
        stories,
        isLoading,
        error,
    ) { currentStories, loading, err ->
        LibraryUiState(
            stories = currentStories,
            isLoading = loading,
            errorMessage = err,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState(isLoading = true),
    )

    init {
        refresh()
    }

    fun onAction(action: LibraryAction) {
        when (action) {
            is LibraryAction.Refresh -> refresh()
            is LibraryAction.OpenStory -> {
                // Handled by native shell callback
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null
            try {
                val result = getStoriesUseCase()
                stories.value = result
                analytics.track("library_loaded", mapOf("count" to result.size))
            } catch (t: Throwable) {
                error.value = t.message ?: "Failed to load stories"
            } finally {
                isLoading.value = false
            }
        }
    }
}