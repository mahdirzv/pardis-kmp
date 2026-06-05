package app.pardis.core.data

import app.pardis.core.domain.GetStoryUseCase
import app.pardis.core.domain.StoryRepository

/**
 * Thin use case for loading a single story's metadata (for video etc.).
 */
class GetStoryUseCaseImpl(
    private val repository: StoryRepository
) : GetStoryUseCase {
    override suspend fun invoke(slug: String): app.pardis.core.model.Story? {
        return repository.getStory(slug)
    }
}