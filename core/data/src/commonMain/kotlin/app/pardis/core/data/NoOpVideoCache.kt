package app.pardis.core.data

import app.pardis.core.domain.VideoCache

/**
 * Safe no-op for platforms or builds where full VideoCache is not (yet) provided via DI.
 * Allows the reader to continue working (remote only) without crashing.
 * Real impls override in platformModules (Android + iOS).
 */
class NoOpVideoCache : VideoCache {
    override suspend fun getLocalVideoPath(slug: String, lang: String): String? = null
    override suspend fun downloadVideoIfNeeded(slug: String, lang: String, remoteUrl: String): String? = null
    override suspend fun clearVideoCache(slug: String) { /* no-op */ }
}
