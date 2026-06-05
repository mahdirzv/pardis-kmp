package app.pardis.shared.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pardis.core.domain.DownloadVideoUseCase
import app.pardis.core.domain.GetLocalVideoPathUseCase
import app.pardis.core.domain.GetProgressUseCase
import app.pardis.core.domain.GetStoryPagesUseCase
import app.pardis.core.domain.GetStoryUseCase
import app.pardis.core.domain.SaveProgressUseCase
import app.pardis.core.model.StoryPage
import app.pardis.shared.analytics.Analytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReaderViewModel(
    private val getStoryPages: GetStoryPagesUseCase,
    private val getStory: GetStoryUseCase,
    private val saveProgress: SaveProgressUseCase,
    private val getProgress: GetProgressUseCase,
    private val analytics: Analytics,
    private val getLocalVideoPath: GetLocalVideoPathUseCase,
    private val downloadVideo: DownloadVideoUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState(isLoading = true))
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    fun onAction(action: ReaderAction) {
        when (action) {
            is ReaderAction.LoadStory -> loadStory(action.slug)
            is ReaderAction.NextPage -> {
                _uiState.update { current ->
                    val max = (current.pages.size - 1).coerceAtLeast(0)
                    val newPage = (current.currentPage + 1).coerceAtMost(max)
                    analytics.track("page_changed", mapOf("slug" to current.storySlug, "page" to newPage))
                    viewModelScope.launch { saveProgress(current.storySlug, newPage) }
                    current.copy(currentPage = newPage)
                }
            }
            is ReaderAction.PrevPage -> _uiState.update { current ->
                val newPage = (current.currentPage - 1).coerceAtLeast(0)
                analytics.track("page_changed", mapOf("slug" to current.storySlug, "page" to newPage))
                viewModelScope.launch { saveProgress(current.storySlug, newPage) }
                current.copy(currentPage = newPage)
            }
            is ReaderAction.GoToPage -> _uiState.update { current ->
                val newPage = action.page.coerceIn(0, (current.pages.size - 1).coerceAtLeast(0))
                viewModelScope.launch { saveProgress(current.storySlug, newPage) }
                current.copy(currentPage = newPage)
            }
            is ReaderAction.ToggleVideo -> _uiState.update { it.copy(isVideoMode = !it.isVideoMode) }
            is ReaderAction.DownloadVideo -> downloadVideoForCurrent(action.lang)
            is ReaderAction.PlayNarration -> {
                // Native shell handles actual playback using current page's narration urls
            }
            is ReaderAction.ShowVocab -> {
                analytics.track("vocab_opened", mapOf("fa" to action.vocab.fa))
                _uiState.update { it.copy(selectedVocab = action.vocab) }
            }
            is ReaderAction.DismissVocab -> _uiState.update { it.copy(selectedVocab = null) }
            is ReaderAction.ErrorDismissed -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadStory(slug: String) {
        val current = _uiState.value
        if (current.storySlug == slug && current.pages.isNotEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(storySlug = slug, isLoading = true, errorMessage = null, currentPage = 0) }
            try {
                val story = getStory(slug)
                val pagesResult = getStoryPages(slug)
                val introDur = ((story?.introAudio?.fa?.durationSeconds ?: 0.0) + (story?.introAudio?.en?.durationSeconds ?: 0.0)) / 2.0
                val outroDur = ((story?.outroAudio?.fa?.durationSeconds ?: 0.0) + (story?.outroAudio?.en?.durationSeconds ?: 0.0)) / 2.0

                // Build simple cues (intro -> pages -> outro), using avg fa/en duration for demo
                val cues = mutableListOf<SubtitleCue>()
                var time = 0.0
                // Intro cue (page 0 special or -1, use 0 for simplicity)
                if (introDur > 0) {
                    cues.add(SubtitleCue(pageIndex = 0, startSec = time, endSec = time + introDur))
                    time += introDur
                }
                pagesResult.forEachIndexed { idx, p ->
                    val dur = ((p.narrationFa?.durationSeconds ?: 0.0) + (p.narrationEn?.durationSeconds ?: 0.0)) / 2.0
                    if (dur > 0) {
                        cues.add(SubtitleCue(pageIndex = idx, startSec = time, endSec = time + dur))
                        time += dur
                    }
                }
                if (outroDur > 0) {
                    cues.add(SubtitleCue(pageIndex = pagesResult.lastIndex, startSec = time, endSec = time + outroDur))
                }

                // Restore last read page if we have saved progress
                val savedPage = getProgress(slug) ?: 0
                val startPage = savedPage.coerceIn(0, (pagesResult.lastIndex).coerceAtLeast(0))

                // Resolve any previously downloaded local video files (enables offline video playback)
                val localFa = story?.videoUrlFa?.let { getLocalVideoPath(slug, "fa") }
                val localEn = story?.videoUrlEn?.let { getLocalVideoPath(slug, "en") }

                _uiState.update {
                    it.copy(
                        pages = pagesResult,
                        videoUrlFa = story?.videoUrlFa,
                        videoUrlEn = story?.videoUrlEn,
                        localVideoUrlFa = localFa,
                        localVideoUrlEn = localEn,
                        introDuration = introDur,
                        outroDuration = outroDur,
                        cues = cues,
                        currentPage = startPage,
                        isLoading = false,
                        isDownloadingVideo = false
                    )
                }
                analytics.track("story_loaded", mapOf("slug" to slug, "pages" to pagesResult.size))
            } catch (t: Throwable) {
                _uiState.update { it.copy(pages = emptyList(), isLoading = false, errorMessage = t.message ?: "Failed to load story pages", isDownloadingVideo = false) }
            }
        }
    }

    private fun downloadVideoForCurrent(lang: String) {
        val current = _uiState.value
        val slug = current.storySlug
        if (slug.isEmpty()) return
        val remote = if (lang == "fa") current.videoUrlFa else current.videoUrlEn
        if (remote.isNullOrBlank()) return

        // Already have local? no-op
        val existing = if (lang == "fa") current.localVideoUrlFa else current.localVideoUrlEn
        if (!existing.isNullOrBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDownloadingVideo = true, errorMessage = null) }
            val local = downloadVideo(slug, lang, remote)
            if (local != null) {
                _uiState.update {
                    if (lang == "fa") it.copy(localVideoUrlFa = local, isDownloadingVideo = false)
                    else it.copy(localVideoUrlEn = local, isDownloadingVideo = false)
                }
                analytics.track("video_downloaded", mapOf("slug" to slug, "lang" to lang))
            } else {
                _uiState.update { it.copy(isDownloadingVideo = false, errorMessage = "Video download failed (check connection)") }
            }
        }
    }
}