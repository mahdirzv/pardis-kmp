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

    override suspend fun invoke(slug: String): String? {
        // Ensure we have story metadata (for video urls)
        val story = getStory(slug) ?: return null
        val pages = getPages(slug)
        if (pages.isEmpty()) return null

        return coroutineScope {
            val jobs = mutableListOf<kotlinx.coroutines.Deferred<String?>>()

            // Prefer fa video
            val videoUrl = story.videoUrlFa ?: story.videoUrlEn
            if (videoUrl != null) {
                val lang = if (story.videoUrlFa != null) "fa" else "en"
                jobs += async { assetCache.downloadAssetIfNeeded(slug, "video", lang, videoUrl) }
            }

            pages.forEach { page ->
                // Illustration
                page.illustrationUrl?.let { url ->
                    jobs += async { assetCache.downloadAssetIfNeeded(slug, "illustration", page.page.toString(), url) }
                }
                // Narration fa
                page.narrationFa?.url?.let { url ->
                    jobs += async { assetCache.downloadAssetIfNeeded(slug, "narration", "fa-${page.page}", url) }
                }
                // Narration en
                page.narrationEn?.url?.let { url ->
                    jobs += async { assetCache.downloadAssetIfNeeded(slug, "narration", "en-${page.page}", url) }
                }
            }

            // Wait for all (best effort)
            jobs.awaitAll()

            // Return the video local path if we have one (for caller)
            assetCache.getLocalAssetPath(slug, "video", if (story.videoUrlFa != null) "fa" else "en")
        }
    }
}