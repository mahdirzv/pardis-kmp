package app.pardis.core.di

import app.pardis.core.data.DownloadVideoUseCaseImpl
import app.pardis.core.data.DownloadStoryAssetsUseCaseImpl
import app.pardis.core.data.ClearStoryAssetsUseCaseImpl
import app.pardis.core.data.GetCachedSizeUseCaseImpl
import app.pardis.core.data.GetLocalAssetPathUseCaseImpl
import app.pardis.core.data.GetLocalVideoPathUseCaseImpl
import app.pardis.core.data.GetProgressUseCaseImpl
import app.pardis.core.data.GetStoriesUseCaseImpl
import app.pardis.core.data.GetStoryPagesUseCaseImpl
import app.pardis.core.data.GetStoryUseCaseImpl
import app.pardis.core.data.NoOpOfflineAssetCache
import app.pardis.core.data.SaveProgressUseCaseImpl
import app.pardis.core.data.StoryRepositoryImpl
import app.pardis.core.data.ProfileRepositoryImpl
import app.pardis.core.data.GetProfilesUseCaseImpl
import app.pardis.core.data.GetSelectedProfileUseCaseImpl
import app.pardis.core.data.SelectProfileUseCaseImpl
import app.pardis.core.domain.ClearStoryAssetsUseCase
import app.pardis.core.domain.DownloadStoryAssetsUseCase
import app.pardis.core.domain.DownloadVideoUseCase
import app.pardis.core.domain.GetLocalAssetPathUseCase
import app.pardis.core.domain.GetLocalVideoPathUseCase
import app.pardis.core.domain.GetCachedSizeUseCase
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
import app.pardis.core.domain.ProfileRepository
import app.pardis.core.domain.GetProfilesUseCase
import app.pardis.core.domain.GetSelectedProfileUseCase
import app.pardis.core.domain.SelectProfileUseCase
import app.pardis.core.network.SupabaseClient
import org.koin.dsl.module

const val platformContextQualifier = "platformContext"

/**
 * Holds the single shared SQLDelight database (null when no driver is provided). A holder lets
 * Koin keep one instance even when the DB is absent — Koin cannot store a null instance directly.
 */
class PardisDatabaseHolder(val database: PardisDatabase?)

val pardisCoreModules = listOf(
    module {
        // Network client (can be overridden in platform modules for auth tokens)
        single { SupabaseClient() }

        // Single shared SQLDelight database wrapper (null when no driver is present), reused by all repos.
        single { PardisDatabaseHolder(getOrNull<SqlDriver>()?.let(::createPardisDatabase)) }

        // Repository layer (data) — uses DB when available for basic offline cache
        single<StoryRepository> {
            StoryRepositoryImpl(get(), get<PardisDatabaseHolder>().database)
        }

        // Profile layer — selected profile persists via the shared SQLDelight DB when available.
        single<ProfileRepository> {
            ProfileRepositoryImpl(get<PardisDatabaseHolder>().database)
        }
        single<GetProfilesUseCase> { GetProfilesUseCaseImpl(get()) }
        single<GetSelectedProfileUseCase> { GetSelectedProfileUseCaseImpl(get()) }
        single<SelectProfileUseCase> { SelectProfileUseCaseImpl(get()) }

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
        single<GetLocalAssetPathUseCase> { GetLocalAssetPathUseCaseImpl(get()) }

        // Full story assets (video + pages illustrations/narration) for offline
        single<DownloadStoryAssetsUseCase> { DownloadStoryAssetsUseCaseImpl(get(), get(), get()) }

        single<ClearStoryAssetsUseCase> { ClearStoryAssetsUseCaseImpl(get()) }

        single<GetCachedSizeUseCase> { GetCachedSizeUseCaseImpl(get()) }
    }
)
