package app.pardis.core.domain

import app.pardis.core.model.Story
import app.pardis.core.model.StoryPage

/**
 * Repository for Pardis content (stories metadata + full page content for reader).
 * Implemented in core/data using network + (future) local SQLDelight cache.
 */
interface StoryRepository {
    suspend fun getStories(): List<Story>
    suspend fun getStory(slug: String): Story?
    suspend fun getStoryPages(slug: String): List<StoryPage>
}