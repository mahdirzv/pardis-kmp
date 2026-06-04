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
 * Tables are publicly readable (for select using (true) RLS).
 * Keys and URL taken from the Pardis web project (.env.local and client usage).
 * For full app, provide via platform config (Android BuildConfig / iOS secrets) and inject in SharedInit platformModules.
 */
object Supabase {
    // From Pardis web project: NEXT_PUBLIC_SUPABASE_URL
    private const val BASE = "https://tpjgnlcporgnlrjwjufq.supabase.co/rest/v1"

    // From Pardis web project: NEXT_PUBLIC_SUPABASE_ANON_KEY (safe for public client reads)
    private const val ANON = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRwamdubGNwb3JnbmxyandqdWZxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzgzMTkzODUsImV4cCI6MjA5Mzg5NTM4NX0.hhw9JZnwNkuaE_Mkjmy_gK_GW42BukzdYfy5d0n9CPo"

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