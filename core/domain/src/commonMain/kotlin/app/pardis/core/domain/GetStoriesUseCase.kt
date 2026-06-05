package app.pardis.core.domain

import app.pardis.core.model.Story
import app.pardis.core.model.StoryPage

interface GetStoriesUseCase {
    suspend operator fun invoke(): List<Story>
}

interface GetStoryUseCase {
    suspend operator fun invoke(slug: String): Story?
}

/**
 * Use case to load full pages + related (vocab, couplets) for a story.
 * Returns the rich reader content.
 */
interface GetStoryPagesUseCase {
    suspend operator fun invoke(slug: String): List<StoryPage>
}