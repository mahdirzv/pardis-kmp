package app.pardis.core.data

import app.pardis.core.domain.GetLocalAssetPathUseCase
import app.pardis.core.domain.OfflineAssetCache

class GetLocalAssetPathUseCaseImpl(
    private val assetCache: OfflineAssetCache
) : GetLocalAssetPathUseCase {
    override suspend fun invoke(slug: String, kind: String, subKey: String): String? =
        assetCache.getLocalAssetPath(slug, kind, subKey)
}