package app.pardis.shared.detail

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val detailModule = module {
    viewModel { StoryDetailViewModel(get(), get(), get()) }
}
