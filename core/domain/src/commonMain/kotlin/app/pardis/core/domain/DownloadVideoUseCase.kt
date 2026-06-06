package app.pardis.core.domain

/**
 * Use case to explicitly download (or ensure cached) the MP4 video for a story.
 * Call from VM on user action (e.g. "Download for offline" in reader for video stories).
 * Returns the local path on success, null on failure.
 */
interface DownloadVideoUseCase {
    suspend operator fun invoke(slug: String, lang: String, remoteUrl: String): String?
}
