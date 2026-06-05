package app.pardis.core.domain

/**
 * Use case to download video + page-level assets (illustrations + narration audio)
 * for offline use of a story. Called when user taps "Cache for offline" for video stories.
 */
interface DownloadStoryAssetsUseCase {
    /**
     * @param onProgress callback for status updates like "Downloaded 3/12 assets..."
     * @return local video path on success (or null if no video), null on critical failure.
     */
    suspend operator fun invoke(slug: String, onProgress: (String) -> Unit = {}): String?
}