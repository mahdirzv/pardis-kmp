package app.pardis.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pardis.design.PardisColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.milliseconds
import app.pardis.design.PardisSpacing
import app.pardis.design.PardisTheme
import app.pardis.design.pardisScreenBackground
import app.pardis.shared.library.LibraryAction
import app.pardis.shared.library.LibraryUiState
import app.pardis.shared.library.LibraryViewModel
import app.pardis.shared.reader.ReaderAction
import app.pardis.shared.reader.ReaderUiState
import app.pardis.shared.reader.ReaderViewModel
import coil.imageLoader
import coil.request.ImageRequest
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import android.media.MediaPlayer
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PardisApp() {
    PardisTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "library") {
                composable("library") {
                    LibraryRoute(
                        onOpenStory = { slug ->
                            navController.navigate("reader/$slug")
                        }
                    )
                }
                composable(
                    "reader/{slug}",
                    arguments = listOf(navArgument("slug") { type = NavType.StringType })
                ) { backStackEntry ->
                    val slug = backStackEntry.arguments?.getString("slug") ?: ""
                    ReaderRoute(
                        slug = slug,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
fun LibraryRoute(
    onOpenStory: (String) -> Unit,
    viewModel: LibraryViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LibraryScreen(
        state = state,
        onAction = viewModel::onAction,
        onOpenStory = onOpenStory,
    )
}

@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onAction: (LibraryAction) -> Unit,
    onOpenStory: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .pardisScreenBackground()
            .padding(PardisSpacing.md)
    ) {
        PardisScreenHeader(
            title = "Pardis",
            subtitle = "Persian heritage stories",
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(PardisSpacing.sm))
        PardisMetricStrip(
            metrics = listOf(
                PardisMetric(
                    value = state.stories.size.toString(),
                    label = "Stories",
                    tone = PardisMetricTone.Saffron,
                ),
                PardisMetric(
                    value = state.ageBands.size.toString(),
                    label = "Age bands",
                    tone = PardisMetricTone.Indigo,
                ),
                PardisMetric(
                    value = state.cachedStorySlugs.size.toString(),
                    label = if (state.totalCachedLabel.isNotEmpty()) state.totalCachedLabel else "Offline",
                    tone = PardisMetricTone.Mint,
                ),
            ),
        )
        Spacer(Modifier.height(PardisSpacing.sm))
        state.stories.firstOrNull()?.let { story ->
            PardisFeaturedStoryCard(
                titleEn = story.titleEn,
                titleFa = story.titleFa,
                ageBand = story.ageBand,
                minutes = story.minutes,
                vocabCount = story.vocabCount,
                coverUrl = state.localCoverUrls[story.slug] ?: story.coverUrl,
                blurb = story.blurbEn,
                onOpen = { onOpenStory(story.slug) },
            )
            Spacer(Modifier.height(PardisSpacing.sm))
        }
        PardisPanel {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onAction(LibraryAction.Search(query = it)) },
                label = { Text("Search stories") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        Spacer(Modifier.height(PardisSpacing.sm))
        // Age-band filter chips (derived from the data). Tapping the active band again clears it.
        if (state.ageBands.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(PardisSpacing.xs)
            ) {
                PardisFilterPill(
                    label = "All ages",
                    selected = state.selectedAgeBand == null,
                    onClick = { onAction(LibraryAction.SetAgeBand(null)) },
                )
                state.ageBands.forEach { band ->
                    PardisFilterPill(
                        label = band,
                        selected = state.selectedAgeBand == band,
                        onClick = {
                            onAction(LibraryAction.SetAgeBand(if (state.selectedAgeBand == band) null else band))
                        },
                    )
                }
            }
            Spacer(Modifier.height(PardisSpacing.sm))
        }
        // Toggle for offline cached only
        PardisPanel(contentPadding = PaddingValues(horizontal = PardisSpacing.md, vertical = PardisSpacing.sm)) {
            Button(onClick = { onAction(LibraryAction.ToggleShowOnlyCached) }) {
                Text(if (state.showOnlyCached) "Show all stories" else "Show only offline cached")
            }
            if (state.totalCachedLabel.isNotEmpty()) {
                Text(
                    "Cached offline: ${state.totalCachedLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = PardisColors.inkSoft,
                )
            }
        }
        Spacer(Modifier.height(PardisSpacing.md))
        PardisSectionHeader(
            title = "Stories",
            subtitle = if (state.selectedAgeBand == null) "All available stories" else "Filtered for ${state.selectedAgeBand}",
            actionLabel = "Refresh",
            onAction = { onAction(LibraryAction.Refresh) },
        )
        Spacer(Modifier.height(PardisSpacing.sm))

        if (state.isLoading && state.stories.isEmpty()) {
            CircularProgressIndicator(color = PardisColors.saffron)
        }

        state.errorMessage?.let { err ->
            Text("Error: $err", color = MaterialTheme.colorScheme.error)
            Button(onClick = { onAction(LibraryAction.Refresh) }) { Text("Retry") }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(PardisSpacing.sm)
        ) {
            items(state.stories, key = { it.slug }) { story ->
                PardisStoryCard(
                    titleEn = story.titleEn,
                    titleFa = story.titleFa,
                    ageBand = story.ageBand,
                    minutes = story.minutes,
                    vocabCount = story.vocabCount,
                    coverUrl = state.localCoverUrls[story.slug] ?: story.coverUrl,
                    downloadProgress = state.downloadProgress[story.slug],
                    downloadedSizeLabel = state.downloadedSizeLabels[story.slug],
                    isFailed = state.failedDownloads.contains(story.slug),
                    onClick = { onOpenStory(story.slug) },
                    onDownload = { onAction(LibraryAction.DownloadStory(story.slug)) },
                    onCancel = { onAction(LibraryAction.CancelDownload(story.slug)) },
                    onRemove = { onAction(LibraryAction.RemoveDownload(story.slug)) },
                )
            }
        }
    }
}

@Composable
fun ReaderRoute(
    slug: String,
    onBack: () -> Unit,
    viewModel: ReaderViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Load when slug changes (Route owns the VM and triggers load)
    LaunchedEffect(slug) {
        viewModel.onAction(ReaderAction.LoadStory(slug))
    }

    // Basic prefetch for next illustration (performance, using Coil) - prefer local cached if available
    val context = LocalContext.current
    LaunchedEffect(state.currentPage, state.pages, state.localIllustrationUrls) {
        val nextPage = state.pages.getOrNull(state.currentPage + 1)
        val url = nextPage?.let { state.localIllustrationUrls[it.page] ?: it.illustrationUrl }
        url?.let {
            val request = ImageRequest.Builder(context)
                .data(it)
                .build()
            context.imageLoader.enqueue(request)
        }
    }

    ReaderScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack,
    )
}

