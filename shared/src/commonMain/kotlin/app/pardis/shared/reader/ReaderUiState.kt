package app.pardis.shared.reader

import app.pardis.core.model.StoryPage
import app.pardis.core.model.VocabItem

data class ReaderUiState(
    val storySlug: String = "",
    val pages: List<StoryPage> = emptyList(),
    val currentPage: Int = 0,
    val isVideoMode: Boolean = false,
    val videoUrlFa: String? = null,
    val videoUrlEn: String? = null,
    val introDuration: Double = 0.0,
    val outroDuration: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val cues: List<SubtitleCue> = emptyList(),
    val selectedVocab: VocabItem? = null,
)

data class SubtitleCue(
    val pageIndex: Int,
    val startSec: Double,
    val endSec: Double,
)

sealed interface ReaderAction {
    data class LoadStory(val slug: String) : ReaderAction
    data object NextPage : ReaderAction
    data object PrevPage : ReaderAction
    data class GoToPage(val page: Int) : ReaderAction
    data object ToggleVideo : ReaderAction
    data object PlayNarration : ReaderAction // per page or current
    data class ShowVocab(val vocab: VocabItem) : ReaderAction
    data object DismissVocab : ReaderAction
    data object ErrorDismissed : ReaderAction
}