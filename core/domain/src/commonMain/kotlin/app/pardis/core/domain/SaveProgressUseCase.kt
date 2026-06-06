package app.pardis.core.domain

interface SaveProgressUseCase {
    suspend operator fun invoke(slug: String, page: Int)
}

interface GetProgressUseCase {
    suspend operator fun invoke(slug: String): Int?
}