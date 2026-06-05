package app.pardis.shared

import app.pardis.core.di.pardisCoreModules
import app.pardis.shared.analytics.analyticsModule
import app.pardis.shared.library.libraryModule
import app.pardis.shared.reader.readerModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.mp.KoinPlatformTools

val pardisSharedModules: List<Module> = listOf(
    libraryModule,
    readerModule,
    analyticsModule,
)

object SharedInit {
    fun init(platformModules: List<Module> = emptyList()) {
        val koinContext = KoinPlatformTools.defaultContext()
        if (koinContext.getOrNull() != null) return

        startKoin {
            modules(platformModules + pardisCoreModules + pardisSharedModules)
        }
    }
}