package app.pardis.core.data

import app.pardis.core.domain.OfflineAssetCache
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.posix.O_CREAT
import platform.posix.O_TRUNC
import platform.posix.O_WRONLY
import platform.posix.close
import platform.posix.open
import platform.posix.write

/**
 * iOS (KMP) implementation of OfflineAssetCache using posix for file write (to avoid NSData interop issues).
 * Stores under caches / pardis/assets/{slug}/{kind}-{subKey}.dat (mp4 for video).
 * Android counterpart streams to disk; here we still buffer the body (iOS devices have generous headroom)
 * but fail loudly on HTTP errors and log the real cause.
 */
@OptIn(ExperimentalForeignApi::class)
class IosOfflineAssetCache : OfflineAssetCache {

    private val http = HttpClient {
        // Fail loudly on HTTP errors (4xx/5xx) so they surface as exceptions we log instead of
        // silently writing a tiny error body.
        expectSuccess = true
        install(HttpTimeout) {
            requestTimeoutMillis = 15 * 60 * 1000L      // large videos
            connectTimeoutMillis = 30 * 1000L
            socketTimeoutMillis = 15 * 60 * 1000L
        }
    }

    private fun baseAssetsDir(): String {
        val caches = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
        val base = (caches.firstOrNull() as? String) ?: "/tmp"
        return "$base/pardis/assets"
    }

    private fun assetsDir(slug: String): String {
        val dir = "${baseAssetsDir()}/$slug"
        NSFileManager.defaultManager.createDirectoryAtPath(dir, withIntermediateDirectories = true, attributes = null, error = null)
        return dir
    }

    private fun assetPath(slug: String, kind: String, subKey: String): String {
        val ext = if (kind == "video") "mp4" else "dat"
        val name = if (subKey.isNotBlank()) "$kind-$subKey.$ext" else "$kind.$ext"
        return "${assetsDir(slug)}/$name"
    }

    override suspend fun getLocalAssetPath(slug: String, kind: String, subKey: String): String? {
        val path = assetPath(slug, kind, subKey)
        return if (NSFileManager.defaultManager.fileExistsAtPath(path)) path else null
    }

    override suspend fun downloadAssetIfNeeded(slug: String, kind: String, subKey: String, remoteUrl: String): String? {
        val path = assetPath(slug, kind, subKey)
        if (getLocalAssetPath(slug, kind, subKey) != null) return path

        return withContext(Dispatchers.IO) {
            try {
                val bytes: ByteArray = http.get(remoteUrl).body()
                // Guard against empty / tiny error bodies being treated as a valid asset.
                if (bytes.size <= 1024) return@withContext null
                if (writeBytesToFile(path, bytes)) path else null
            } catch (t: Throwable) {
                // Log the real cause (timeout, 403/404, SSL, write error). expectSuccess=true turns
                // HTTP errors into exceptions that land here.
                println("IosOfflineAssetCache: downloadAssetIfNeeded FAILED kind=$kind subKey=$subKey url=$remoteUrl error=$t")
                NSFileManager.defaultManager.removeItemAtPath(path, null)
                null
            }
        }
    }

    override suspend fun clearAssetsForStory(slug: String) {
        withContext(Dispatchers.IO) {
            val dir = assetsDir(slug)
            NSFileManager.defaultManager.removeItemAtPath(dir, null)
        }
    }

    override suspend fun getCachedSizeBytes(slug: String): Long = withContext(Dispatchers.IO) {
        val dir = "${baseAssetsDir()}/$slug"
        val fm = NSFileManager.defaultManager
        if (!fm.fileExistsAtPath(dir)) return@withContext 0L
        val names = fm.contentsOfDirectoryAtPath(dir, null) ?: return@withContext 0L
        var total = 0L
        for (name in names) {
            val attrs = fm.attributesOfItemAtPath("$dir/$name", null)
            total += (attrs?.get(NSFileSize) as? NSNumber)?.longLongValue ?: 0L
        }
        total
    }

    private fun writeBytesToFile(path: String, bytes: ByteArray): Boolean {
        return bytes.usePinned { pinned ->
            val fd = open(path, O_WRONLY or O_CREAT or O_TRUNC, 0x1B6)
            if (fd == -1) return@usePinned false
            val written = write(fd, pinned.addressOf(0), bytes.size.toULong())
            close(fd)
            written == bytes.size.toLong()
        }
    }
}
