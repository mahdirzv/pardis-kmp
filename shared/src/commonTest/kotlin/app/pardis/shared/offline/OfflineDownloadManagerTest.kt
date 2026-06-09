package app.pardis.shared.offline

import app.pardis.core.domain.ClearStoryAssetsUseCase
import app.pardis.core.domain.DownloadStoryAssetsUseCase
import app.pardis.core.domain.GetCachedSizeUseCase
import app.pardis.core.domain.StoryAssetsResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun fullSuccess(succeeded: Int = 3, total: Int = 3) =
    StoryAssetsResult(hadVideo = true, videoCached = true, total = total, succeeded = succeeded)

private class FakeDownloadStoryAssets(
    var result: StoryAssetsResult = fullSuccess(),
    private val progresses: List<String> = emptyList(),
    private val hangAfterProgress: Boolean = false,
) : DownloadStoryAssetsUseCase {
    override suspend fun invoke(
        slug: String,
        preferVideoLang: String,
        onProgress: (String) -> Unit,
    ): StoryAssetsResult {
        progresses.forEach { onProgress(it) }
        if (hangAfterProgress) awaitCancellation()
        return result
    }
}

private class FakeClear : ClearStoryAssetsUseCase {
    val cleared = mutableListOf<String>()
    override suspend fun invoke(slug: String) { cleared.add(slug) }
}

private class FakeSize(var size: Long = 0L) : GetCachedSizeUseCase {
    override suspend fun invoke(slug: String): Long = size
}

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineDownloadManagerTest {

    @Test
    fun download_success_setsDownloadedWithCachedSize() = runTest {
        val mgr = OfflineDownloadManager(
            downloadStoryAssets = FakeDownloadStoryAssets(result = fullSuccess()),
            clearStoryAssets = FakeClear(),
            getCachedSize = FakeSize(size = 2048L),
            scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
        )

        mgr.download("s1")
        advanceUntilIdle()

        assertEquals(StoryDownloadState.Downloaded(2048L), mgr.states.value["s1"])
    }

    @Test
    fun download_totalFailure_setsFailed() = runTest {
        val mgr = OfflineDownloadManager(
            downloadStoryAssets = FakeDownloadStoryAssets(result = fullSuccess(succeeded = 0)),
            clearStoryAssets = FakeClear(),
            getCachedSize = FakeSize(size = 0L),
            scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
        )

        mgr.download("s1")
        advanceUntilIdle()

        assertTrue(mgr.states.value["s1"] is StoryDownloadState.Failed)
    }

    @Test
    fun download_surfacesProgress_whileInFlight() = runTest {
        val mgr = OfflineDownloadManager(
            downloadStoryAssets = FakeDownloadStoryAssets(
                progresses = listOf("Downloaded 1/3", "Downloaded 2/3"),
                hangAfterProgress = true,
            ),
            clearStoryAssets = FakeClear(),
            getCachedSize = FakeSize(),
            scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
        )

        mgr.download("s1")
        advanceUntilIdle()

        // Last progress emitted is reflected; the job is still running (hung), so not terminal.
        assertEquals(StoryDownloadState.Downloading("Downloaded 2/3"), mgr.states.value["s1"])
    }

    @Test
    fun cancel_clearsAssets_andResetsState_evenForInFlightDownload() = runTest {
        val clear = FakeClear()
        val mgr = OfflineDownloadManager(
            downloadStoryAssets = FakeDownloadStoryAssets(hangAfterProgress = true),
            clearStoryAssets = clear,
            getCachedSize = FakeSize(),
            scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
        )

        mgr.download("s1")
        advanceUntilIdle()
        mgr.cancel("s1")
        advanceUntilIdle()

        assertEquals(StoryDownloadState.NotDownloaded, mgr.states.value["s1"])
        assertTrue(clear.cleared.contains("s1"))
    }

    @Test
    fun refreshState_reflectsOnDiskSize() = runTest {
        // refreshState hops to Dispatchers.Main internally, so provide a test Main.
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        try {
            val size = FakeSize(size = 0L)
            val mgr = OfflineDownloadManager(
                downloadStoryAssets = FakeDownloadStoryAssets(),
                clearStoryAssets = FakeClear(),
                getCachedSize = size,
                scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
            )

            mgr.refreshState(listOf("s1"))
            assertEquals(StoryDownloadState.NotDownloaded, mgr.states.value["s1"])

            size.size = 4096L
            mgr.refreshState(listOf("s1"))
            assertEquals(StoryDownloadState.Downloaded(4096L), mgr.states.value["s1"])
        } finally {
            Dispatchers.resetMain()
        }
    }
}
