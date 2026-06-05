package app.pardis.core.di

import app.pardis.core.data.DownloadVideoUseCaseImpl
import app.pardis.core.data.GetLocalVideoPathUseCaseImpl
import app.pardis.core.data.GetProgressUseCaseImpl
import app.pardis.core.data.GetStoriesUseCaseImpl
import app.pardis.core.data.GetStoryPagesUseCaseImpl
import app.pardis.core.data.GetStoryUseCaseImpl
import app.pardis.core.data.NoOpOfflineAssetCache
import app.pardis.core.data.SaveProgressUseCaseImpl
import app.pardis.core.data.StoryRepositoryImpl
import app.pardis.core.domain.ClearStoryAssetsUseCase
import app.pardis.core.domain.DownloadStoryAssetsUseCase
import app.pardis.core.domain.DownloadVideoUseCase
import app.pardis.core.domain.GetLocalAssetPathUseCase
import app.pardis.core.domain.GetLocalVideoPathUseCase
import app.pardis.core.domain.OfflineAssetCache
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

        // Offline asset cache (overridden in platform modules with real FS impl; no-op safe default)
        single<OfflineAssetCache> { NoOpOfflineAssetCache() }

        // Asset download / local path use cases (for offline video + page assets in reader)
        single<GetLocalVideoPathUseCase> { GetLocalVideoPathUseCaseImpl(get()) }
        single<DownloadVideoUseCase> { DownloadVideoUseCaseImpl(get()) }

        // General local asset path (for resolving cached illustrations, narrations, video in reader)
        single<app.pardis.core.domain.GetLocalAssetPathUseCase> { app.pardis.core.data.GetLocalAssetPathUseCaseImpl(get()) }

        // Full story assets (video + pages illustrations/narration) for offline
        single<app.pardis.core.domain.DownloadStoryAssetsUseCase> { app.pardis.core.data.DownloadStoryAssetsUseCaseImpl(get(), get(), get()) }

        single<app.pardis.core.domain.ClearStoryAssetsUseCase> { app.pardis.core.data.ClearStoryAssetsUseCaseImpl(get()) }
    }
)