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
import kotlinx.coroutines.withContext

/**
 * Single source of truth for per-story offline download state, shared across screens.
 *
 * Concurrency: all state and job-map mutations run on the main dispatcher (the scope's dispatcher,
 * and the withContext in refreshState); the heavy IO happens inside the cache impls via
 * withContext(IO). Keeping the jobs map single-threaded avoids races without locks. Call
 * download/cancel/remove from the main (UI) thread.
 *
 * Each running download captures its own Job and only mutates state while it is still the tracked
 * job for its slug (identity check), so a cancel()/remove() (which drops the slug from the map)
 * cannot be overwritten by a late result from the job it replaced.
 *
 * The scope is intentionally never cancelled: this is a Koin single that lives for the whole
 * process, which is also the seam for future background downloads.
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
        var job: Job? = null
        job = scope.launch {
            try {
                val result = downloadStoryAssets(slug) { progress ->
                    if (jobs[slug] === job) setState(slug, StoryDownloadState.Downloading(progress))
                }
                if (jobs[slug] === job) {
                    // anyCached (not isUsable): the library caches for offline READING, so a story
                    // whose images/narration cached but video failed is still "downloaded".
                    if (result.anyCached) {
                        setState(slug, StoryDownloadState.Downloaded(getCachedSize(slug)))
                    } else {
                        setState(slug, StoryDownloadState.Failed("Download failed (check connection)"))
                    }
                }
            } catch (e: CancellationException) {
                throw e // never swallow cancellation
            } catch (e: Exception) {
                if (jobs[slug] === job) setState(slug, StoryDownloadState.Failed(e.message ?: "Download failed"))
            } finally {
                if (jobs[slug] === job) jobs.remove(slug)
            }
        }
        jobs[slug] = job
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
    suspend fun refreshState(slugs: List<String>) = withContext(Dispatchers.Main) {
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
