package app.pardis.shared

import app.pardis.core.data.NoOpOfflineAssetCache
import app.pardis.core.di.pardisCoreModules
import app.pardis.core.domain.OfflineAssetCache
import app.pardis.shared.analytics.analyticsModule
import app.pardis.shared.detail.detailModule
import app.pardis.shared.finish.finishModule
import app.pardis.shared.library.libraryModule
import app.pardis.shared.offline.offlineModule
import app.pardis.shared.profile.profileModule
import app.pardis.shared.reader.readerModule
import app.pardis.shared.theme.themeModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.mp.KoinPlatformTools

val pardisSharedModules: List<Module> = listOf(
    libraryModule,
    readerModule,
    detailModule,
    finishModule,
    analyticsModule,
    offlineModule,
    profileModule,
    themeModule,
)

object SharedInit {
    // Named doInit (not init): `init` is a reserved initializer name in Swift, so an `init(...)`
    // method on a Kotlin object can't be called from the iOS app.
    fun doInit(platformModules: List<Module> = emptyList()) {
        val koinContext = KoinPlatformTools.defaultContext()
        if (koinContext.getOrNull() != null) return

        val app = startKoin {
            // Order matters: Koin resolves duplicate definitions with last-loaded-wins.
            // Core modules provide safe defaults (e.g. NoOpOfflineAssetCache); platform modules
            // provide the real implementations (e.g. AndroidOfflineAssetCache / IosOfflineAssetCache).
            // Platform modules MUST load LAST so their real impls override the core defaults —
            // otherwise the no-op cache wins and offline asset downloads silently do nothing.
            modules(pardisCoreModules + pardisSharedModules + platformModules)
        }

        // Fail fast instead of relying on load order staying correct: if a platform provided modules,
        // the real OfflineAssetCache MUST have overridden the core NoOp default. A silent NoOp here
        // means offline downloads do nothing — surface it loudly at startup rather than at runtime.
        validateOfflineCacheWiring(app.koin.get(), hasPlatformModules = platformModules.isNotEmpty())
    }
}

/**
 * Guards the platform-cache override: when platform modules are loaded, the resolved
 * [OfflineAssetCache] must not be the [NoOpOfflineAssetCache] default (which would make offline
 * downloads silently no-op). With no platform modules (e.g. a bring-up/test context) the NoOp
 * default is fine. Pure + side-effect-free so it can be unit-tested without standing up Koin.
 */
internal fun validateOfflineCacheWiring(cache: OfflineAssetCache, hasPlatformModules: Boolean) {
    if (hasPlatformModules) {
        check(cache !is NoOpOfflineAssetCache) {
            "OfflineAssetCache resolved to NoOpOfflineAssetCache despite platform modules being " +
                "loaded — a platform module must register the real cache (check module load order " +
                "in SharedInit.doInit)."
        }
    }
}