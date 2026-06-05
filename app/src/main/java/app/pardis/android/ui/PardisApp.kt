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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pardis.design.PardisColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import app.pardis.design.PardisRadius
import app.pardis.design.PardisShadows
import app.pardis.design.PardisSpacing
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
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = PardisColors.background
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
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
            .background(PardisColors.background)
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
                    coverUrl = story.coverUrl,
                    isCachedOffline = state.cachedStorySlugs.contains(story.slug),
                    onClick = { onOpenStory(story.slug) }
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
    isCachedOffline: Boolean = false,
    onClick: () -> Unit
) {
    PardisCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        onClick = onClick
    ) {
        Row(Modifier.padding(PardisSpacing.md)) {
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
                Text(
                    titleFa,
                    style = MaterialTheme.typography.bodyLarge,
                    color = PardisColors.indigo
                )
                Spacer(Modifier.height(PardisSpacing.xs))
                Row(horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
                    Text("$ageBand • ${minutes}m", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkSoft)
                    Text("• $vocabCount words", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkMuted)
                    if (isCachedOffline) {
                        Spacer(Modifier.width(PardisSpacing.xs))
                        Text("✓ Offline", style = MaterialTheme.typography.labelSmall, color = PardisColors.mint)
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
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(PardisRadius.md)
    Surface(
        modifier = modifier
            .shadow(PardisShadows.md, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        color = PardisColors.surface2,
        border = androidx.compose.foundation.BorderStroke(1.dp, PardisColors.border)
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(PardisRadius.sm),
        color = PardisColors.mintSoft
    ) {
        Text(
            "${vocab.fa} (${vocab.translit}) — ${vocab.en}",
            modifier = Modifier.padding(horizontal = PardisSpacing.sm, vertical = PardisSpacing.xs / 2)
                .semantics { contentDescription = "Vocabulary term: ${vocab.fa} transliterated as ${vocab.translit}, English ${vocab.en}" },
            style = MaterialTheme.typography.bodySmall,
            color = PardisColors.ink
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
            .background(PardisColors.background)
            .padding(PardisSpacing.md)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Library", color = PardisColors.indigo) }
            Spacer(Modifier.weight(1f))
            if (state.pages.isNotEmpty()) {
                Text("${state.currentPage + 1} / ${state.pages.size}", color = PardisColors.inkSoft)
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

                // Stable video player instance (only when in video mode with url)
                val exoPlayer = remember(videoUrl) {
                    if (videoUrl != null) {
                        ExoPlayer.Builder(context).build().apply {
                            setMediaItem(MediaItem.fromUri(videoUrl))
                            prepare()
                            playWhenReady = true
                            addListener(object : Player.Listener {
                                override fun onPlaybackStateChanged(playbackState: Int) {
                                    if (playbackState == Player.STATE_ENDED) {
                                        // Video finished - go to last page / end of story
                                        onAction(ReaderAction.GoToPage(state.pages.lastIndex.coerceAtLeast(0)))
                                    }
                                }
                            })
                        }
                    } else null
                }

                // Retained per-page narration audio player (MediaPlayer for short clips; release prior on new)
                val narrationPlayer = remember { mutableStateOf<MediaPlayer?>(null) }

                // Drive page changes from video playback position using cues (basic ticker)
                LaunchedEffect(exoPlayer, state.cues, state.isVideoMode) {
                    val player = exoPlayer
                    if (player != null && state.isVideoMode && state.cues.isNotEmpty()) {
                        while (isActive) {
                            val posSec = player.currentPosition / 1000.0
                            val matching = state.cues.firstOrNull { posSec >= it.startSec && posSec < it.endSec }
                            if (matching != null && matching.pageIndex != state.currentPage) {
                                onAction(ReaderAction.GoToPage(matching.pageIndex))
                            }
                            delay(350)
                        }
                    }
                }

                // When page changes (by user or cue), seek video to the cue start for that page
                LaunchedEffect(state.currentPage, exoPlayer, state.isVideoMode) {
                    val player = exoPlayer
                    if (player != null && state.isVideoMode) {
                        val cue = state.cues.firstOrNull { it.pageIndex == state.currentPage }
                        if (cue != null) {
                            player.seekTo((cue.startSec * 1000).toLong().coerceAtLeast(0))
                        }
                    }
                }

                DisposableEffect(exoPlayer) {
                    onDispose {
                        exoPlayer?.release()
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

                if (videoUrl != null && exoPlayer != null) {
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
                        Text(
                            page.paragraphsFa.joinToString("\n\n"),
                            style = MaterialTheme.typography.titleMedium,
                            color = PardisColors.ink
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

                        Text(page.paragraphsFa.joinToString("\n\n"), style = MaterialTheme.typography.bodyLarge, color = PardisColors.ink)
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

                // Transport (fixed at bottom)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
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
                                val localNar = state.localNarrationUrls[faKey] ?: state.localNarrationUrls[enKey]
                                val url = localNar ?: current?.narrationFa?.url ?: current?.narrationEn?.url
                                url?.let {
                                    val mp = MediaPlayer().apply {
                                        setDataSource(it)
                                        setOnCompletionListener { completed ->
                                            completed.release()
                                            if (narrationPlayer.value == completed) narrationPlayer.value = null
                                            // Auto-advance to next page after narration clip ends (only in text mode)
                                            if (!state.isVideoMode && state.currentPage < (state.pages.lastIndex)) {
                                                onAction(ReaderAction.NextPage)
                                            }
                                        }
                                        prepare()
                                        start()
                                    }
                                    narrationPlayer.value = mp
                                }
                            } catch (e: Exception) {
                                // Silent fail for demo; in real app surface error (e.g. no audio for this page)
                                narrationPlayer.value?.release()
                                narrationPlayer.value = null
                            }
                        }) {
                            Text("Play Audio")
                        }
                    }
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
                                    Text(if (state.isDownloadingVideo) "Downloading video + assets..." else "Cache video + assets")
                                }
                            } else {
                                // Simple cached indicator using existing design tokens (no new visuals/tokens added).
                                Text("✓ Video + assets cached", color = PardisColors.mint)
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
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(PardisRadius.md),
                        shadowElevation = 4.dp
                    ) {
                        Column(Modifier.padding(PardisSpacing.md)) {
                            Text("Vocab", style = MaterialTheme.typography.labelMedium, color = PardisColors.indigo)
                            Spacer(Modifier.height(PardisSpacing.xs))
                            Text("${v.fa}  (${v.translit})", style = MaterialTheme.typography.titleMedium, color = PardisColors.ink)
                            Text(v.en, style = MaterialTheme.typography.bodyLarge, color = PardisColors.inkSoft)
                            if (v.context != null) {
                                Spacer(Modifier.height(PardisSpacing.xs))
                                Text("in: ${v.context}", style = MaterialTheme.typography.bodySmall, color = PardisColors.inkMuted)
                            }
                            if (v.audioUrl != null) {
                                TextButton(onClick = {
                                    // reuse narration style play for vocab audio if present
                                    val mp = android.media.MediaPlayer()
                                    try { mp.setDataSource(v.audioUrl); mp.prepare(); mp.start() } catch (_: Exception) { mp.release() }
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