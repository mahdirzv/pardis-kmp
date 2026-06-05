package app.pardis.core.data

import app.pardis.core.domain.GetLocalVideoPathUseCase
import app.pardis.core.domain.VideoCache

class GetLocalVideoPathUseCaseImpl(
    private val videoCache: VideoCache
) : GetLocalVideoPathUseCase {
    override suspend fun invoke(slug: String, lang: String): String? =
        videoCache.getLocalVideoPath(slug, lang)
}
