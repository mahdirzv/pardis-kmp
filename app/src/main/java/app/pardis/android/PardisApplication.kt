package app.pardis.android

import android.app.Application
import app.pardis.core.data.AndroidOfflineAssetCache
import app.pardis.core.database.provideSqlDriver
import app.pardis.core.di.platformContextQualifier
import app.pardis.core.domain.OfflineAssetCache
import app.pardis.core.network.SupabaseClient
import app.pardis.core.network.SupabaseConfig
import app.pardis.shared.SharedInit
import org.koin.core.qualifier.named
import org.koin.dsl.module

class PardisApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SharedInit.init(
            platformModules = listOf(
                module {
                    single { this@PardisApplication }
                    single<Any>(named(platformContextQualifier)) { applicationContext }

                    // SQLDelight driver for basic offline cache (used by PardisDatabase in core)
                    single { provideSqlDriver(applicationContext) }

                    // Supabase client: default uses public anon (from platform expect/actual).
                    // For auth (Phase 3+), provide a configured instance with user token:
                    // single { SupabaseClient(SupabaseConfig(/* base from secrets or override */, userToken = "jwt-here")) }
                    single { SupabaseClient() }

                    // Real Android asset cache (FS + download) overriding the no-op. Enables offline video + page assets.
                    // Use applicationContext directly (like provideSqlDriver above): there is no bare
                    // single<Context> registered, so get() would fail to resolve android.content.Context.
                    single<OfflineAssetCache> { AndroidOfflineAssetCache(applicationContext) }
                }
            )
        )
    }
}