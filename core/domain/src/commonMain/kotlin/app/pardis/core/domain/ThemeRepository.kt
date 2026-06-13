package app.pardis.core.domain

/** The user's theme choice. [SYSTEM] defers to the OS appearance; the default until the user picks. */
enum class ThemePreference { LIGHT, DARK, SYSTEM }

interface ThemeRepository {
    suspend fun themePreference(): ThemePreference
    suspend fun setThemePreference(preference: ThemePreference)
}

/** Reads the persisted theme preference (SYSTEM when none stored). */
interface GetThemePreferenceUseCase {
    suspend operator fun invoke(): ThemePreference
}

/** Persists the theme preference. */
interface SetThemePreferenceUseCase {
    suspend operator fun invoke(preference: ThemePreference)
}
