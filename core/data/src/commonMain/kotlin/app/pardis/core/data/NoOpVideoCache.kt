package app.pardis.core.data

import app.pardis.core.domain.OfflineAssetCache

/**
 * Safe no-op for platforms or builds where full OfflineAssetCache is not (yet) provided via DI.
 * Allows the reader to continue working (remote only) without crashing.
 * Real impls override in platformModules (Android + iOS).
 */
class NoOpOfflineAssetCache : OfflineAssetCache {
    override suspend fun getLocalAssetPath(slug: String, kind: String, subKey: String): String? = null
    override suspend fun downloadAssetIfNeeded(slug: String, kind: String, subKey: String, remoteUrl: String): String? = null
    override suspend fun clearAssetsForStory(slug: String) { /* no-op */ }
}
