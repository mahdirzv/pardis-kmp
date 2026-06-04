package app.pardis.shared.library

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val libraryModule = module {
    viewModel { LibraryViewModel(get()) }
}