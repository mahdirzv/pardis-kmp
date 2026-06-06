package app.pardis.core.data

import app.pardis.core.domain.ClearStoryAssetsUseCase
import app.pardis.core.domain.OfflineAssetCache

class ClearStoryAssetsUseCaseImpl(
    private val assetCache: OfflineAssetCache
) : ClearStoryAssetsUseCase {
    override suspend fun invoke(slug: String) {
        assetCache.clearAssetsForStory(slug)
    }
}