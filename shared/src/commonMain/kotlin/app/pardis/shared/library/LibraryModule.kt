package app.pardis.shared.library

import app.pardis.shared.analytics.Analytics
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val libraryModule = module {
    viewModel { LibraryViewModel(get(), get()) }
}