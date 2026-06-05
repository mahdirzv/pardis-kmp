package app.pardis.core.di

import app.pardis.core.data.GetStoriesUseCaseImpl
import app.pardis.core.data.GetStoryPagesUseCaseImpl
import app.pardis.core.data.StoryRepositoryImpl
import app.pardis.core.domain.GetStoriesUseCase
import app.pardis.core.domain.GetStoryPagesUseCase
import app.pardis.core.domain.StoryRepository
import org.koin.dsl.module

const val platformContextQualifier = "platformContext"

val pardisCoreModules = listOf(
    module {
        // Repository layer (data)
        single<StoryRepository> { StoryRepositoryImpl() }

        // Use cases (domain, depend on repo)
        single<GetStoriesUseCase> { GetStoriesUseCaseImpl(get()) }
        single<GetStoryPagesUseCase> { GetStoryPagesUseCaseImpl(get()) }
    }
)