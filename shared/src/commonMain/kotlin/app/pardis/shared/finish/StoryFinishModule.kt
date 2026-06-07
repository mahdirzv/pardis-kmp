package app.pardis.shared.finish

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val finishModule = module {
    viewModel { StoryFinishViewModel(get()) }
}
