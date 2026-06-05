package app.pardis.core.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual fun provideSqlDriver(context: Any?): SqlDriver {
    val ctx = (context as? Context) ?: throw IllegalStateException("provideSqlDriver requires Android Context")
    return AndroidSqliteDriver(PardisDatabase.Schema, ctx, "pardis.db")
}