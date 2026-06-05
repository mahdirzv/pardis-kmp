package app.pardis.core.domain

/**
 * Outcome of a story-assets download. Lets callers distinguish full success, partial success
 * (e.g. the video failed but illustrations/narration cached) and total failure, instead of a
 * single nullable path where any video failure looked like everything failing.
 */
data class StoryAssetsResult(
    /** The story had a video URL we attempted to cache. */
    val hadVideo: Boolean,
    /** The video file is present locally after this run. */
    val videoCached: Boolean,
    /** Total asset jobs attempted (video + cover + per-page illustrations + narrations). */
    val total: Int,
    /** How many of those jobs produced a usable local file. */
    val succeeded: Int,
) {
    val failed: Int get() = total - succeeded
    val anyCached: Boolean get() = succeeded > 0
    /** Usable for entering offline video mode: the headline video cached, or (no video) any asset cached. */
    val isUsable: Boolean get() = if (hadVideo) videoCached else anyCached
}

/**
 * Use case to download video + page-level assets (illustrations + narration audio)
 * for offline use of a story. Called when user taps "Cache for offline" for video stories.
 */
interface DownloadStoryAssetsUseCase {
    /**
     * @param onProgress callback for status updates like "Downloaded 3/12 assets..."
     * @return a [StoryAssetsResult] describing how many assets cached and whether the video is offline-ready.
     */
    suspend operator fun invoke(slug: String, onProgress: (String) -> Unit = {}): StoryAssetsResult
}
