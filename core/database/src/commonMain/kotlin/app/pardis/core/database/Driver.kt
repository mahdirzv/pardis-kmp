package app.pardis.core.database

import app.cash.sqldelight.db.SqlDriver

/**
 * Expect for platform SQLDelight driver.
 * Pass Android Context on Android; ignored on iOS.
 */
expect fun provideSqlDriver(context: Any? = null): SqlDriver

/** Factory to build the generated DB once driver is available. */
fun createPardisDatabase(driver: SqlDriver): PardisDatabase = PardisDatabase(driver)