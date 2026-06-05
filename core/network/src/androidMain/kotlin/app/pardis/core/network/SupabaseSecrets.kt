package app.pardis.core.network

/**
 * Android actual for public Supabase config (Pardis content).
 * These are the safe NEXT_PUBLIC_* values from the Pardis web .env.local.
 * Public anon key is intended to be embedded for client reads (same as web).
 */
actual object SupabaseSecrets {
    actual val baseUrl: String = "https://tpjgnlcporgnlrjwjufq.supabase.co/rest/v1"
    actual val anonKey: String =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRwamdubGNwb3JnbmxyandqdWZxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzgzMTkzODUsImV4cCI6MjA5Mzg5NTM4NX0.hhw9JZnwNkuaE_Mkjmy_gK_GW42BukzdYfy5d0n9CPo"
}