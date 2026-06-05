package app.pardis.core.data

import app.pardis.core.domain.DownloadStoryAssetsUseCase
import app.pardis.core.domain.GetStoryPagesUseCase
import app.pardis.core.domain.GetStoryUseCase
import app.pardis.core.domain.OfflineAssetCache
import app.pardis.core.domain.StoryAssetsResult
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class DownloadStoryAssetsUseCaseImpl(
    private val assetCache: OfflineAssetCache,
    private val getStory: GetStoryUseCase,
    private val getPages: GetStoryPagesUseCase
) : DownloadStoryAssetsUseCase {

    override suspend fun invoke(slug: String, onProgress: (String) -> Unit): StoryAssetsResult {
        // Ensure we have story metadata (for video urls)
        val story = getStory(slug)
            ?: return StoryAssetsResult(hadVideo = false, videoCached = false, total = 0, succeeded = 0)
        val pages = getPages(slug)
        // Do not early-return on empty pages. We still want to allow caching the video (+ cover)
        // even if the per-page cached_pages row is missing for some reason. The loops below will
        // simply add 0 page-asset jobs. This makes "Cache video + assets" more robust.

        return coroutineScope {
            val videoUrl = story.videoUrlFa ?: story.videoUrlEn
            val hadVideo = videoUrl != null

            // Pre-count total assets for progress
            var total = 0
            if (videoUrl != null) total += 1
            if (story.coverUrl != null) total += 1
            pages.forEach { p ->
                if (p.illustrationUrl != null) total += 1
                if (p.narrationFa?.url != null) total += 1
                if (p.narrationEn?.url != null) total += 1
            }

            var done = 0
            fun report() {
                onProgress("Downloaded $done/$total assets...")
            }

            val jobs = mutableListOf<Deferred<String?>>()
            var videoJob: Deferred<String?>? = null

            // Prefer fa video
            if (videoUrl != null) {
                val lang = if (story.videoUrlFa != null) "fa" else "en"
                val job = async {
                    val res = assetCache.downloadAssetIfNeeded(slug, "video", lang, videoUrl)
                    done++
                    report()
                    res
                }
                videoJob = job
                jobs += job
            }

            // Also cache the cover for offline library cards
            story.coverUrl?.let { url ->
                jobs += async {
                    val res = assetCache.downloadAssetIfNeeded(slug, "cover", "", url)
                    done++
                    report()
                    res
                }
            }

            pages.forEach { page ->
                // Illustration
                page.illustrationUrl?.let { url ->
                    jobs += async {
                        val res = assetCache.downloadAssetIfNeeded(slug, "illustration", page.page.toString(), url)
                        done++
                        report()
                        res
                    }
                }
                // Narration fa
                page.narrationFa?.url?.let { url ->
                    jobs += async {
                        val res = assetCache.downloadAssetIfNeeded(slug, "narration", "fa-${page.page}", url)
                        done++
                        report()
                        res
                    }
                }
                // Narration en
                page.narrationEn?.url?.let { url ->
                    jobs += async {
                        val res = assetCache.downloadAssetIfNeeded(slug, "narration", "en-${page.page}", url)
                        done++
                        report()
                        res
                    }
                }
            }

            // Initial report
            report()

            // Wait for all (best effort — individual asset failures return null rather than throwing)
            val results = jobs.awaitAll()
            val succeeded = results.count { it != null }
            val videoCached = videoJob?.await() != null

            StoryAssetsResult(
                hadVideo = hadVideo,
                videoCached = videoCached,
                total = total,
                succeeded = succeeded,
            )
        }
    }
}
