package app.pardis.core.data

import app.pardis.core.domain.DownloadVideoUseCase
import app.pardis.core.domain.OfflineAssetCache

class DownloadVideoUseCaseImpl(
    private val assetCache: OfflineAssetCache
) : DownloadVideoUseCase {
    override suspend fun invoke(slug: String, lang: String, remoteUrl: String): String? =
        assetCache.downloadAssetIfNeeded(slug, "video", lang, remoteUrl)
}
