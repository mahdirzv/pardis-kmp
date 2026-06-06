package app.pardis.core.data

import app.cash.sqldelight.db.SqlDriver
import app.pardis.core.database.provideSqlDriver
import app.pardis.core.domain.OfflineAssetCache
import org.koin.dsl.module

/**
 * iOS platform DI module. Pass this (or list containing it) from Swift's SharedInit.shared.doInit(platformModules: ...)
 * when you want the real iOS offline stack (asset cache + SQLDelight driver for pages/progress cache).
 */
val iosOfflineAssetCacheModule = module {
    single<SqlDriver> { provideSqlDriver() }
    single<OfflineAssetCache> { IosOfflineAssetCache() }
}
