package app.pardis.core.data

import app.pardis.core.domain.GetCachedSizeUseCase
import app.pardis.core.domain.OfflineAssetCache

class GetCachedSizeUseCaseImpl(
    private val assetCache: OfflineAssetCache,
) : GetCachedSizeUseCase {
    override suspend fun invoke(slug: String): Long = assetCache.getCachedSizeBytes(slug)
}
