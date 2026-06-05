package app.pardis.core.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual fun provideSqlDriver(context: Any?): SqlDriver {
    val ctx = (context as? Context) ?: throw IllegalStateException("provideSqlDriver requires Android Context")
    // Use Callback so that SQLDelight migrations (in migrations/ folder) are applied
    // when the schema changes (e.g. adding cached_pages table).
    return AndroidSqliteDriver(
        schema = PardisDatabase.Schema,
        context = ctx,
        name = "pardis.db",
        callback = AndroidSqliteDriver.Callback(PardisDatabase.Schema)
    )
}