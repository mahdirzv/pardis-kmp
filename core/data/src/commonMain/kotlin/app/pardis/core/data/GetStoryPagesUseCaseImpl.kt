package app.pardis.core.data

import app.pardis.core.domain.GetStoryPagesUseCase
import app.pardis.core.domain.StoryRepository

/**
 * Thin use case for loading reader pages for a story.
 */
class GetStoryPagesUseCaseImpl(
    private val repository: StoryRepository
) : GetStoryPagesUseCase {
    override suspend fun invoke(slug: String): List<app.pardis.core.model.StoryPage> {
        return repository.getStoryPages(slug)
    }
}