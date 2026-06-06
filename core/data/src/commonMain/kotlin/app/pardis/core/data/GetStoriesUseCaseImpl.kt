package app.pardis.core.data

import app.pardis.core.domain.GetStoriesUseCase
import app.pardis.core.domain.StoryRepository
import app.pardis.core.model.Story

/**
 * Thin use case delegating to repository (proper data/domain separation).
 */
class GetStoriesUseCaseImpl(
    private val repository: StoryRepository
) : GetStoriesUseCase {
    override suspend fun invoke(): List<Story> {
        return repository.getStories()
    }
}