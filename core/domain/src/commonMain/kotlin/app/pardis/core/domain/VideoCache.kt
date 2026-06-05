package app.pardis.core.domain

/**
 * Cross-platform cache for large media assets (primarily MP4 video for offline playback).
 * Deterministic local file paths so players can use file:// or absolute paths when cached.
 * Impl provided per-platform (needs FS + context on Android).
 *
 * Used to make the (now polished) video player work fully offline for stories that have videoReady.
 * Follows the same optional/cache-fallback spirit as the basic story DB cache.
 */
interface VideoCache {
    /**
     * Returns a local file path (absolute, suitable for platform player Uri/File) if the video
     * for this slug+lang has been successfully downloaded before. Null if not cached.
     * lang: "fa" or "en"
     */
    suspend fun getLocalVideoPath(slug: String, lang: String): String?

    /**
     * Download the remote MP4 (if not already cached) to a stable local location and return the
     * local path. Returns existing local path if already present (no re-download).
     * Returns null on failure (no net, write error, bad url, etc.).
     */
    suspend fun downloadVideoIfNeeded(slug: String, lang: String, remoteUrl: String): String?

    /** Optional: clear cached video for a slug (both langs) for testing or management. */
    suspend fun clearVideoCache(slug: String)
}
