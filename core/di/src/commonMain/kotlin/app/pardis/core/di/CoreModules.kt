package app.pardis.core.di

import app.pardis.core.data.GetStoriesUseCaseImpl
import app.pardis.core.domain.GetStoriesUseCase
import org.koin.dsl.module

const val platformContextQualifier = "platformContext"

val pardisCoreModules = listOf(
    module {
        single<GetStoriesUseCase> { GetStoriesUseCaseImpl() }
        // Add network, db, etc. as implemented
    }
)