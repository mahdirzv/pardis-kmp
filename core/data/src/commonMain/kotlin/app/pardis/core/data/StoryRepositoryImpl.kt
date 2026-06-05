package app.pardis.core.data

import app.pardis.core.database.PardisDatabase
import app.pardis.core.domain.StoryRepository
import app.pardis.core.model.Bookend
import app.pardis.core.model.BookendAudio
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
import kotlinx.serialization.json.Json

/**
 * Ktor + Supabase backed implementation of StoryRepository.
 * Does the same 3-table join pattern as web getStoryPages for fidelity.
 * Basic offline: uses SQLDelight cache for stories when provided (fallback on net fail).
 * authToken support for Phase 3+ authenticated calls.
 */
class StoryRepositoryImpl(
    private val supabase: SupabaseClient = SupabaseClient(),
    private val db: PardisDatabase? = null
) : StoryRepository {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private fun Story.toJson(): String = json.encodeToString(Story.serializer(), this)
    private fun jsonToStory(s: String): Story? = try { json.decodeFromString(Story.serializer(), s) } catch (_: Exception) { null }
    override suspend fun getStory(slug: String): Story? {
        // Try cache first for basic offline
        db?.pardisQueries?.selectStory(slug)?.executeAsOneOrNull()?.let { cached ->
            jsonToStory(cached.data_json)?.let { return it }
        }
        return try {
            val row = supabase.getStory(slug) ?: return null
            val story = Story(
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
                coverUrl = row.cover_url,
                videoUrlFa = row.video_url_fa,
                videoUrlEn = row.video_url_en,
                introAudio = row.intro_audio?.let { Bookend(
                    fa = it.fa?.let { b -> BookendAudio(b.url, b.durationSeconds, b.voice) },
                    en = it.en?.let { b -> BookendAudio(b.url, b.durationSeconds, b.voice) }
                ) },
                outroAudio = row.outro_audio?.let { Bookend(
                    fa = it.fa?.let { b -> BookendAudio(b.url, b.durationSeconds, b.voice) },
                    en = it.en?.let { b -> BookendAudio(b.url, b.durationSeconds, b.voice) }
                ) }
            )
            // Upsert to cache (enriched with video/bookends if present)
            upsertToCache(story)
            story
        } catch (t: Throwable) {
            // If full video fields not in DB yet, don't break the reader — just no video data.
            // Pages will still load in the caller (ReaderVM).
            null
        }
    }

    private fun upsertToCache(story: Story) {
        db?.pardisQueries?.insertOrReplaceStory(
            slug = story.slug,
            title_en = story.titleEn,
            title_fa = story.titleFa,
            age_band = story.ageBand,
            data_json = story.toJson(),
            downloaded_at = System.currentTimeMillis()
        )
    }

    override suspend fun getStories(): List<Story> {
        return try {
            // Prefer network; on any failure (offline etc) fallback to cache if present
            // Library list uses minimal select (safe even if video columns not yet added to Supabase table).
            // Video-specific fields (video_url_*, intro/outro) are only fetched in getStory() for the Reader.
            val rows: List<StoryRow> = supabase.getStories(
                mapOf(
                    "select" to "slug,title_en,title_fa,age_band,minutes,page_count,vocab_count,status,kid_ready,video_ready,cover_url",
                    "status" to "eq.available",
                    "order" to "display_order"
                )
            )
            val stories = rows.map { row ->
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
                    coverUrl = row.cover_url,
                    videoUrlFa = row.video_url_fa,
                    videoUrlEn = row.video_url_en,
                    introAudio = row.intro_audio?.let { Bookend(
                        fa = it.fa?.let { b -> BookendAudio(b.url, b.durationSeconds, b.voice) },
                        en = it.en?.let { b -> BookendAudio(b.url, b.durationSeconds, b.voice) }
                    ) },
                    outroAudio = row.outro_audio?.let { Bookend(
                        fa = it.fa?.let { b -> BookendAudio(b.url, b.durationSeconds, b.voice) },
                        en = it.en?.let { b -> BookendAudio(b.url, b.durationSeconds, b.voice) }
                    ) }
                )
            }
            // Cache for offline
            stories.forEach { upsertToCache(it) }
            stories
        } catch (t: Throwable) {
            // Fallback to cache for offline/network issues. But if it looks like a schema/column error (e.g. video fields not added to DB yet),
            // rethrow so LibraryScreen shows the real error message + Retry button instead of silent blank list.
            val msg = t.message?.lowercase() ?: ""
            val looksLikeSchemaError = msg.contains("column") || msg.contains("does not exist") || msg.contains("schema")
            if (looksLikeSchemaError) {
                throw t
            }
            db?.pardisQueries?.selectAllStories()?.executeAsList()?.mapNotNull { jsonToStory(it.data_json) } ?: emptyList()
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