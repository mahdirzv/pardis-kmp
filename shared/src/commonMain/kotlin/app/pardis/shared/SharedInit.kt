package app.pardis.shared

import app.pardis.core.di.pardisCoreModules
import app.pardis.shared.analytics.analyticsModule
import app.pardis.shared.library.libraryModule
import app.pardis.shared.offline.offlineModule
import app.pardis.shared.reader.readerModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.mp.KoinPlatformTools

val pardisSharedModules: List<Module> = listOf(
    libraryModule,
    readerModule,
    analyticsModule,
    offlineModule,
)

object SharedInit {
    fun init(platformModules: List<Module> = emptyList()) {
        val koinContext = KoinPlatformTools.defaultContext()
        if (koinContext.getOrNull() != null) return

        startKoin {
            // Order matters: Koin resolves duplicate definitions with last-loaded-wins.
            // Core modules provide safe defaults (e.g. NoOpOfflineAssetCache); platform modules
            // provide the real implementations (e.g. AndroidOfflineAssetCache / IosOfflineAssetCache).
            // Platform modules MUST load LAST so their real impls override the core defaults —
            // otherwise the no-op cache wins and offline asset downloads silently do nothing.
            modules(pardisCoreModules + pardisSharedModules + platformModules)
        }
    }
}