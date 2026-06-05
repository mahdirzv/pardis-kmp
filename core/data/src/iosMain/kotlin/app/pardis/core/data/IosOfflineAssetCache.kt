package app.pardis.core.data

import app.pardis.core.domain.OfflineAssetCache

/**
 * iOS (KMP) implementation of OfflineAssetCache.
 * 
 * NOTE: Full file I/O + download interop is stubbed for build stability.
 * The real implementation needs careful NSData / usePinned work (see Android counterpart for reference).
 * When enabled from Swift via iosVideoCacheModule, it will act as no-op (remote URLs only)
 * until the interop is completed.
 */
class IosOfflineAssetCache : OfflineAssetCache {
    override suspend fun getLocalAssetPath(slug: String, kind: String, subKey: String): String? = null
    override suspend fun downloadAssetIfNeeded(slug: String, kind: String, subKey: String, remoteUrl: String): String? = null
    override suspend fun clearAssetsForStory(slug: String) {}
}
