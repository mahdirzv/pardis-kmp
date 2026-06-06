package app.pardis.shared.offline

/** Human-friendly size: "<NNN KB" under 1 MB, "NNN MB" under 1 GB, else "N.N GB". */
fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 MB"
    val mb = bytes / (1024.0 * 1024.0)
    return when {
        mb < 1.0 -> "${(bytes / 1024.0).toInt()} KB"
        mb < 1024.0 -> "${mb.toInt()} MB"
        else -> {
            val gbTenths = (mb / 1024.0 * 10).toInt()
            "${gbTenths / 10}.${gbTenths % 10} GB"
        }
    }
}
