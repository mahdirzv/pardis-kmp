package app.pardis.core.model

/**
 * Static Rivana demo content (lullabies, characters, badges, words) shared by the native
 * Android and iOS screens until backed by real shared state. Platform-neutral: icons are a
 * semantic [RivanaBadgeIcon] enum resolved to platform icon kinds in each UI layer (mirroring
 * how [ProfileTone] is resolved to colors). No platform UI types here (kmpSkill §3).
 */

/** Semantic badge icon. Each platform maps this to its own icon set. */
enum class RivanaBadgeIcon { Compass, Flame, Feather, Crown, Moon, Mic }

data class Lullaby(
    val title: String,
    val titleFa: String,
    val minutes: Int,
    val origin: String,
    val sceneVariant: Int,
    val plays: String,
)

/** A Shahnameh figure for the Character screen. No character model yet; from the design roster. */
data class RivanaCharacter(
    val name: String,
    val nameFa: String,
    val role: String,
    val bio: String,
    val sceneVariant: Int,
    val tone: String,
    val collected: Boolean,
    val appearances: Int,
)

data class RivanaBadge(
    val label: String,
    val desc: String,
    val icon: RivanaBadgeIcon,
    val tone: String,
    val earned: Boolean,
    val progress: Float = 0f,
)

data class RivanaWord(val fa: String, val tr: String, val en: String, val mastery: Float)

/**
 * Single source of the static roster for both platforms. Exposed as an `object` (rather than
 * top-level `val`s) so iOS can reach it predictably as `RivanaContent.shared.*` through the
 * Kotlin framework; Android uses `RivanaContent.*`.
 */
object RivanaContent {
    val lullabies: List<Lullaby> = listOf(
        Lullaby("Moon Over Damavand", "ماه بر فرازِ دماوند", 18, "Traditional · Mazandaran", 6, "2.1k"),
        Lullaby("Laay Laay, Little Star", "لای‌لای، ستاره‌ی کوچک", 12, "Folk lullaby", 0, "4.8k"),
        Lullaby("The Sleepy River", "رودِ خواب‌آلود", 22, "Original · Rivana", 6, "1.3k"),
        Lullaby("Garden of Dreams", "باغِ رؤیاها", 15, "Traditional · Shiraz", 0, "3.6k"),
    )

    val characters: List<RivanaCharacter> = listOf(
        RivanaCharacter("Rostam", "رستم", "Champion of Persia", "The mightiest of all Persian heroes — strong as a mountain, loyal to his king, and rider of the great horse Rakhsh.", 1, "saffron", true, 3),
        RivanaCharacter("Simurgh", "سیمرغ", "The Wise Bird", "A vast, kind bird whose feathers glow like dawn. She heals the wounded and raises lost children on Mount Damavand.", 3, "lilac", true, 2),
        RivanaCharacter("Anahita", "آناهیتا", "Keeper of Waters", "The radiant guardian of rivers, rain, and wisdom — bringer of life to every field in Persia.", 0, "lapis", true, 1),
        RivanaCharacter("Kaveh", "کاوه", "The Blacksmith", "An ordinary blacksmith whose courage sparked a revolt and whose apron became the banner of a free Persia.", 5, "rose", true, 1),
        RivanaCharacter("Zal", "زال", "Child of the Simurgh", "Born with snow-white hair and raised by the Simurgh, Zal grows into a wise and gentle prince.", 3, "sun", false, 1),
        RivanaCharacter("Sindbad", "سندباد", "The Sea Traveller", "A curious voyager who sails toward every horizon and returns with tales of wonder.", 4, "mint", true, 1),
    )

    val badges: List<RivanaBadge> = listOf(
        RivanaBadge("First Voyage", "Finished your first story", RivanaBadgeIcon.Compass, "mint", true),
        RivanaBadge("7-Night Streak", "Read 7 nights in a row", RivanaBadgeIcon.Flame, "saffron", true),
        RivanaBadge("Word Collector", "Learned 25 Persian words", RivanaBadgeIcon.Feather, "lapis", true),
        RivanaBadge("Hero of Persia", "Met 5 Shahnameh heroes", RivanaBadgeIcon.Crown, "lilac", false, 0.6f),
        RivanaBadge("Night Owl", "Listen to 10 lullabies", RivanaBadgeIcon.Moon, "lilac", false, 0.4f),
        RivanaBadge("Storyteller", "Read aloud 3 times", RivanaBadgeIcon.Mic, "rose", false, 0.33f),
    )

    val words: List<RivanaWord> = listOf(
        RivanaWord("پهلوان", "pahlavân", "champion", 1f), RivanaWord("شیر", "shir", "lion", 1f), RivanaWord("ستاره", "setâre", "star", 0.66f),
        RivanaWord("آتش", "âtash", "fire", 0.66f), RivanaWord("پرنده", "parande", "bird", 1f), RivanaWord("دریا", "daryâ", "sea", 0.33f),
        RivanaWord("آب", "âb", "water", 1f), RivanaWord("کوه", "kuh", "mountain", 0.33f), RivanaWord("آسمان", "âsemân", "sky", 0.66f),
    )
}