@Composable
fun ReaderScreen(
    state: ReaderUiState,
    onAction: (ReaderAction) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .pardisScreenBackground()
            .padding(PardisSpacing.md)
    ) {
        val hasOfflineAssets = state.localVideoUrlFa != null || state.localVideoUrlEn != null || state.localIllustrationUrls.isNotEmpty() || state.localNarrationUrls.isNotEmpty()
        PardisReaderHeaderBar(
            onBack = onBack,
            pageLabel = if (state.pages.isNotEmpty()) "${state.currentPage + 1} / ${state.pages.size}" else "Reader",
            isOffline = hasOfflineAssets,
            backLabel = "← Library",
            offlineLabel = "Offline",
        )
        Spacer(Modifier.height(PardisSpacing.sm))

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PardisColors.saffron)
                }
            }
            state.errorMessage != null -> {
                Text("Error: ${state.errorMessage}", color = MaterialTheme.colorScheme.error)
                Button(onClick = { onAction(ReaderAction.LoadStory(state.storySlug)) }) { Text("Retry") }
            }
            state.pages.isEmpty() -> {
                Text("No pages loaded for ${state.storySlug}")
            }
            else -> {
                val page = state.pages.getOrNull(state.currentPage) ?: state.pages.first()
                // Prefer locally cached video file (offline video support) over remote Supabase URL.
                // The local paths are absolute file paths from OfflineAssetCache (Android cacheDir/pardis/assets/...).
                val videoUrl = if (state.isVideoMode) {
                    (state.localVideoUrlFa ?: state.localVideoUrlEn ?: state.videoUrlFa ?: state.videoUrlEn)
                } else null
                val context = LocalContext.current

                // Stable ExoPlayer instance for the reader session (created once, reused across text<->video toggles and remote->local after cache).
                // Updating via setMediaItem avoids full release/create which triggers noisy MediaCodec/BufferQueue detach/cancel logs.
                val exoPlayer = remember {
                    ExoPlayer.Builder(context).build().apply {
                        addListener(object : Player.Listener {
                            override fun onPlaybackStateChanged(playbackState: Int) {
                                if (playbackState == Player.STATE_ENDED) {
                                    // Video finished - go to last page / end of story
                                    onAction(ReaderAction.GoToPage(state.pages.lastIndex.coerceAtLeast(0)))
                                }
                            }
                        })
                    }
                }

                // React to effective video source (remote or local file path) changes: set on player (create once).
                // Also handles initial entry into video mode, and switch after successful "Cache video + assets".
                LaunchedEffect(videoUrl) {
                    if (videoUrl != null) {
                        exoPlayer.setMediaItem(MediaItem.fromUri(videoUrl))
                        exoPlayer.prepare()
                        exoPlayer.playWhenReady = true
                    } else {
                        exoPlayer.pause()
                    }
                }

                // Pause decode during explicit asset download (frees decoder / bandwidth while Ktor pulls large video + many assets).
                // After success the source-update effect above will re-prepare from local.
                LaunchedEffect(state.isDownloadingVideo, exoPlayer) {
                    if (state.isDownloadingVideo && exoPlayer.isPlaying) {
                        exoPlayer.pause()
                    }
                }

                // Retained per-page narration audio player (MediaPlayer for short clips; release prior on new)
                val narrationPlayer = remember { mutableStateOf<MediaPlayer?>(null) }

                // Drive page changes from video playback position using cues (basic ticker)
                LaunchedEffect(exoPlayer, state.cues, state.isVideoMode) {
                    if (state.isVideoMode && state.cues.isNotEmpty()) {
                        while (isActive) {
                            val posSec = exoPlayer.currentPosition / 1000.0
                            val matching = state.cues.firstOrNull { posSec >= it.startSec && posSec < it.endSec }
                            if (matching != null && matching.pageIndex != state.currentPage) {
                                onAction(ReaderAction.GoToPage(matching.pageIndex))
                            }
                            delay(350.milliseconds)
                        }
                    }
                }

                // When the page changes, seek the video to that page's cue start — but ONLY for
                // user-initiated jumps (Prev/Next/restore). When the video itself drives the page
                // change (cue ticker above), the playback position is already inside the new cue,
                // so seeking would rewind the video to the cue start and stutter at every boundary.
                LaunchedEffect(state.currentPage, exoPlayer, state.isVideoMode) {
                    if (state.isVideoMode) {
                        val cue = state.cues.firstOrNull { it.pageIndex == state.currentPage }
                        if (cue != null) {
                            val posSec = exoPlayer.currentPosition / 1000.0
                            val alreadyInCue = posSec >= cue.startSec && posSec < cue.endSec
                            if (!alreadyInCue) {
                                exoPlayer.seekTo((cue.startSec * 1000).toLong().coerceAtLeast(0))
                            }
                        }
                    }
                }

                DisposableEffect(exoPlayer) {
                    onDispose {
                        exoPlayer.release()
                    }
                }

                DisposableEffect(narrationPlayer.value) {
                    onDispose {
                        narrationPlayer.value?.release()
                        narrationPlayer.value = null
                    }
                }

                PardisMetaPill(
                    text = "Page ${page.page}",
                    containerColor = PardisColors.backgroundAlt,
                    contentColor = PardisColors.inkMuted,
                )
                Spacer(Modifier.height(PardisSpacing.sm))

                if (videoUrl != null) {
                    // Video mode: player always visible at top (tall, prominent), 
                    // dedicated scrollable captions area below for readable synced text.
                    // Much better UX than cramped scroll + tiny overlay.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp)  // taller for better viewing
                    ) {
                        PardisCard(modifier = Modifier.fillMaxSize()) {
                            AndroidView(
                                factory = {
                                    PlayerView(it).apply {
                                        player = exoPlayer
                                        useController = true
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(Modifier.height(PardisSpacing.sm))

                    // Scrollable captions / current page text (large, readable while video plays)
                    PardisPanel(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        PersianReaderParagraph(
                            text = page.paragraphsFa.joinToString("\n\n"),
                            style = MaterialTheme.typography.titleMedium,
                            color = PardisColors.ink,
                        )
                        Text(
                            page.paragraphsEn.joinToString("\n\n"),
                            style = MaterialTheme.typography.bodyLarge,
                            color = PardisColors.inkSoft,
                        )

                        if (page.vocabulary.isNotEmpty()) {
                            Text("Vocab on this page", style = MaterialTheme.typography.labelMedium, color = PardisColors.inkMuted)
                            Column {
                                page.vocabulary.take(3).forEach { v ->
                                    PardisVocabChip(vocab = v, onClick = { onAction(ReaderAction.ShowVocab(v)) })
                                }
                            }
                        }
                    }
                } else {
                    // Normal text/illustration mode - everything scrollable
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        val illoUrl = state.localIllustrationUrls[page.page] ?: page.illustrationUrl
                        PardisRemoteImageFrame(
                            imageUrl = illoUrl,
                            contentDescription = "Illustration for page ${page.page} of story",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                        )

                        PardisPanel {
                            PersianReaderParagraph(
                                text = page.paragraphsFa.joinToString("\n\n"),
                                style = MaterialTheme.typography.bodyLarge,
                                color = PardisColors.ink,
                            )
                            Text(page.paragraphsEn.joinToString("\n\n"), style = MaterialTheme.typography.bodyMedium, color = PardisColors.inkSoft)

                            if (page.vocabulary.isNotEmpty()) {
                                Text("Vocab on this page", style = MaterialTheme.typography.labelMedium, color = PardisColors.inkMuted)
                                Column {
                                    page.vocabulary.take(3).forEach { v ->
                                        PardisVocabChip(vocab = v, onClick = { onAction(ReaderAction.ShowVocab(v)) })
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(PardisSpacing.md))

                // Transport (fixed at bottom) - split for better UX and accessibility
                PardisPanel {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(onClick = { onAction(ReaderAction.PrevPage) }, enabled = state.currentPage > 0) {
                            Text("Prev")
                        }
                        Button(
                            onClick = { onAction(ReaderAction.NextPage) },
                            enabled = state.currentPage < state.pages.lastIndex,
                            colors = ButtonDefaults.buttonColors(containerColor = PardisColors.saffron)
                        ) {
                            Text(if (state.currentPage == state.pages.lastIndex) "Finish" else "Next")
                        }
                        Spacer(Modifier.weight(1f))
                        if (state.videoUrlFa != null || state.videoUrlEn != null) {
                            Button(onClick = { onAction(ReaderAction.ToggleVideo) }) {
                                Text(if (state.isVideoMode) "Text mode" else "Video mode")
                            }

                            if (state.isVideoMode) {
                                val hasLocal = state.localVideoUrlFa != null || state.localVideoUrlEn != null
                                if (!hasLocal) {
                                    Button(
                                        onClick = { onAction(ReaderAction.DownloadVideo("fa")) },
                                        enabled = !state.isDownloadingVideo
                                    ) {
                                        Text(state.downloadProgress ?: if (state.isDownloadingVideo) "Downloading video + assets..." else "Cache video + assets")
                                    }
                                } else {
                                    PardisMetaPill("Video cached", PardisColors.mintSoft, PardisColors.mintDeep)
                                    Button(
                                        onClick = { onAction(ReaderAction.ClearAssets) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                    ) {
                                        Text("Clear", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }

                    if (!state.isVideoMode) {
                        Button(onClick = {
                            onAction(ReaderAction.PlayNarration)
                            // Play current page narration (fa preferred), stop/release any prior clip
                            try {
                                narrationPlayer.value?.release()
                                val current = state.pages.getOrNull(state.currentPage)
                                val pageNum = current?.page ?: 0
                                val faKey = "fa-$pageNum"
                                val enKey = "en-$pageNum"
                                val localNar = if (state.preferredNarrationLang == "fa") state.localNarrationUrls[faKey] ?: state.localNarrationUrls[enKey]
                                               else state.localNarrationUrls[enKey] ?: state.localNarrationUrls[faKey]
                                val url = localNar ?: if (state.preferredNarrationLang == "fa") current?.narrationFa?.url ?: current?.narrationEn?.url
                                                      else current?.narrationEn?.url ?: current?.narrationFa?.url
                                url?.let {
                                    val mp = MediaPlayer().apply {
                                        setDataSource(it)
                                        setOnPreparedListener { prepared ->
                                            prepared.start()
                                            // API 23+ is guaranteed by minSdk 24; keep default rate if a device rejects it.
                                            try {
                                                prepared.playbackParams = prepared.playbackParams.setSpeed(state.playbackRate)
                                            } catch (_: Exception) { /* keep default rate */ }
                                        }
                                        setOnCompletionListener { completed ->
                                            completed.release()
                                            if (narrationPlayer.value == completed) narrationPlayer.value = null
                                            // Auto-advance to next page after narration clip ends (only in text mode)
                                            if (!state.isVideoMode && state.currentPage < (state.pages.lastIndex)) {
                                                onAction(ReaderAction.NextPage)
                                            }
                                        }
                                        setOnErrorListener { p, _, _ ->
                                            p.release()
                                            if (narrationPlayer.value == p) narrationPlayer.value = null
                                            true
                                        }
                                        // Async prepare: never block the UI thread on a (possibly remote) narration URL.
                                        prepareAsync()
                                    }
                                    narrationPlayer.value = mp
                                }
                            } catch (_: Exception) {
                                // Silent fail for demo; in real app surface error (e.g. no audio for this page)
                                narrationPlayer.value?.release()
                                narrationPlayer.value = null
                            }
                        }) {
                            Text("Play Audio")
                        }
                        PardisControlGroup(label = "Narration language") {
                            Button(onClick = { onAction(ReaderAction.SetNarrationLang("fa")) }, enabled = state.preferredNarrationLang != "fa") {
                                Text("FA", style = MaterialTheme.typography.labelSmall)
                            }
                            Button(onClick = { onAction(ReaderAction.SetNarrationLang("en")) }, enabled = state.preferredNarrationLang != "en") {
                                Text("EN", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        PardisControlGroup(label = "Playback speed") {
                            Button(onClick = { onAction(ReaderAction.SetPlaybackRate(0.5f)) }) { Text("0.5x", style = MaterialTheme.typography.labelSmall) }
                            Button(onClick = { onAction(ReaderAction.SetPlaybackRate(1.0f)) }) { Text("1x", style = MaterialTheme.typography.labelSmall) }
                            Button(onClick = { onAction(ReaderAction.SetPlaybackRate(1.5f)) }) { Text("1.5x", style = MaterialTheme.typography.labelSmall) }
                            Button(onClick = { onAction(ReaderAction.SetPlaybackRate(2.0f)) }) { Text("2x", style = MaterialTheme.typography.labelSmall) }
                        }
                        // Clear cached assets if any
                        val hasLocal = state.localVideoUrlFa != null || state.localVideoUrlEn != null || state.localIllustrationUrls.isNotEmpty() || state.localNarrationUrls.isNotEmpty()
                        if (hasLocal) {
                            Button(onClick = { onAction(ReaderAction.ClearAssets) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                                Text("Clear offline", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                // Basic bottom "sheet" for selected vocab (tappable chips open this)
                state.selectedVocab?.let { v ->
                    Spacer(Modifier.height(PardisSpacing.sm))
                    PardisVocabSheet(
                        vocab = v,
                        modifier = Modifier.fillMaxWidth(),
                        onPlayPronunciation = if (v.audioUrl != null) {
                            {
                                val mp = MediaPlayer()
                                try {
                                    mp.setDataSource(v.audioUrl)
                                    mp.setOnPreparedListener { it.start() }
                                    mp.setOnCompletionListener { it.release() }
                                    mp.setOnErrorListener { p, _, _ -> p.release(); true }
                                    mp.prepareAsync()
                                } catch (_: Exception) {
                                    mp.release()
                                }
                            }
                        } else {
                            null
                        },
                        onClose = { onAction(ReaderAction.DismissVocab) },
                    )
                }
            }
        }
    }
}
