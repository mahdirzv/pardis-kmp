package app.pardis.core.data

import android.content.Context
import app.pardis.core.domain.OfflineAssetCache
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Android implementation of OfflineAssetCache.
 * Stores under app cacheDir/pardis/assets/{slug}/{kind}-{subKey}.dat (or .mp4 for video)
 * Uses Ktor for download (consistent with SupabaseClient).
 * Registered in platformModules in PardisApplication.
 */
class AndroidOfflineAssetCache(
    private val context: Context
) : OfflineAssetCache {

    private val http = HttpClient {
        // Minimal config; follows existing lenient json but not needed for bytes
    }

    private fun assetsDir(slug: String): File {
        return File(context.cacheDir, "pardis/assets/$slug").apply { mkdirs() }
    }

    private fun assetFile(slug: String, kind: String, subKey: String): File {
        val ext = when (kind) {
            "video" -> "mp4"
            else -> "dat"
        }
        val name = if (subKey.isNotBlank()) "$kind-$subKey.$ext" else "$kind.$ext"
        return File(assetsDir(slug), name)
    }

    override suspend fun getLocalAssetPath(slug: String, kind: String, subKey: String): String? {
        val f = assetFile(slug, kind, subKey)
        return if (f.exists() && f.length() > 1024) f.absolutePath else null
    }

    override suspend fun downloadAssetIfNeeded(slug: String, kind: String, subKey: String, remoteUrl: String): String? {
        val f = assetFile(slug, kind, subKey)
        if (f.exists() && f.length() > 1024) return f.absolutePath

        return withContext(Dispatchers.IO) {
            try {
                val bytes: ByteArray = http.get(remoteUrl).body()
                if (bytes.isEmpty()) return@withContext null
                f.writeBytes(bytes)
                f.absolutePath
            } catch (t: Throwable) {
                // Do not crash the reader; caller sees null and can show error
                null
            }
        }
    }

    override suspend fun clearAssetsForStory(slug: String) {
        withContext(Dispatchers.IO) {
            assetsDir(slug).deleteRecursively()
        }
    }
}
