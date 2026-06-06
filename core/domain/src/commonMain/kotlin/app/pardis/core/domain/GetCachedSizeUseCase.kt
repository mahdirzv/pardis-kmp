package app.pardis.core.domain

/** Returns total bytes cached for a story (0 if none). */
interface GetCachedSizeUseCase {
    suspend operator fun invoke(slug: String): Long
}
