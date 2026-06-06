package app.pardis.core.data

import android.content.Context
import android.util.Log
import app.pardis.core.domain.OfflineAssetCache
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Android implementation of OfflineAssetCache.
 * Stores under app cacheDir/pardis/assets/{slug}/{kind}-{subKey}.dat (or .mp4 for video)
 * Uses Ktor (OkHttp engine) for download (consistent with SupabaseClient).
 * Registered in platformModules in PardisApplication.
 */
class AndroidOfflineAssetCache(
    private val context: Context
) : OfflineAssetCache {

    private val http = HttpClient(OkHttp) {
        // Fail loudly on HTTP errors (4xx/5xx) so they surface as exceptions we log, instead of
        // silently writing a tiny error body that later fails the size check and looks like a
        // generic "download failed".
        expectSuccess = true
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
                // Stream the body straight to disk (ByteReadChannel -> file OutputStream) so large
                // videos (tens of MB) never need to sit in a single in-memory ByteArray.
                http.prepareGet(remoteUrl).execute { response ->
                    f.outputStream().use { out ->
                        response.bodyAsChannel().copyTo(out)
                    }
                }
                if (f.length() > 1024) {
                    f.absolutePath
                } else {
                    // Truncated/empty write (e.g. dropped connection mid-stream) — don't leave a bad file.
                    f.delete()
                    null
                }
            } catch (t: Throwable) {
                // Log the *real* cause (timeout, connection refused, 403/404 on Supabase storage URL,
                // SSL, etc). expectSuccess=true turns HTTP errors into exceptions that land here.
                Log.e("AndroidOfflineAssetCache", "downloadAssetIfNeeded FAILED kind=$kind subKey=$subKey url=$remoteUrl", t)
                // Clean up any partial file so a retry starts fresh.
                if (f.exists()) f.delete()
                null
            }
        }
    }

    override suspend fun clearAssetsForStory(slug: String) {
        withContext(Dispatchers.IO) {
            assetsDir(slug).deleteRecursively()
        }
    }

    override suspend fun getCachedSizeBytes(slug: String): Long = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "pardis/assets/$slug")
        if (!dir.exists()) 0L
        else dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }
}
