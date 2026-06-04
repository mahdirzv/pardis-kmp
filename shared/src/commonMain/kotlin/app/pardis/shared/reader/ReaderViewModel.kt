package app.pardis.shared.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pardis.core.domain.GetStoryPagesUseCase // assume added
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReaderViewModel(
    // private val getPages: GetStoryPagesUseCase
) : ViewModel() {

    private val currentPage = MutableStateFlow(0)
    private val isVideo = MutableStateFlow(false)

    // For scaffold, use sample pages from model or empty
    val uiState: StateFlow<ReaderUiState> = combine(
        currentPage,
        isVideo,
    ) { page, video ->
        ReaderUiState(
            currentPage = page,
            isVideoMode = video,
            // pages = ... from use case
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ReaderUiState(isLoading = true)
    )

    fun onAction(action: ReaderAction) {
        when (action) {
            is ReaderAction.NextPage -> currentPage.value = (currentPage.value + 1).coerceAtMost(20)
            is ReaderAction.PrevPage -> currentPage.value = (currentPage.value - 1).coerceAtLeast(0)
            is ReaderAction.GoToPage -> currentPage.value = action.page
            is ReaderAction.ToggleVideo -> isVideo.value = !isVideo.value
            is ReaderAction.PlayNarration -> { /* native player in shell */ }
        }
    }
}