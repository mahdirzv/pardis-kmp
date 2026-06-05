package app.pardis.core.data

import app.pardis.core.domain.OfflineAssetCache

/**
 * iOS (KMP) implementation of OfflineAssetCache.
 * 
 * NOTE: Full file I/O + download interop is stubbed for build stability (NSData/NSMutableData interop issues in current K/N bindings).
 * The real implementation needs careful usePinned + NSData.dataWithBytes or NSMutableData.appendBytes (see Android counterpart).
 * When enabled from Swift via iosOfflineAssetCacheModule in SharedInit, it will act as no-op (remote URLs only)
 * until the interop is completed. Android side is fully functional.
 */
class IosOfflineAssetCache : OfflineAssetCache {
    override suspend fun getLocalAssetPath(slug: String, kind: String, subKey: String): String? = null
    override suspend fun downloadAssetIfNeeded(slug: String, kind: String, subKey: String, remoteUrl: String): String? = null
    override suspend fun clearAssetsForStory(slug: String) {}
}
