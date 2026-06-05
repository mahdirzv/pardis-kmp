package app.pardis.core.domain

/**
 * Use case to download video + page-level assets (illustrations + narration audio)
 * for offline use of a story. Called when user taps "Cache for offline" for video stories.
 */
interface DownloadStoryAssetsUseCase {
    /**
     * @return summary or local video path on success, null on critical failure.
     */
    suspend operator fun invoke(slug: String): String?
}