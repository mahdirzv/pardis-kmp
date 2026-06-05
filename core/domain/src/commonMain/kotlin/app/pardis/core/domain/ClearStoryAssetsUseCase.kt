package app.pardis.core.domain

/**
 * Clear cached assets for a story (video + pages).
 */
interface ClearStoryAssetsUseCase {
    suspend operator fun invoke(slug: String)
}