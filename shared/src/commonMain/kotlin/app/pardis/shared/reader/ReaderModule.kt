package app.pardis.shared.reader

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val readerModule = module {
    viewModel { ReaderViewModel() }
}