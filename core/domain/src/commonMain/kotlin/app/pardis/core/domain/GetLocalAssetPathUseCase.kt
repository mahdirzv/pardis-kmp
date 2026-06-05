package app.pardis.core.domain

/**
 * Use case to get a locally cached asset path for a story asset.
 * kind examples: "video", "illustration", "narration"
 * subKey: for video "fa"/"en", for illustration page number string, for narration "fa-5" etc.
 */
interface GetLocalAssetPathUseCase {
    suspend operator fun invoke(slug: String, kind: String, subKey: String = ""): String?
}

class GetLocalAssetPathUseCaseImpl(
    private val assetCache: OfflineAssetCache
) : GetLocalAssetPathUseCase {
    override suspend fun invoke(slug: String, kind: String, subKey: String): String? =
        assetCache.getLocalAssetPath(slug, kind, subKey)
}