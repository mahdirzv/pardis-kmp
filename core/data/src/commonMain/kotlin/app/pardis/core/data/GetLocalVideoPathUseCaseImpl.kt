package app.pardis.core.data

import app.pardis.core.domain.GetLocalVideoPathUseCase
import app.pardis.core.domain.OfflineAssetCache

class GetLocalVideoPathUseCaseImpl(
    private val assetCache: OfflineAssetCache
) : GetLocalVideoPathUseCase {
    override suspend fun invoke(slug: String, lang: String): String? =
        assetCache.getLocalAssetPath(slug, "video", lang)
}
