package app.pardis.core.domain

/**
 * Thin use case to check for a locally cached video file path (for offline player).
 * lang: "fa" (preferred) or "en".
 */
interface GetLocalVideoPathUseCase {
    suspend operator fun invoke(slug: String, lang: String): String?
}
