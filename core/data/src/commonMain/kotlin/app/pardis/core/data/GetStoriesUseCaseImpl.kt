package app.pardis.core.data

import app.pardis.core.domain.GetStoriesUseCase
import app.pardis.core.model.Story
import app.pardis.core.network.Supabase
import kotlinx.serialization.Serializable

@Serializable
private data class StoryRow(
    val slug: String,
    val title_en: String,
    val title_fa: String,
    val age_band: String,
    val minutes: Int,
    val page_count: Int,
    val vocab_count: Int,
    val status: String = "available",
    val kid_ready: Boolean = false,
    val video_ready: Boolean = false,
    val cover_url: String? = null
)

class GetStoriesUseCaseImpl : GetStoriesUseCase {
    override suspend fun invoke(): List<Story> {
        // Public query for available stories
        val rows: List<StoryRow> = Supabase.get("stories", mapOf(
            "select" to "slug,title_en,title_fa,age_band,minutes,page_count,vocab_count,status,kid_ready,video_ready,cover_url",
            "status" to "eq.available",
            "order" to "display_order"
        ))
        return rows.map {
            Story(
                slug = it.slug,
                titleEn = it.title_en,
                titleFa = it.title_fa,
                ageBand = it.age_band,
                minutes = it.minutes,
                pageCount = it.page_count,
                vocabCount = it.vocab_count,
                status = it.status,
                kidReady = it.kid_ready,
                videoReady = it.video_ready,
                coverUrl = it.cover_url
            )
        }
    }
}