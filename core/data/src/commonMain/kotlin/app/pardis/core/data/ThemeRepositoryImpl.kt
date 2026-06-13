package app.pardis.core.data

import app.pardis.core.database.PardisDatabase
import app.pardis.core.domain.GetThemePreferenceUseCase
import app.pardis.core.domain.SetThemePreferenceUseCase
import app.pardis.core.domain.ThemePreference
import app.pardis.core.domain.ThemeRepository
import kotlin.concurrent.Volatile

private const val KEY_THEME_PREFERENCE = "theme_preference"

/**
 * Theme-preference repository. Persists via the generic app_setting table when a SQLDelight driver
 * is present, with an in-memory fallback when not — mirrors [ProfileRepositoryImpl].
 */
class ThemeRepositoryImpl(
    private val db: PardisDatabase? = null,
) : ThemeRepository {

    // Repo is a Koin singleton; @Volatile makes the in-memory fallback's writes visible across
    // coroutine dispatch threads (single-writer/single-reader).
    @Volatile
    private var inMemoryPreference: ThemePreference = ThemePreference.SYSTEM

    override suspend fun themePreference(): ThemePreference {
        val stored = db?.pardisQueries?.getSetting(KEY_THEME_PREFERENCE)?.executeAsOneOrNull()
        return stored?.let(::parse) ?: inMemoryPreference
    }

    override suspend fun setThemePreference(preference: ThemePreference) {
        inMemoryPreference = preference
        db?.pardisQueries?.setSetting(KEY_THEME_PREFERENCE, preference.name)
    }

    // Unknown/garbled stored values fall back to SYSTEM rather than throwing.
    private fun parse(value: String): ThemePreference =
        ThemePreference.values().firstOrNull { it.name == value } ?: ThemePreference.SYSTEM
}

class GetThemePreferenceUseCaseImpl(
    private val repository: ThemeRepository,
) : GetThemePreferenceUseCase {
    override suspend fun invoke(): ThemePreference = repository.themePreference()
}

class SetThemePreferenceUseCaseImpl(
    private val repository: ThemeRepository,
) : SetThemePreferenceUseCase {
    override suspend fun invoke(preference: ThemePreference) = repository.setThemePreference(preference)
}
