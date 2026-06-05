package app.pardis.shared.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pardis.core.domain.ClearStoryAssetsUseCase
import app.pardis.core.domain.DownloadStoryAssetsUseCase
import app.pardis.core.domain.DownloadVideoUseCase
import app.pardis.core.domain.GetLocalAssetPathUseCase
import app.pardis.core.domain.GetLocalVideoPathUseCase
import app.pardis.core.domain.GetProgressUseCase
import app.pardis.core.domain.GetStoryPagesUseCase
import app.pardis.core.domain.GetStoryUseCase
import app.pardis.core.domain.SaveProgressUseCase
import app.pardis.core.model.StoryPage
import app.pardis.shared.analytics.Analytics
import kotlinx.coroutines.delay
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
    private val downloadStoryAssets: DownloadStoryAssetsUseCase,
    private val getLocalAssetPath: GetLocalAssetPathUseCase,
    private val clearAssets: ClearStoryAssetsUseCase,
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
            is ReaderAction.ToggleVideo -> {
                val current = _uiState.value
                val nowVideo = !current.isVideoMode
                _uiState.update { it.copy(isVideoMode = nowVideo) }
                if (nowVideo) {
                    val hasVideo = current.videoUrlFa != null || current.videoUrlEn != null
                    val hasLocal = current.localVideoUrlFa != null || current.localVideoUrlEn != null
                    if (hasVideo && !hasLocal) {
                        // Auto cache on entering video if not yet (for videoReady stories) -- silent on fail (e.g. no connection)
                        viewModelScope.launch {
                            try {
                                val localVideo = downloadStoryAssets(current.storySlug) { /* silent progress for auto */ }
                                if (localVideo != null) {
                                    updateLocalsAfterSuccessfulDownload(current.storySlug)
                                    _uiState.update { it.copy(downloadProgress = "Download complete!") }
                                    delay(1200)
                                    _uiState.update { it.copy(downloadProgress = null) }
                                    analytics.track("story_assets_downloaded_auto", mapOf("slug" to current.storySlug))
                                }
                                // silent for auto; do not set errorMessage
                            } catch (e: Exception) {
                                // silent for auto
                            }
                        }
                    }
                }
            }
            is ReaderAction.DownloadVideo -> downloadVideoForCurrent(action.lang, showErrorOnFail = true)
            is ReaderAction.SetNarrationLang -> _uiState.update { it.copy(preferredNarrationLang = action.lang) }
            is ReaderAction.SetPlaybackRate -> _uiState.update { it.copy(playbackRate = action.rate.coerceIn(0.5f, 2.0f)) }
            is ReaderAction.ClearAssets -> {
                viewModelScope.launch {
                    clearAssets(_uiState.value.storySlug)
                    // Reset locals
                    _uiState.update {
                        it.copy(
                            localVideoUrlFa = null,
                            localVideoUrlEn = null,
                            localIllustrationUrls = emptyMap(),
                            localNarrationUrls = emptyMap()
                        )
                    }
                }
            }
            is ReaderAction.PlayNarration -> {
                // Native shell handles actual playback using current page's narration urls (prefers preferredNarrationLang + local) and rate
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

                // Resolve any previously downloaded local video files (enables offline video playback). Uses the asset cache under the hood.
                val localFa = story?.videoUrlFa?.let { getLocalVideoPath(slug, "fa") }
                val localEn = story?.videoUrlEn?.let { getLocalVideoPath(slug, "en") }

                // Resolve local page assets (illustrations + narration) if cached
                val (localIllos, localNars) = resolveLocalPageAssets(slug, pagesResult)

                _uiState.update {
                    it.copy(
                        pages = pagesResult,
                        videoUrlFa = story?.videoUrlFa,
                        videoUrlEn = story?.videoUrlEn,
                        localVideoUrlFa = localFa,
                        localVideoUrlEn = localEn,
                        localIllustrationUrls = localIllos,
                        localNarrationUrls = localNars,
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

    private fun downloadVideoForCurrent(lang: String, showErrorOnFail: Boolean = true) {
        val current = _uiState.value
        val slug = current.storySlug
        if (slug.isEmpty()) return

        // Use the full assets downloader (video + illustrations + narration audio for pages)
        // This makes offline video actually complete (captions have images, audio fallback works)
        viewModelScope.launch {
            _uiState.update { it.copy(isDownloadingVideo = true, errorMessage = null, downloadProgress = "Starting download...") }
            try {
                val localVideo = downloadStoryAssets(slug) { progress ->
                    _uiState.update { it.copy(downloadProgress = progress) }
                }
                if (localVideo != null) {
                    updateLocalsAfterSuccessfulDownload(slug)
                    _uiState.update { it.copy(isDownloadingVideo = false, downloadProgress = "Download complete!") }
                    delay(1200)
                    _uiState.update { it.copy(downloadProgress = null) }
                    analytics.track("story_assets_downloaded", mapOf("slug" to slug))
                } else if (showErrorOnFail) {
                    // Report via progress area (non-fatal to reader UI, avoids replacing content + player release)
                    _uiState.update { it.copy(isDownloadingVideo = false, downloadProgress = "Download failed (check connection)") }
                    delay(2200)
                    _uiState.update { it.copy(downloadProgress = null) }
                }
            } catch (e: Exception) {
                if (showErrorOnFail) {
                    _uiState.update { it.copy(isDownloadingVideo = false, downloadProgress = "Download failed (check connection)") }
                    delay(2200)
                    _uiState.update { it.copy(downloadProgress = null) }
                }
            }
        }
    }

    private suspend fun resolveLocalPageAssets(slug: String, pages: List<StoryPage>): Pair<Map<Int, String>, Map<String, String>> {
        val illos = mutableMapOf<Int, String>()
        val nars = mutableMapOf<String, String>()

        pages.forEach { p ->
            // Illustration
            p.illustrationUrl?.let {
                getLocalAssetPath(slug, "illustration", p.page.toString())?.let { local ->
                    illos[p.page] = local
                }
            }
            // Narration fa
            p.narrationFa?.url?.let {
                getLocalAssetPath(slug, "narration", "fa-${p.page}")?.let { local ->
                    nars["fa-${p.page}"] = local
                }
            }
            // Narration en
            p.narrationEn?.url?.let {
                getLocalAssetPath(slug, "narration", "en-${p.page}")?.let { local ->
                    nars["en-${p.page}"] = local
                }
            }
        }
        return illos to nars
    }

    private suspend fun updateLocalsAfterSuccessfulDownload(slug: String) {
        val localFa = getLocalVideoPath(slug, "fa")
        val localEn = getLocalVideoPath(slug, "en")
        val pages = _uiState.value.pages
        val (localIllos, localNars) = resolveLocalPageAssets(slug, pages)
        _uiState.update {
            it.copy(
                localVideoUrlFa = localFa,
                localVideoUrlEn = localEn,
                localIllustrationUrls = localIllos,
                localNarrationUrls = localNars
            )
        }
    }
}