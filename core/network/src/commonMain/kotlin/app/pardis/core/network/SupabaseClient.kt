package app.pardis.core.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Platform-provided Supabase public config.
 * Actual implementations MUST live only in androidMain and iosMain source sets.
 */
expect object SupabaseSecrets {
    val baseUrl: String
    val anonKey: String
}

/**
 * Config for Supabase client. anonKey always sent as apikey.
 * For authenticated calls, pass userToken (JWT) as bearer.
 */
data class SupabaseConfig(
    val baseUrl: String = SupabaseSecrets.baseUrl,
    val anonKey: String = SupabaseSecrets.anonKey
)

@Serializable
data class StoryRow(
    val slug: String,
    val title_en: String,
    val title_fa: String,
    val age_band: String,
    val minutes: Int,
    val page_count: Int,
    val vocab_count: Int,
    val status: String = "available",
    val kid_ready: Boolean = false,
    val video_ready: Boolean = false,
    val cover_url: String? = null
)

// --- Story pages + related (for reader) ---

@Serializable
data class StoryPageRow(
    val page_number: Int,
    val illustration_url: String? = null,
    val narration_seconds: Double = 0.0,
    val paragraphs_en: List<String> = emptyList(),
    val paragraphs_fa: List<String> = emptyList(),
    val narration_fa: NarrationRow? = null,
    val narration_en: NarrationRow? = null
)

@Serializable
data class NarrationRow(
    val url: String,
    val durationSeconds: Double,
    val voice: String? = null
)

@Serializable
data class CoupletRow(
    val fa: String,
    val en: String,
    val page_number: Int? = null
)

@Serializable
data class VocabRow(
    val word_fa: String,
    val translit: String,
    val meaning_en: String,
    val context: String,
    val page_number: Int? = null,
    val audio_url: String? = null
)

/**
 * Basic Ktor client for Supabase reads (Pardis content).
 * Public tables use anon key (for select using (true) RLS).
 * For user-specific data (Phase 3+), pass authToken (user JWT) to use Bearer.
 * Config provided via platform; injectable in DI for auth support.
 */
class SupabaseClient(config: SupabaseConfig = SupabaseConfig()) {
    private val baseUrl = config.baseUrl
    private val anonKey = config.anonKey

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }

    private fun HttpRequestBuilder.applyHeaders(authToken: String? = null) {
        val bearer = authToken ?: anonKey
        header("apikey", anonKey)
        header("Authorization", "Bearer $bearer")
    }

    suspend fun getStories(params: Map<String, String> = emptyMap(), authToken: String? = null): List<StoryRow> {
        return client.get("$baseUrl/stories") {
            applyHeaders(authToken)
            params.forEach { (k, v) -> parameter(k, v) }
        }.body()
    }

    suspend fun getStoryPages(storySlug: String, authToken: String? = null): List<StoryPageRow> {
        return client.get("$baseUrl/story_pages") {
            applyHeaders(authToken)
            parameter("select", "*")
            parameter("story_slug", "eq.$storySlug")
            parameter("order", "page_number")
        }.body()
    }

    suspend fun getCouplets(storySlug: String, authToken: String? = null): List<CoupletRow> {
        return client.get("$baseUrl/couplets") {
            applyHeaders(authToken)
            parameter("select", "fa,en,page_number")
            parameter("story_slug", "eq.$storySlug")
        }.body()
    }

    suspend fun getVocabTerms(storySlug: String, authToken: String? = null): List<VocabRow> {
        return client.get("$baseUrl/vocab_terms") {
            applyHeaders(authToken)
            parameter("select", "word_fa,translit,meaning_en,context,page_number,audio_url")
            parameter("story_slug", "eq.$storySlug")
            parameter("order", "page_number")
        }.body()
    }
}

// Backwards-compatible object for existing code (uses public anon only)
object Supabase {
    private val client = SupabaseClient()

    suspend fun getStories(params: Map<String, String> = emptyMap()): List<StoryRow> =
        client.getStories(params)

    suspend fun getStoryPages(storySlug: String): List<StoryPageRow> =
        client.getStoryPages(storySlug)

    suspend fun getCouplets(storySlug: String): List<CoupletRow> =
        client.getCouplets(storySlug)

    suspend fun getVocabTerms(storySlug: String): List<VocabRow> =
        client.getVocabTerms(storySlug)
}