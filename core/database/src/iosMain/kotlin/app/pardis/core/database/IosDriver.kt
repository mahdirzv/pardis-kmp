package app.pardis.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual fun provideSqlDriver(context: Any?): SqlDriver {
    return NativeSqliteDriver(PardisDatabase.Schema, "pardis.db")
}