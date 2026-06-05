package app.pardis.core.data

import android.content.Context
import app.pardis.core.domain.VideoCache
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Android implementation of VideoCache.
 * Stores under app cacheDir/pardis/videos/{slug}/video-{lang}.mp4
 * Uses Ktor for download (consistent with SupabaseClient).
 * Registered in platformModules in PardisApplication.
 */
class AndroidVideoCache(
    private val context: Context
) : VideoCache {

    private val http = HttpClient {
        // Minimal config; follows existing lenient json but not needed for bytes
    }

    private fun videoDir(slug: String): File {
        return File(context.cacheDir, "pardis/videos/$slug").apply { mkdirs() }
    }

    private fun videoFile(slug: String, lang: String): File {
        return File(videoDir(slug), "video-$lang.mp4")
    }

    override suspend fun getLocalVideoPath(slug: String, lang: String): String? {
        val f = videoFile(slug, lang)
        return if (f.exists() && f.length() > 1024) f.absolutePath else null
    }

    override suspend fun downloadVideoIfNeeded(slug: String, lang: String, remoteUrl: String): String? {
        val f = videoFile(slug, lang)
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

    override suspend fun clearVideoCache(slug: String) {
        withContext(Dispatchers.IO) {
            videoDir(slug).deleteRecursively()
        }
    }
}
