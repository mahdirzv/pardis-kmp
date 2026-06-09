package app.pardis.shared

import app.pardis.core.data.NoOpOfflineAssetCache
import app.pardis.core.domain.OfflineAssetCache
import kotlin.test.Test
import kotlin.test.assertFailsWith

private class FakeRealCache : OfflineAssetCache {
    override suspend fun getLocalAssetPath(slug: String, kind: String, subKey: String): String? = null
    override suspend fun downloadAssetIfNeeded(slug: String, kind: String, subKey: String, remoteUrl: String): String? = null
    override suspend fun clearAssetsForStory(slug: String) {}
    override suspend fun getCachedSizeBytes(slug: String): Long = 0L
}

class SharedInitTest {

    @Test
    fun throws_whenPlatformModulesPresent_butCacheIsNoOp() {
        assertFailsWith<IllegalStateException> {
            validateOfflineCacheWiring(NoOpOfflineAssetCache(), hasPlatformModules = true)
        }
    }

    @Test
    fun passes_whenPlatformModulesPresent_andRealCache() {
        // Should not throw.
        validateOfflineCacheWiring(FakeRealCache(), hasPlatformModules = true)
    }

    @Test
    fun passes_whenNoPlatformModules_evenWithNoOpDefault() {
        // No platform modules → NoOp default is acceptable (bring-up / test contexts).
        validateOfflineCacheWiring(NoOpOfflineAssetCache(), hasPlatformModules = false)
    }
}
