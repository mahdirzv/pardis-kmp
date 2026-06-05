package app.pardis.core.data

import app.pardis.core.domain.DownloadStoryAssetsUseCase
import app.pardis.core.domain.GetStoryPagesUseCase
import app.pardis.core.domain.GetStoryUseCase
import app.pardis.core.domain.OfflineAssetCache
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class DownloadStoryAssetsUseCaseImpl(
    private val assetCache: OfflineAssetCache,
    private val getStory: GetStoryUseCase,
    private val getPages: GetStoryPagesUseCase
) : DownloadStoryAssetsUseCase {

    override suspend fun invoke(slug: String, onProgress: (String) -> Unit): String? {
        // Ensure we have story metadata (for video urls)
        val story = getStory(slug) ?: return null
        val pages = getPages(slug)
        if (pages.isEmpty()) return null

        return coroutineScope {
            // Pre-count total assets for progress
            var total = 0
            val videoUrl = story.videoUrlFa ?: story.videoUrlEn
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

            val jobs = mutableListOf<kotlinx.coroutines.Deferred<String?>>()

            // Prefer fa video
            if (videoUrl != null) {
                val lang = if (story.videoUrlFa != null) "fa" else "en"
                jobs += async {
                    val res = assetCache.downloadAssetIfNeeded(slug, "video", lang, videoUrl)
                    done++
                    report()
                    res
                }
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

            // Wait for all (best effort)
            jobs.awaitAll()

            // Final report
            onProgress("Download complete ($done/$total)")

            // Return the video local path if we have one (for caller)
            assetCache.getLocalAssetPath(slug, "video", if (story.videoUrlFa != null) "fa" else "en")
        }
    }
}