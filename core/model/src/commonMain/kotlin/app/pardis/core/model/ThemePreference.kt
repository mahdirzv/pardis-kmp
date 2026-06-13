package app.pardis.core.model

/**
 * The user's theme choice. [SYSTEM] defers to the OS appearance; the default until the user picks.
 * Lives in core:model (not core:domain) so it's exported in the iOS Shared.framework public API.
 */
enum class ThemePreference { LIGHT, DARK, SYSTEM }
