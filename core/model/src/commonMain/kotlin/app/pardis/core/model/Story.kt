package app.pardis.core.model

import kotlinx.serialization.Serializable

/**
 * Core shared models for Pardis content.
 * Mirrors the public shapes from the web backend (stories + story_pages + vocab_terms).
 * These are the contracts owned by shared logic.
 */

@Serializable
data class Story(
    val slug: String,
    val titleEn: String,
    val titleFa: String,
    val pronunciation: String? = null,
    val ageBand: String, // "4–7", "7–10", "10–14"
    val minutes: Int,
    val pageCount: Int,
    val vocabCount: Int,
    val status: String = "available",
    val kidReady: Boolean = false,
    val gamesReady: Boolean = false,
    val videoReady: Boolean = false,
    val coverUrl: String? = null,
    val blurbEn: String? = null,
    val blurbFa: String? = null,
)

@Serializable
data class StoryPage(
    val page: Int,
    val illustrationUrl: String? = null,
    val paragraphsEn: List<String> = emptyList(),
    val paragraphsFa: List<String> = emptyList(),
    val narrationFa: Narration? = null,
    val narrationEn: Narration? = null,
    val vocabulary: List<VocabItem> = emptyList(),
    val couplet: Couplet? = null,
)

@Serializable
data class Narration(
    val url: String,
    val durationSeconds: Double,
    val voice: String? = null,
)

@Serializable
data class VocabItem(
    val fa: String,
    val translit: String,
    val en: String,
    val context: String,
    val audioUrl: String? = null,
)

@Serializable
data class Couplet(
    val fa: String,
    val en: String,
)