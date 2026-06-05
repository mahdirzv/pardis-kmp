package app.pardis.core.domain

import app.pardis.core.model.Story

interface GetStoriesUseCase {
    suspend operator fun invoke(): List<Story>
}

/**
 * Use case to load full pages + related (vocab, couplets) for a story.
 * Returns the rich reader content.
 */
interface GetStoryPagesUseCase {
    suspend operator fun invoke(slug: String): List<app.pardis.core.model.StoryPage>
}