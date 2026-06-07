package app.pardis.core.model

import kotlinx.serialization.Serializable

/**
 * A child reader profile. Demo roster for now (no backend profile system yet); exposed
 * through the shared contract so the picker, You card, and greeting share one source.
 */
@Serializable
data class ChildProfile(
    val id: String,
    val name: String,
    val tone: ProfileTone,
    val age: Int,
    val streak: Int,
)

/** Accent family for a profile. Mapped to PardisColors in the UI layer — no raw colors here. */
@Serializable
enum class ProfileTone { Saffron, Lapis, Lilac }

/** Static demo roster, mirroring the v2 design's data.js `profiles`. */
val pardisProfiles: List<ChildProfile> = listOf(
    ChildProfile(id = "roya", name = "Roya", tone = ProfileTone.Saffron, age = 7, streak = 7),
    ChildProfile(id = "darius", name = "Darius", tone = ProfileTone.Lapis, age = 9, streak = 3),
    ChildProfile(id = "mina", name = "Mina", tone = ProfileTone.Lilac, age = 5, streak = 0),
)
