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
 * Basic Ktor client for public Supabase reads (Pardis content).
 * Tables are publicly readable (for select using (true) RLS).
 * Config (URL + anon key) provided exclusively via platform expect/actual in androidMain/iosMain.
 * Never put literals in commonMain.
 */
object Supabase {
    private val BASE get() = SupabaseSecrets.baseUrl
    private val ANON get() = SupabaseSecrets.anonKey

    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }

    suspend fun getStories(params: Map<String, String> = emptyMap()): List<StoryRow> {
        return client.get("$BASE/stories") {
            header("apikey", ANON)
            header("Authorization", "Bearer $ANON")
            params.forEach { (k, v) -> parameter(k, v) }
        }.body()
    }

    suspend fun getStoryPages(storySlug: String): List<StoryPageRow> {
        return client.get("$BASE/story_pages") {
            header("apikey", ANON)
            header("Authorization", "Bearer $ANON")
            parameter("select", "*")
            parameter("story_slug", "eq.$storySlug")
            parameter("order", "page_number")
        }.body()
    }

    suspend fun getCouplets(storySlug: String): List<CoupletRow> {
        return client.get("$BASE/couplets") {
            header("apikey", ANON)
            header("Authorization", "Bearer $ANON")
            parameter("select", "fa,en,page_number")
            parameter("story_slug", "eq.$storySlug")
        }.body()
    }

    suspend fun getVocabTerms(storySlug: String): List<VocabRow> {
        return client.get("$BASE/vocab_terms") {
            header("apikey", ANON)
            header("Authorization", "Bearer $ANON")
            parameter("select", "word_fa,translit,meaning_en,context,page_number,audio_url")
            parameter("story_slug", "eq.$storySlug")
            parameter("order", "page_number")
        }.body()
    }
}