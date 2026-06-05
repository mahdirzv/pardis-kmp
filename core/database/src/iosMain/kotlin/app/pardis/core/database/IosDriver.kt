package app.pardis.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual fun provideSqlDriver(context: Any?): SqlDriver {
    // Migrations are handled automatically by SQLDelight when .sqm files are present
    // (the schema version is derived from migration files).
    return NativeSqliteDriver(PardisDatabase.Schema, "pardis.db")
}