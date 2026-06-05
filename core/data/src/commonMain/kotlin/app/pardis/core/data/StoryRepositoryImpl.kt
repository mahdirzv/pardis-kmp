package app.pardis.core.data

import app.pardis.core.domain.StoryRepository
import app.pardis.core.model.Couplet
import app.pardis.core.model.Narration
import app.pardis.core.model.Story
import app.pardis.core.model.StoryPage
import app.pardis.core.model.VocabItem
import app.pardis.core.network.CoupletRow
import app.pardis.core.network.StoryPageRow
import app.pardis.core.network.StoryRow
import app.pardis.core.network.SupabaseClient
import app.pardis.core.network.VocabRow
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Ktor + Supabase backed implementation of StoryRepository.
 * Does the same 3-table join pattern as web getStoryPages for fidelity.
 * Future: will merge with local SQLDelight cache for offline.
 * authToken support for Phase 3+ authenticated calls.
 */
class StoryRepositoryImpl(
    private val supabase: SupabaseClient = SupabaseClient()
) : StoryRepository {
    override suspend fun getStories(): List<Story> {
        // Public query for available stories. Matches web select + filters.
        val rows: List<StoryRow> = supabase.getStories(
            mapOf(
                "select" to "slug,title_en,title_fa,age_band,minutes,page_count,vocab_count,status,kid_ready,video_ready,cover_url",
                "status" to "eq.available",
                "order" to "display_order"
            )
        )
        return rows.map { row ->
            Story(
                slug = row.slug,
                titleEn = row.title_en,
                titleFa = row.title_fa,
                ageBand = row.age_band,
                minutes = row.minutes,
                pageCount = row.page_count,
                vocabCount = row.vocab_count,
                status = row.status,
                kidReady = row.kid_ready,
                videoReady = row.video_ready,
                coverUrl = row.cover_url
            )
        }
    }

    override suspend fun getStoryPages(slug: String): List<StoryPage> {
        val (pageRows, coupletRows, vocabRows) = coroutineScope {
            awaitAll<List<*>>(
                async { supabase.getStoryPages(slug) },
                async { supabase.getCouplets(slug) },
                async { supabase.getVocabTerms(slug) }
            )
        }

        val coupletsByPage = (coupletRows as List<CoupletRow>).associateBy { it.page_number }
        val vocabsByPage = (vocabRows as List<VocabRow>).groupBy { it.page_number }

        return (pageRows as List<StoryPageRow>).map { p ->
            StoryPage(
                page = p.page_number,
                illustrationUrl = p.illustration_url,
                paragraphsEn = p.paragraphs_en,
                paragraphsFa = p.paragraphs_fa,
                narrationFa = p.narration_fa?.let { Narration(it.url, it.durationSeconds, it.voice) },
                narrationEn = p.narration_en?.let { Narration(it.url, it.durationSeconds, it.voice) },
                vocabulary = (vocabsByPage[p.page_number] ?: emptyList()).map { v ->
                    VocabItem(
                        fa = v.word_fa,
                        translit = v.translit,
                        en = v.meaning_en,
                        context = v.context,
                        audioUrl = v.audio_url
                    )
                },
                couplet = coupletsByPage[p.page_number]?.let { Couplet(it.fa, it.en) }
            )
        }
    }
}