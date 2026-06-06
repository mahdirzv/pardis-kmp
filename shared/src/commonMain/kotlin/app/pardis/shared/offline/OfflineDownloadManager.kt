package app.pardis.shared.offline

import app.pardis.core.domain.ClearStoryAssetsUseCase
import app.pardis.core.domain.DownloadStoryAssetsUseCase
import app.pardis.core.domain.GetCachedSizeUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Single source of truth for per-story offline download state, shared across screens.
 * Owns its own scope on the main dispatcher (state/job-map mutations stay single-threaded;
 * the heavy IO happens inside the cache impls via withContext(IO)). The scope outliving any
 * ViewModel is the seam for future background downloads.
 */
class OfflineDownloadManager(
    private val downloadStoryAssets: DownloadStoryAssetsUseCase,
    private val clearStoryAssets: ClearStoryAssetsUseCase,
    private val getCachedSize: GetCachedSizeUseCase,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    private val _states = MutableStateFlow<Map<String, StoryDownloadState>>(emptyMap())
    val states: StateFlow<Map<String, StoryDownloadState>> = _states.asStateFlow()

    private val jobs = mutableMapOf<String, Job>()

    private fun setState(slug: String, state: StoryDownloadState) {
        _states.update { it + (slug to state) }
    }

    fun download(slug: String) {
        if (jobs[slug]?.isActive == true) return
        setState(slug, StoryDownloadState.Downloading("Starting download..."))
        jobs[slug] = scope.launch {
            try {
                val result = downloadStoryAssets(slug) { progress ->
                    setState(slug, StoryDownloadState.Downloading(progress))
                }
                if (result.anyCached) {
                    setState(slug, StoryDownloadState.Downloaded(getCachedSize(slug)))
                } else {
                    setState(slug, StoryDownloadState.Failed("Download failed (check connection)"))
                }
            } catch (e: CancellationException) {
                throw e // never swallow cancellation
            } catch (e: Exception) {
                setState(slug, StoryDownloadState.Failed(e.message ?: "Download failed"))
            } finally {
                jobs.remove(slug)
            }
        }
    }

    fun cancel(slug: String) {
        jobs.remove(slug)?.cancel()
        scope.launch {
            clearStoryAssets(slug)
            setState(slug, StoryDownloadState.NotDownloaded)
        }
    }

    fun remove(slug: String) {
        scope.launch {
            clearStoryAssets(slug)
            setState(slug, StoryDownloadState.NotDownloaded)
        }
    }

    /** Reflect on-disk reality (incl. reader-initiated caches). Skips in-flight downloads. */
    suspend fun refreshState(slugs: List<String>) {
        for (slug in slugs) {
            if (jobs[slug]?.isActive == true) continue
            val size = getCachedSize(slug)
            setState(
                slug,
                if (size > 0L) StoryDownloadState.Downloaded(size) else StoryDownloadState.NotDownloaded,
            )
        }
    }
}
