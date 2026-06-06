package app.pardis.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pardis.design.PardisColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.milliseconds
import app.pardis.design.PardisRadius
import app.pardis.design.PardisShadows
import app.pardis.design.PardisSpacing
import app.pardis.design.PardisTheme
import app.pardis.shared.library.LibraryAction
import app.pardis.shared.library.LibraryUiState
import app.pardis.shared.library.LibraryViewModel
import app.pardis.shared.reader.ReaderAction
import app.pardis.shared.reader.ReaderUiState
import app.pardis.shared.reader.ReaderViewModel
import app.pardis.core.model.VocabItem
import coil.compose.AsyncImage
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
            .background(Brush.verticalGradient(listOf(PardisColors.background, PardisColors.backgroundAlt)))
            .padding(PardisSpacing.md)
    ) {
        Text(
            "Pardis",
            style = MaterialTheme.typography.headlineMedium,
            color = PardisColors.indigo,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            "Persian heritage stories",
            style = MaterialTheme.typography.bodyMedium,
            color = PardisColors.inkSoft
        )
        Spacer(Modifier.height(PardisSpacing.sm))
        // Simple search (filters title/age)
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { onAction(LibraryAction.Search(query = it)) },
            label = { Text("Search stories") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(PardisSpacing.sm))
        // Age-band filter chips (derived from the data). Tapping the active band again clears it.
        if (state.ageBands.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(PardisSpacing.xs)
            ) {
                FilterChip(
                    selected = state.selectedAgeBand == null,
                    onClick = { onAction(LibraryAction.SetAgeBand(null)) },
                    label = { Text("All ages") }
                )
                state.ageBands.forEach { band ->
                    FilterChip(
                        selected = state.selectedAgeBand == band,
                        onClick = {
                            onAction(LibraryAction.SetAgeBand(if (state.selectedAgeBand == band) null else band))
                        },
                        label = { Text(band) }
                    )
                }
            }
            Spacer(Modifier.height(PardisSpacing.sm))
        }
        // Toggle for offline cached only
        Button(onClick = { onAction(LibraryAction.ToggleShowOnlyCached) }) {
            Text(if (state.showOnlyCached) "Show all stories" else "Show only offline cached")
        }
        if (state.totalCachedLabel.isNotEmpty()) {
            Spacer(Modifier.height(PardisSpacing.xs))
            Text(
                "Cached offline: ${state.totalCachedLabel}",
                style = MaterialTheme.typography.labelSmall,
                color = PardisColors.inkSoft
            )
        }
        Spacer(Modifier.height(PardisSpacing.md))

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
                StoryCard(
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

        // Floating refresh for demo
        Spacer(Modifier.height(PardisSpacing.md))
        Button(
            onClick = { onAction(LibraryAction.Refresh) },
            colors = ButtonDefaults.buttonColors(containerColor = PardisColors.saffron)
        ) {
            Text("Refresh library")
        }
    }
}

