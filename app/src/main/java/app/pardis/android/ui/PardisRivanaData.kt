/**
 * Static Rivana demo data (lullabies, characters, badges, words) shared across the native
 * screens until backed by real shared state.
 */
package app.pardis.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue

internal data class Lullaby(
    val title: String,
    val titleFa: String,
    val minutes: Int,
    val origin: String,
    val sceneVariant: Int,
    val plays: String,
)

internal val rivanaLullabies = listOf(
    Lullaby("Moon Over Damavand", "ماه بر فرازِ دماوند", 18, "Traditional · Mazandaran", 6, "2.1k"),
    Lullaby("Laay Laay, Little Star", "لای‌لای، ستاره‌ی کوچک", 12, "Folk lullaby", 0, "4.8k"),
    Lullaby("The Sleepy River", "رودِ خواب‌آلود", 22, "Original · Rivana", 6, "1.3k"),
    Lullaby("Garden of Dreams", "باغِ رؤیاها", 15, "Traditional · Shiraz", 0, "3.6k"),
)

/**
 * Static cast of Shahnameh figures for the Character screen. The app has no character
 * model yet, so this content is hardcoded from the design's roster (data.js).
 */
internal data class RivanaCharacter(
    val name: String,
    val nameFa: String,
    val role: String,
    val bio: String,
    val sceneVariant: Int,
    val tone: String,
    val collected: Boolean,
    val appearances: Int,
)

internal val rivanaCharacters = listOf(
    RivanaCharacter("Rostam", "رستم", "Champion of Persia", "The mightiest of all Persian heroes — strong as a mountain, loyal to his king, and rider of the great horse Rakhsh.", 1, "saffron", true, 3),
    RivanaCharacter("Simurgh", "سیمرغ", "The Wise Bird", "A vast, kind bird whose feathers glow like dawn. She heals the wounded and raises lost children on Mount Damavand.", 3, "lilac", true, 2),
    RivanaCharacter("Anahita", "آناهیتا", "Keeper of Waters", "The radiant guardian of rivers, rain, and wisdom — bringer of life to every field in Persia.", 0, "lapis", true, 1),
    RivanaCharacter("Kaveh", "کاوه", "The Blacksmith", "An ordinary blacksmith whose courage sparked a revolt and whose apron became the banner of a free Persia.", 5, "rose", true, 1),
    RivanaCharacter("Zal", "زال", "Child of the Simurgh", "Born with snow-white hair and raised by the Simurgh, Zal grows into a wise and gentle prince.", 3, "sun", false, 1),
    RivanaCharacter("Sindbad", "سندباد", "The Sea Traveller", "A curious voyager who sails toward every horizon and returns with tales of wonder.", 4, "mint", true, 1),
)

internal data class RBadge(val label: String, val desc: String, val icon: PardisIconKind, val tone: String, val earned: Boolean, val progress: Float = 0f)

internal val rivanaBadges = listOf(
    RBadge("First Voyage", "Finished your first story", PardisIconKind.Compass, "mint", true),
    RBadge("7-Night Streak", "Read 7 nights in a row", PardisIconKind.Flame, "saffron", true),
    RBadge("Word Collector", "Learned 25 Persian words", PardisIconKind.Feather, "lapis", true),
    RBadge("Hero of Persia", "Met 5 Shahnameh heroes", PardisIconKind.Crown, "lilac", false, 0.6f),
    RBadge("Night Owl", "Listen to 10 lullabies", PardisIconKind.Moon, "lilac", false, 0.4f),
    RBadge("Storyteller", "Read aloud 3 times", PardisIconKind.Mic, "rose", false, 0.33f),
)

internal data class RWord(val fa: String, val tr: String, val en: String, val mastery: Float)

internal val rivanaWords = listOf(
    RWord("پهلوان", "pahlavân", "champion", 1f), RWord("شیر", "shir", "lion", 1f), RWord("ستاره", "setâre", "star", 0.66f),
    RWord("آتش", "âtash", "fire", 0.66f), RWord("پرنده", "parande", "bird", 1f), RWord("دریا", "daryâ", "sea", 0.33f),
    RWord("آب", "âb", "water", 1f), RWord("کوه", "kuh", "mountain", 0.33f), RWord("آسمان", "âsemân", "sky", 0.66f),
)
