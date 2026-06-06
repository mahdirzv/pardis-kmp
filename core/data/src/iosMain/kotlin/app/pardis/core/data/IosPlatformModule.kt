package app.pardis.core.data

import app.pardis.core.domain.OfflineAssetCache
import org.koin.dsl.module

/**
 * iOS platform DI module. Pass this (or list containing it) from Swift's SharedInit().init(platformModules: ...)
 * when you want real offline asset cache on iOS (pairs with enabling the SqlDriver for pages cache too).
 */
val iosOfflineAssetCacheModule = module {
    single<OfflineAssetCache> { IosOfflineAssetCache() }
}
