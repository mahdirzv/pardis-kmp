package app.pardis.shared.analytics

/**
 * Simple analytics/telemetry stub.
 * Platform implementations provided via DI (no-op by default for now).
 * Call from VMs or shells for key events. Keep properties non-PII.
 */
interface Analytics {
    fun track(event: String, properties: Map<String, Any> = emptyMap())
}

/**
 * No-op default implementation.
 */
class NoOpAnalytics : Analytics {
    override fun track(event: String, properties: Map<String, Any>) {
        // TODO in Phase 3+: log or send to platform (Firebase etc.)
    }
}