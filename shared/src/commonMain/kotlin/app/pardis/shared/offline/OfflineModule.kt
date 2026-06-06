package app.pardis.shared.offline

import org.koin.dsl.module

val offlineModule = module {
    single { OfflineDownloadManager(get(), get(), get()) }
}
