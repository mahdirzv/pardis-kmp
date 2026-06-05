package app.pardis.core.domain

/**
 * Cross-platform cache for story assets (video MP4, illustrations, narration audio) for offline playback.
 * Deterministic local file paths so players and image loaders can use file:// or absolute paths when cached.
 * Impl provided per-platform (needs FS + context on Android).
 *
 * Used to make the video player (and full reader) work fully offline for stories that have videoReady
 * or when user explicitly caches assets. Follows the same optional/cache-fallback spirit as the basic story DB cache.
 *
 * Kinds supported:
 * - "video" with subKey "fa" or "en"
 * - "illustration" with subKey = page number as string (e.g. "0", "5")
 * - "narration" with subKey = "fa-3" or "en-3" (lang-page)
 */
interface OfflineAssetCache {
    /**
     * Returns a local file path (absolute, suitable for platform player/ loader) if the asset
     * for this slug+kind+subKey has been successfully downloaded before. Null if not cached.
     */
    suspend fun getLocalAssetPath(slug: String, kind: String, subKey: String = ""): String?

    /**
     * Download the remote asset (if not already cached) to a stable local location and return the
     * local path. Returns existing local path if already present (no re-download).
     * Returns null on failure (no net, write error, bad url, etc.).
     */
    suspend fun downloadAssetIfNeeded(slug: String, kind: String, subKey: String, remoteUrl: String): String?

    /** Optional: clear cached assets for a slug for testing or management. */
    suspend fun clearAssetsForStory(slug: String)
}
