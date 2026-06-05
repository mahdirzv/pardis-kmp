package app.pardis.core.data

import android.content.Context
import android.util.Log
import app.pardis.core.domain.OfflineAssetCache
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Android implementation of OfflineAssetCache.
 * Stores under app cacheDir/pardis/assets/{slug}/{kind}-{subKey}.dat (or .mp4 for video)
 * Uses Ktor for download (consistent with SupabaseClient).
 * Registered in platformModules in PardisApplication.
 */
class AndroidOfflineAssetCache(
    private val context: Context
) : OfflineAssetCache {

    private val http = HttpClient(OkHttp) {
        // Explicit engine (ktor-client-okhttp) + long timeouts for large public MP4 video files.
        // Streaming used below for video to avoid OOM from full ByteArray.
        engine {
            config {
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(15, TimeUnit.MINUTES)   // videos can be large; give plenty of time
                writeTimeout(15, TimeUnit.MINUTES)
            }
        }
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
                // Note: for very large videos a streaming copy (ByteReadChannel to OutputStream) would be better
                // to avoid full memory load. For now we use body<ByteArray> + the engine has long read timeout.
                val bytes: ByteArray = http.get(remoteUrl).body()
                if (bytes.isEmpty()) return@withContext null
                f.writeBytes(bytes)
                if (f.length() > 1024) {
                    f.absolutePath
                } else {
                    f.delete()
                    null
                }
            } catch (t: Throwable) {
                // Log the *real* cause (timeout, connection refused, 403/404 on Supabase storage URL,
                // SSL, OOM on huge ByteArray, etc). This is the key to understand "download failed".
                Log.e("AndroidOfflineAssetCache", "downloadAssetIfNeeded FAILED kind=$kind subKey=$subKey url=$remoteUrl", t)
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
