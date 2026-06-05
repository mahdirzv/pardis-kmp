package app.pardis.shared.analytics

import org.koin.dsl.module

val analyticsModule = module {
    single<Analytics> { NoOpAnalytics() }
}