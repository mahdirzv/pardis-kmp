package app.pardis.core.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Basic Ktor client for public Supabase reads (Pardis content).
 * Tables are publicly readable.
 * For production reader, configure anon key or use Supabase KMP client.
 */
object Supabase {
    // Public demo URL from Pardis web. In real: inject from platform config / BuildConfig.
    private const val BASE = "https://tpjgnlcporgnlrjwjufq.supabase.co/rest/v1"
    // Anon key is safe for public reads; in app use from secure config.
    private const val ANON = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRwamdubGNwb3Jnbmxyaml3dWZxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDU5NDUyNzAsImV4cCI6MjA2MTUyMTI3MH0.placeholder" // replace with real anon from .env if needed; for public data often works without

    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
        defaultRequest {
            header("apikey", ANON)
            header("Authorization", "Bearer $ANON")
        }
    }

    suspend inline fun <reified T> get(path: String, params: Map<String, String> = emptyMap()): T {
        return client.get("$BASE/$path") {
            params.forEach { (k, v) -> parameter(k, v) }
        }.body()
    }
}