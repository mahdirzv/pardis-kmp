package app.pardis.core.data

import app.pardis.core.domain.VideoCache
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToFile

/**
 * iOS (KMP) implementation of VideoCache for offline MP4 video.
 * 
 * NOTE: Full file I/O + download interop is stubbed for build stability.
 * The real implementation needs careful NSData / usePinned work (see Android counterpart for reference).
 * When enabled from Swift via iosVideoCacheModule, it will act as no-op (remote URLs only)
 * until the interop is completed.
 */
class IosVideoCache : VideoCache {
    override suspend fun getLocalVideoPath(slug: String, lang: String): String? = null
    override suspend fun downloadVideoIfNeeded(slug: String, lang: String, remoteUrl: String): String? = null
    override suspend fun clearVideoCache(slug: String) {}
}
