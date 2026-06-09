package app.pardis.shared.reader

import app.pardis.core.model.Bookend
import app.pardis.core.model.BookendAudio
import app.pardis.core.model.Narration
import app.pardis.core.model.Story
import app.pardis.core.model.StoryPage
import kotlin.test.Test
import kotlin.test.assertEquals

private fun story(
    videoUrlFa: String? = null,
    videoUrlEn: String? = null,
    introAudio: Bookend? = null,
    outroAudio: Bookend? = null,
) = Story(
    slug = "s",
    titleEn = "T",
    titleFa = "ت",
    ageBand = "7–10",
    minutes = 5,
    pageCount = 2,
    vocabCount = 0,
    videoUrlFa = videoUrlFa,
    videoUrlEn = videoUrlEn,
    introAudio = introAudio,
    outroAudio = outroAudio,
)

private fun page(p: Int, faSec: Double?, enSec: Double?) = StoryPage(
    page = p,
    narrationFa = faSec?.let { Narration(url = "fa", durationSeconds = it) },
    narrationEn = enSec?.let { Narration(url = "en", durationSeconds = it) },
)

class BuildVideoCueTimelineTest {

    @Test
    fun usesFaDurations_whenFaVideoPresent_notTheAverage() {
        val pages = listOf(page(1, faSec = 3.0, enSec = 9.0), page(2, faSec = 4.0, enSec = 10.0))
        val timeline = buildVideoCueTimeline(story(videoUrlFa = "fa.mp4", videoUrlEn = "en.mp4"), pages)

        // Two page cues, sequential, built from the Fa track (3.0, 4.0) — NOT the avg (6.0, 7.0).
        assertEquals(2, timeline.cues.size)
        assertEquals(0.0, timeline.cues[0].startSec)
        assertEquals(3.0, timeline.cues[0].endSec)
        assertEquals(3.0, timeline.cues[1].startSec)
        assertEquals(7.0, timeline.cues[1].endSec)
    }

    @Test
    fun usesEnDurations_whenNoFaVideo() {
        val pages = listOf(page(1, faSec = 3.0, enSec = 9.0))
        val timeline = buildVideoCueTimeline(story(videoUrlFa = null, videoUrlEn = "en.mp4"), pages)

        assertEquals(1, timeline.cues.size)
        assertEquals(9.0, timeline.cues[0].endSec) // En track, not avg (6.0)
    }

    @Test
    fun fallsBackToOtherTrack_whenChosenLangDurationMissing() {
        // Fa video plays, but this page only has an En narration duration → use it rather than drop.
        val pages = listOf(page(1, faSec = null, enSec = 5.0))
        val timeline = buildVideoCueTimeline(story(videoUrlFa = "fa.mp4"), pages)

        assertEquals(1, timeline.cues.size)
        assertEquals(5.0, timeline.cues[0].endSec)
    }

    @Test
    fun intro_and_outro_bracket_the_pages_usingChosenLang() {
        val intro = Bookend(fa = BookendAudio(durationSeconds = 2.0), en = BookendAudio(durationSeconds = 8.0))
        val outro = Bookend(fa = BookendAudio(durationSeconds = 1.0), en = BookendAudio(durationSeconds = 7.0))
        val pages = listOf(page(1, faSec = 3.0, enSec = 9.0))
        val timeline = buildVideoCueTimeline(
            story(videoUrlFa = "fa.mp4", introAudio = intro, outroAudio = outro),
            pages,
        )

        assertEquals(2.0, timeline.introDuration)
        assertEquals(1.0, timeline.outroDuration)
        // intro [0,2] → page [2,5] → outro [5,6], all from Fa durations.
        assertEquals(3, timeline.cues.size)
        assertEquals(0.0, timeline.cues[0].startSec)
        assertEquals(2.0, timeline.cues[0].endSec)
        assertEquals(2.0, timeline.cues[1].startSec)
        assertEquals(5.0, timeline.cues[1].endSec)
        assertEquals(5.0, timeline.cues[2].startSec)
        assertEquals(6.0, timeline.cues[2].endSec)
    }
}
