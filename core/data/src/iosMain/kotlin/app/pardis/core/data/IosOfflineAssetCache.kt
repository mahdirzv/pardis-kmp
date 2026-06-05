package app.pardis.core.data

import app.pardis.core.domain.OfflineAssetCache
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
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
 * Android counterpart has full Foundation version.
 */
@OptIn(ExperimentalForeignApi::class)
class IosOfflineAssetCache : OfflineAssetCache {

    private val http = HttpClient()

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
                if (bytes.isEmpty()) return@withContext null
                if (writeBytesToFile(path, bytes)) path else null
            } catch (_: Throwable) {
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