@Composable
private fun StoryCard(
    titleEn: String,
    titleFa: String,
    ageBand: String,
    minutes: Int,
    vocabCount: Int,
    coverUrl: String?,
    downloadProgress: String?,
    downloadedSizeLabel: String?,
    isFailed: Boolean,
    onClick: () -> Unit,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onRemove: () -> Unit,
) {
    PardisCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        onClick = onClick
    ) {
        Column(Modifier.padding(PardisSpacing.md)) {
            Row {
                if (coverUrl != null) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = "Cover image for story: $titleEn in $ageBand age band",
                        modifier = Modifier
                            .size(60.dp)
                            .padding(end = PardisSpacing.sm)
                    )
                }
                Column {
                    Text(
                        titleEn,
                        style = MaterialTheme.typography.titleMedium,
                        color = PardisColors.ink,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    PersianReaderInline(
                        text = titleFa,
                        style = MaterialTheme.typography.bodyLarge,
                        color = PardisColors.indigo,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(PardisSpacing.xs))
                    Row(horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
                        Text("$ageBand • ${minutes}m", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkSoft)
                        Text("• $vocabCount words", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkMuted)
                    }
                }
            }
            Spacer(Modifier.height(PardisSpacing.sm))
            // Offline download control: reflects OfflineDownloadManager state for this story.
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm)) {
                when {
                    downloadProgress != null -> {
                        Text(downloadProgress, style = MaterialTheme.typography.labelSmall, color = PardisColors.inkSoft)
                        Spacer(Modifier.weight(1f))
                        OutlinedButton(onClick = onCancel) { Text("Cancel", style = MaterialTheme.typography.labelSmall) }
                    }
                    downloadedSizeLabel != null -> {
                        Text("✓ Offline ($downloadedSizeLabel)", style = MaterialTheme.typography.labelSmall, color = PardisColors.mint)
                        Spacer(Modifier.weight(1f))
                        OutlinedButton(onClick = onRemove) { Text("Remove", style = MaterialTheme.typography.labelSmall) }
                    }
                    isFailed -> {
                        Text("Download failed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.weight(1f))
                        Button(onClick = onDownload) { Text("Retry", style = MaterialTheme.typography.labelSmall) }
                    }
                    else -> {
                        Spacer(Modifier.weight(1f))
                        Button(onClick = onDownload, colors = ButtonDefaults.buttonColors(containerColor = PardisColors.saffron)) {
                            Text("Download offline", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Basic PardisCard component using design tokens (colors, radius, spacing, shadows).
 * Follows kmpSkill + Phase 3 plan: tokenised, native only.
 */
@Composable
fun PardisCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(PardisRadius.md)
    Surface(
        modifier = modifier
            .shadow(PardisShadows.md, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        color = PardisColors.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, PardisColors.borderSoft)
    ) {
        content()
    }
}

/**
 * Simple vocab chip component using tokens.
 */
@Composable
fun PardisVocabChip(vocab: VocabItem, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = PardisSpacing.xs),
        shape = RoundedCornerShape(PardisRadius.full),
        color = PardisColors.mintSoft,
        border = androidx.compose.foundation.BorderStroke(1.dp, PardisColors.borderSoft),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = PardisSpacing.sm, vertical = PardisSpacing.xs / 2)
                .semantics { contentDescription = "Vocabulary term: ${vocab.fa} transliterated as ${vocab.translit}, English ${vocab.en}" }
        ) {
            PersianReaderInline(
                text = vocab.fa,
                style = MaterialTheme.typography.bodySmall,
                color = PardisColors.indigoDeep,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${vocab.translit} — ${vocab.en}",
                style = MaterialTheme.typography.labelSmall,
                color = PardisColors.inkSoft,
            )
        }
    }
}

@Composable
private fun PersianReaderParagraph(
    text: String,
    style: TextStyle,
    color: Color,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            style = style,
            color = color,
            textAlign = TextAlign.Start,
        )
    }
}

