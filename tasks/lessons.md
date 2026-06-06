# Lessons

- Koin `single { ... }` providers in shared/common code must never return `null`. On iOS this caused a startup abort when the optional SQLDelight driver was absent and the `PardisDatabase?` singleton returned no value. For optional platform services, resolve the optional dependency inside the consumer provider instead of registering a nullable singleton.
- When the exported iOS `Shared` framework starts using SQLDelight's native driver, the framework target must link the system sqlite library (`-lsqlite3`) or the Kotlin/Native link step fails with unresolved `sqlite3_*` symbols even though Kotlin compilation succeeds.
- For an English-first shell, do not force global RTL at the app root. Keep the platform default layout direction and handle Farsi/RTL presentation only where the specific screen or text block actually needs it.
- Reader RTL should be scoped to the Persian text blocks themselves. Keeping navigation, controls, and English content LTR avoids flipping the whole screen while still rendering FA paragraphs correctly.
- Apply the same scoped RTL treatment to inline Persian reader elements (titles, vocab words, detail sheets) so FA content stays visually consistent without making surrounding English metadata or controls RTL.

