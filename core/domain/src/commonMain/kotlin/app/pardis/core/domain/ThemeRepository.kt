package app.pardis.core.domain

import app.pardis.core.model.ThemePreference

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
