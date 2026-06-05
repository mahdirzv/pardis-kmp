package app.pardis.core.di

import app.pardis.core.data.GetStoriesUseCaseImpl
import app.pardis.core.data.GetStoryPagesUseCaseImpl
import app.pardis.core.data.StoryRepositoryImpl
import app.pardis.core.domain.GetStoriesUseCase
import app.pardis.core.domain.GetStoryPagesUseCase
import app.pardis.core.domain.StoryRepository
import app.pardis.core.network.SupabaseClient
import org.koin.dsl.module

const val platformContextQualifier = "platformContext"

val pardisCoreModules = listOf(
    module {
        // Network client (can be overridden in platform modules for auth tokens)
        single { SupabaseClient() }

        // Repository layer (data)
        single<StoryRepository> { StoryRepositoryImpl(get()) }

        // Use cases (domain, depend on repo)
        single<GetStoriesUseCase> { GetStoriesUseCaseImpl(get()) }
        single<GetStoryPagesUseCase> { GetStoryPagesUseCaseImpl(get()) }
    }
)