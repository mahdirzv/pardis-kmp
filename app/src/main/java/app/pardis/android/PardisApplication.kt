package app.pardis.android

import android.app.Application
import app.pardis.core.di.platformContextQualifier
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
                    // Add Supabase anon key or other config here if needed as named singles
                }
            )
        )
    }
}