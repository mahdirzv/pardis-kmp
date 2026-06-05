package app.pardis.core.di

import app.pardis.core.data.DownloadVideoUseCaseImpl
import app.pardis.core.data.GetLocalVideoPathUseCaseImpl
import app.pardis.core.data.GetProgressUseCaseImpl
import app.pardis.core.data.GetStoriesUseCaseImpl
import app.pardis.core.data.GetStoryPagesUseCaseImpl
import app.pardis.core.data.GetStoryUseCaseImpl
import app.pardis.core.data.NoOpVideoCache
import app.pardis.core.data.SaveProgressUseCaseImpl
import app.pardis.core.data.StoryRepositoryImpl
import app.pardis.core.domain.DownloadVideoUseCase
import app.pardis.core.domain.GetLocalVideoPathUseCase
import app.pardis.core.domain.VideoCache
import app.cash.sqldelight.db.SqlDriver
import app.pardis.core.database.PardisDatabase
import app.pardis.core.database.createPardisDatabase
import app.pardis.core.domain.GetProgressUseCase
import app.pardis.core.domain.GetStoriesUseCase
import app.pardis.core.domain.GetStoryPagesUseCase
import app.pardis.core.domain.GetStoryUseCase
import app.pardis.core.domain.SaveProgressUseCase
import app.pardis.core.domain.StoryRepository
import app.pardis.core.network.SupabaseClient
import org.koin.dsl.module

const val platformContextQualifier = "platformContext"

val pardisCoreModules = listOf(
    module {
        // Network client (can be overridden in platform modules for auth tokens)
        single { SupabaseClient() }

        // Database (driver provided by platformModules; optional for iOS until Swift bootstrap wired)
        single<PardisDatabase?> { getOrNull<SqlDriver>()?.let { createPardisDatabase(it) } }

        // Repository layer (data) — uses DB when available for basic offline cache
        single<StoryRepository> { StoryRepositoryImpl(get(), getOrNull<PardisDatabase>()) }

        // Use cases (domain, depend on repo)
        single<GetStoriesUseCase> { GetStoriesUseCaseImpl(get()) }
        single<GetStoryUseCase> { GetStoryUseCaseImpl(get()) }
        single<GetStoryPagesUseCase> { GetStoryPagesUseCaseImpl(get()) }
        single<SaveProgressUseCase> { SaveProgressUseCaseImpl(get()) }
        single<GetProgressUseCase> { GetProgressUseCaseImpl(get()) }

        // Video/asset offline cache (overridden in platform modules with real FS impl; no-op safe default)
        single<VideoCache> { NoOpVideoCache() }

        // Video download / local path use cases (for offline video in reader)
        single<GetLocalVideoPathUseCase> { GetLocalVideoPathUseCaseImpl(get()) }
        single<DownloadVideoUseCase> { DownloadVideoUseCaseImpl(get()) }
    }
)