@Composable
private fun PersianReaderInline(
    text: String,
    style: TextStyle,
    color: Color,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Text(
            text = text,
            style = style,
            color = color,
            textAlign = TextAlign.Start,
            maxLines = maxLines,
            overflow = overflow,
        )
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
    val context = androidx.compose.ui.platform.LocalContext.current
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
            .background(Brush.verticalGradient(listOf(PardisColors.background, PardisColors.backgroundAlt)))
            .padding(PardisSpacing.md)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Library", color = PardisColors.indigo) }
            Spacer(Modifier.weight(1f))
            if (state.pages.isNotEmpty()) {
                Text("${state.currentPage + 1} / ${state.pages.size}", color = PardisColors.inkSoft)
                val hasOfflineAssets = state.localVideoUrlFa != null || state.localVideoUrlEn != null || state.localIllustrationUrls.isNotEmpty() || state.localNarrationUrls.isNotEmpty()
                if (hasOfflineAssets) {
                    Spacer(Modifier.width(PardisSpacing.sm))
                    Text("✓ Offline", color = PardisColors.mint, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

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

                Text(
                    "Page ${page.page}",
                    style = MaterialTheme.typography.labelSmall,
                    color = PardisColors.inkMuted
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

                    Spacer(Modifier.height(PardisSpacing.sm))

                    // Scrollable captions / current page text (large, readable while video plays)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        PersianReaderParagraph(
                            text = page.paragraphsFa.joinToString("\n\n"),
                            style = MaterialTheme.typography.titleMedium,
                            color = PardisColors.ink,
                        )
                        Spacer(Modifier.height(PardisSpacing.sm))
                        Text(
                            page.paragraphsEn.joinToString("\n\n"),
                            style = MaterialTheme.typography.bodyLarge,
                            color = PardisColors.inkSoft
                        )

                        Spacer(Modifier.height(PardisSpacing.lg))

                        if (page.vocabulary.isNotEmpty()) {
                            Text("Vocab on this page:", style = MaterialTheme.typography.labelMedium)
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
                        if (illoUrl != null) {
                            AsyncImage(
                                model = illoUrl,
                                contentDescription = "Illustration for page ${page.page} of story",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                            )
                        } else {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                color = PardisColors.surfaceLilac
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "No illustration",
                                        color = PardisColors.inkSoft
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(PardisSpacing.md))

                        PersianReaderParagraph(
                            text = page.paragraphsFa.joinToString("\n\n"),
                            style = MaterialTheme.typography.bodyLarge,
                            color = PardisColors.ink,
                        )
                        Spacer(Modifier.height(PardisSpacing.sm))
                        Text(page.paragraphsEn.joinToString("\n\n"), style = MaterialTheme.typography.bodyMedium, color = PardisColors.inkSoft)

                        Spacer(Modifier.height(PardisSpacing.lg))

                        if (page.vocabulary.isNotEmpty()) {
                            Text("Vocab on this page:", style = MaterialTheme.typography.labelMedium)
                            Column {
                                page.vocabulary.take(3).forEach { v ->
                                    PardisVocabChip(vocab = v, onClick = { onAction(ReaderAction.ShowVocab(v)) })
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(PardisSpacing.md))

                // Transport (fixed at bottom) - split for better UX and accessibility
                // Main nav row
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

                        // Offline video download affordance (appears for video stories; only in video mode to not clutter text mode).
                        // Uses the polished video UX area. "Cache video" triggers MP4 download to local cache for offline play.
                        // Once cached, player will use the local file path (no net needed).
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
                                // Simple cached indicator using existing design tokens (no new visuals/tokens added).
                                Text("✓ Video + assets cached", color = PardisColors.mint)
                                Button(onClick = { onAction(ReaderAction.ClearAssets) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                                    Text("Clear", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                // Separate accessible row for audio controls (only in text mode) - avoids cramming, better touch targets
                if (!state.isVideoMode) {
                    Spacer(Modifier.height(PardisSpacing.xs))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
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
                        // Lang switch - grouped
                        Text("Lang:", style = MaterialTheme.typography.labelSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(PardisSpacing.xs)) {
                            Button(onClick = { onAction(ReaderAction.SetNarrationLang("fa")) }, enabled = state.preferredNarrationLang != "fa") {
                                Text("FA")
                            }
                            Button(onClick = { onAction(ReaderAction.SetNarrationLang("en")) }, enabled = state.preferredNarrationLang != "en") {
                                Text("EN")
                            }
                        }
                        // Rate controls - grouped, smaller for density but still accessible
                        Text("Rate:", style = MaterialTheme.typography.labelSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(PardisSpacing.xs)) {
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
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = PardisColors.surface2,
                        shape = RoundedCornerShape(PardisRadius.md),
                        shadowElevation = 4.dp
                    ) {
                        Column(Modifier.padding(PardisSpacing.md)) {
                            Text("Vocab", style = MaterialTheme.typography.labelMedium, color = PardisColors.indigo)
                            Spacer(Modifier.height(PardisSpacing.xs))
                            PersianReaderInline(
                                text = v.fa,
                                style = MaterialTheme.typography.titleMedium,
                                color = PardisColors.ink,
                            )
                            Text("(${v.translit})", style = MaterialTheme.typography.bodyMedium, color = PardisColors.inkSoft)
                            Text(v.en, style = MaterialTheme.typography.bodyLarge, color = PardisColors.inkSoft)
                            if (v.context.isNotBlank()) {
                                Spacer(Modifier.height(PardisSpacing.xs))
                                Text("in: ${v.context}", style = MaterialTheme.typography.bodySmall, color = PardisColors.inkMuted)
                            }
                            if (v.audioUrl != null) {
                                TextButton(onClick = {
                                    // One-shot vocab pronunciation. prepareAsync (no main-thread block) and
                                    // always release on completion/error so we don't leak a player per tap.
                                    val mp = android.media.MediaPlayer()
                                    try {
                                        mp.setDataSource(v.audioUrl)
                                        mp.setOnPreparedListener { it.start() }
                                        mp.setOnCompletionListener { it.release() }
                                        mp.setOnErrorListener { p, _, _ -> p.release(); true }
                                        mp.prepareAsync()
                                    } catch (_: Exception) { mp.release() }
                                }) { Text("▶ Play pronunciation") }
                            }
                            TextButton(onClick = { onAction(ReaderAction.DismissVocab) }) {
                                Text("Close", color = PardisColors.saffron)
                            }
                        }
                    }
                }
            }
        }
    }
}