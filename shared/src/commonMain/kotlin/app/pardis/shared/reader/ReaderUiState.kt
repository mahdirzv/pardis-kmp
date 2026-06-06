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
    // Local file paths (absolute, file:// friendly) when the MP4 has been cached for offline.
    // Shells should prefer local* over the remote videoUrl* for the native player.
    val localVideoUrlFa: String? = null,
    val localVideoUrlEn: String? = null,
    val isDownloadingVideo: Boolean = false,
    val downloadProgress: String? = null, // e.g. "Downloaded 3/12 assets..."
    // Local asset paths for page content (keyed by page number for illustration, "fa-3"/"en-3" for narration)
    val localIllustrationUrls: Map<Int, String> = emptyMap(),
    val localNarrationUrls: Map<String, String> = emptyMap(), // e.g. "fa-0" -> local path
    val preferredNarrationLang: String = "fa", // "fa" or "en" for per-page audio play
    val playbackRate: Float = 1.0f, // 0.5x to 2.0x for narration audio
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
    data class DownloadVideo(val lang: String = "fa") : ReaderAction // "fa" or "en"; starts download of the MP4 for offline
    data class SetNarrationLang(val lang: String) : ReaderAction // "fa" or "en"
    data class SetPlaybackRate(val rate: Float) : ReaderAction
    data object ClearAssets : ReaderAction
    data object PlayNarration : ReaderAction // per page or current
    data class ShowVocab(val vocab: VocabItem) : ReaderAction
    data object DismissVocab : ReaderAction
    data object ErrorDismissed : ReaderAction
}