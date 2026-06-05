package app.pardis.core.data

import app.pardis.core.domain.DownloadVideoUseCase
import app.pardis.core.domain.VideoCache

class DownloadVideoUseCaseImpl(
    private val videoCache: VideoCache
) : DownloadVideoUseCase {
    override suspend fun invoke(slug: String, lang: String, remoteUrl: String): String? =
        videoCache.downloadVideoIfNeeded(slug, lang, remoteUrl)
}
