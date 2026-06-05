package app.pardis.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual fun provideSqlDriver(context: Any?): SqlDriver {
    // Use Callback so that SQLDelight migrations (in migrations/ folder) are applied
    // when the schema changes (e.g. adding cached_pages table).
    return NativeSqliteDriver(
        schema = PardisDatabase.Schema,
        name = "pardis.db",
        callback = NativeSqliteDriver.Callback(PardisDatabase.Schema)
    )
}