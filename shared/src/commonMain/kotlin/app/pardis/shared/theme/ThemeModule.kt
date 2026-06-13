package app.pardis.shared.theme

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val themeModule = module {
    viewModel { ThemeViewModel(get(), get()) }
}
