package app.pardis.shared.reader

import app.pardis.core.domain.DownloadVideoUseCase
import app.pardis.core.domain.GetLocalVideoPathUseCase
import app.pardis.shared.analytics.Analytics
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val readerModule = module {
    viewModel { ReaderViewModel(get(), get(), get(), get(), get(), get<GetLocalVideoPathUseCase>(), get<DownloadVideoUseCase>()) }
}