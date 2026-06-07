package app.pardis.android.ui

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.pardis.design.PardisFonts
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import app.pardis.core.model.StoryPage
import app.pardis.core.model.VocabItem
import app.pardis.design.PardisColors
import app.pardis.design.PardisRadius
import app.pardis.design.PardisSpacing
import app.pardis.shared.reader.ReaderAction
import app.pardis.shared.reader.ReaderUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.milliseconds

private val cueTagRegex = Regex("\\s*\\[[^\\]]*]")

@Composable
fun ReaderScreen(
    state: ReaderUiState,
    onAction: (ReaderAction) -> Unit,
    onBack: () -> Unit,
    onFinish: (String) -> Unit = {},
) {
    Box(Modifier.fillMaxSize().background(PardisColors.background)) {
        when {
            state.isLoading -> CircularProgressIndicator(color = PardisColors.saffron, modifier = Modifier.align(Alignment.Center))
            state.errorMessage != null -> Column(Modifier.align(Alignment.Center).padding(PardisSpacing.lg), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Error: ${state.errorMessage}", color = PardisColors.error)
                Spacer(Modifier.height(PardisSpacing.sm))
                Button(onClick = { onAction(ReaderAction.LoadStory(state.storySlug)) }, colors = ButtonDefaults.buttonColors(containerColor = PardisColors.saffron)) { Text("Retry") }
            }
            state.pages.isEmpty() -> Text("No pages loaded for ${state.storySlug}", modifier = Modifier.align(Alignment.Center))
            else -> ReaderContent(state, onAction, onBack, onFinish)
        }
    }
}

@Composable
private fun ReaderContent(
    state: ReaderUiState,
    onAction: (ReaderAction) -> Unit,
    onBack: () -> Unit,
    onFinish: (String) -> Unit,
) {
    val page = state.pages.getOrNull(state.currentPage) ?: state.pages.first()
    val videoUrl = if (state.isVideoMode) {
        state.localVideoUrlFa ?: state.localVideoUrlEn ?: state.videoUrlFa ?: state.videoUrlEn
    } else null
    val context = LocalContext.current

    // ─── Preserved playback logic ───────────────────────────────────────────
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        onAction(ReaderAction.GoToPage(state.pages.lastIndex.coerceAtLeast(0)))
                    }
                }
            })
        }
    }
    LaunchedEffect(videoUrl) {
        if (videoUrl != null) {
            exoPlayer.setMediaItem(MediaItem.fromUri(videoUrl))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        } else {
            exoPlayer.pause()
        }
    }
    LaunchedEffect(state.isDownloadingVideo, exoPlayer) {
        if (state.isDownloadingVideo && exoPlayer.isPlaying) exoPlayer.pause()
    }
    val narrationPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
    var narrating by remember { mutableStateOf(false) }
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
    LaunchedEffect(state.currentPage, exoPlayer, state.isVideoMode) {
        if (state.isVideoMode) {
            val cue = state.cues.firstOrNull { it.pageIndex == state.currentPage }
            if (cue != null) {
                val posSec = exoPlayer.currentPosition / 1000.0
                val alreadyInCue = posSec >= cue.startSec && posSec < cue.endSec
                if (!alreadyInCue) exoPlayer.seekTo((cue.startSec * 1000).toLong().coerceAtLeast(0))
            }
        }
    }
    DisposableEffect(exoPlayer) { onDispose { exoPlayer.release() } }
    DisposableEffect(narrationPlayer.value) {
        onDispose { narrationPlayer.value?.release(); narrationPlayer.value = null }
    }

    val playNarration: () -> Unit = play@{
        try {
            narrationPlayer.value?.release()
            val current = state.pages.getOrNull(state.currentPage)
            val pageNum = current?.page ?: 0
            val faKey = "fa-$pageNum"; val enKey = "en-$pageNum"
            val localNar = if (state.preferredNarrationLang == "fa") state.localNarrationUrls[faKey] ?: state.localNarrationUrls[enKey]
                           else state.localNarrationUrls[enKey] ?: state.localNarrationUrls[faKey]
            val url = localNar ?: if (state.preferredNarrationLang == "fa") current?.narrationFa?.url ?: current?.narrationEn?.url
                                  else current?.narrationEn?.url ?: current?.narrationFa?.url
            if (url == null) return@play
            val mp = MediaPlayer().apply {
                setDataSource(url)
                setOnPreparedListener { prepared ->
                    prepared.start()
                    narrating = true
                    try { prepared.playbackParams = prepared.playbackParams.setSpeed(state.playbackRate) } catch (_: Exception) {}
                }
                setOnCompletionListener { completed ->
                    completed.release()
                    if (narrationPlayer.value == completed) narrationPlayer.value = null
                    narrating = false
                    if (!state.isVideoMode && state.currentPage < state.pages.lastIndex) onAction(ReaderAction.NextPage)
                }
                setOnErrorListener { p, _, _ ->
                    p.release(); if (narrationPlayer.value == p) narrationPlayer.value = null; narrating = false; true
                }
                prepareAsync()
            }
            narrationPlayer.value = mp
        } catch (_: Exception) {
            narrationPlayer.value?.release(); narrationPlayer.value = null; narrating = false
        }
    }
    val stopNarration: () -> Unit = {
        narrationPlayer.value?.release(); narrationPlayer.value = null; narrating = false
    }
    // ─── End preserved logic ────────────────────────────────────────────────

    var displayLang by remember { mutableStateOf("both") } // both | en | fa
    val onLastPage = state.currentPage == state.pages.lastIndex
    val hasVideo = state.videoUrlFa != null || state.videoUrlEn != null

    Column(Modifier.fillMaxSize()) {
        ReaderTopBar(
            title = state.storyTitle.ifEmpty { "Reader" },
            page = state.currentPage + 1,
            total = state.pages.size,
            onBack = onBack,
        )
        ReaderPageDots(
            total = state.pages.size,
            current = state.currentPage,
            onGoTo = { onAction(ReaderAction.GoToPage(it)) },
        )
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = PardisSpacing.lg),
        ) {
            Spacer(Modifier.height(PardisSpacing.sm))
            // Illustration (or video player when in video mode)
            Box(Modifier.fillMaxWidth().height(290.dp).clip(RoundedCornerShape(PardisRadius.xl))) {
                if (videoUrl != null) {
                    AndroidView(
                        factory = { PlayerView(it).apply { player = exoPlayer; useController = true } },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    val illoUrl = state.localIllustrationUrls[page.page] ?: page.illustrationUrl
                    PardisRemoteImageFrame(imageUrl = illoUrl, contentDescription = "Illustration for page ${page.page}", modifier = Modifier.fillMaxSize())
                    PardisPatternOverlay(PardisMotif.Paisley, PardisColors.inkOnDark, alpha = 0.10f, fade = PardisPatternFade.BottomLeft, modifier = Modifier.matchParentSize())
                    Box(
                        Modifier.align(Alignment.TopStart).padding(12.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.scrim).padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text("${state.currentPage + 1} / ${state.pages.size}", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkOnDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(PardisSpacing.md))

            // Prose
            if (displayLang != "fa") {
                Text(
                    page.paragraphsEn.joinToString("\n\n").replace(cueTagRegex, ""),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleLarge,
                    color = PardisColors.ink,
                    fontWeight = FontWeight.Medium,
                )
            }
            if (displayLang != "en") {
                if (displayLang == "both") {
                    Spacer(Modifier.height(PardisSpacing.md))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(PardisColors.border))
                    Spacer(Modifier.height(PardisSpacing.md))
                }
                FarsiGlossaryText(
                    faText = page.paragraphsFa.joinToString("\n\n").replace(cueTagRegex, ""),
                    vocab = page.vocabulary,
                    onTap = { onAction(ReaderAction.ShowVocab(it)) },
                )
            }
            if (page.vocabulary.isNotEmpty()) {
                Spacer(Modifier.height(PardisSpacing.md))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    PardisIcon(PardisIconKind.Languages, contentDescription = null, tint = PardisColors.inkFaint, size = 13.dp)
                    Text("Tap a highlighted word to learn it", style = MaterialTheme.typography.bodySmall, color = PardisColors.inkFaint)
                }
            }

            // Secondary controls: video toggle + offline (only when the story has video)
            if (hasVideo) {
                Spacer(Modifier.height(PardisSpacing.md))
                ReaderVideoControls(state, onAction)
            }
            Spacer(Modifier.height(PardisSpacing.md))
        }

        ReaderDock(
            progress = if (state.pages.size > 1) (state.currentPage + 1).toFloat() / state.pages.size else 1f,
            leftLabel = "${state.currentPage + 1}",
            rightLabel = "${state.pages.size}",
            displayLang = displayLang,
            onLangChange = {
                displayLang = it
                if (it != "both") onAction(ReaderAction.SetNarrationLang(it))
            },
            playing = narrating,
            onPlayPause = { if (narrating) stopNarration() else playNarration() },
            onPrev = { stopNarration(); onAction(ReaderAction.PrevPage) },
            prevEnabled = state.currentPage > 0,
            onNext = { stopNarration(); if (onLastPage) onFinish(state.storySlug) else onAction(ReaderAction.NextPage) },
            nextIsFinish = onLastPage,
        )
    }

    // Word card — modal bottom sheet with a dismiss scrim
    state.selectedVocab?.let { v ->
        ReaderWordCard(
            vocab = v,
            storyTitle = state.storyTitle,
            onHear = v.audioUrl?.let {
                {
                    val mp = MediaPlayer()
                    try {
                        mp.setDataSource(v.audioUrl)
                        mp.setOnPreparedListener { it.start() }
                        mp.setOnCompletionListener { it.release() }
                        mp.setOnErrorListener { p, _, _ -> p.release(); true }
                        mp.prepareAsync()
                    } catch (_: Exception) { mp.release() }
                }
            },
            onClose = { onAction(ReaderAction.DismissVocab) },
        )
    }
}

/** Tappable-Farsi-word card: a modal bottom sheet matching the v2 WordCard design. */
@Composable
private fun ReaderWordCard(
    vocab: VocabItem,
    storyTitle: String,
    onHear: (() -> Unit)?,
    onClose: () -> Unit,
) {
    var added by remember { mutableStateOf(false) }
    Box(
        Modifier.fillMaxSize().background(PardisColors.scrimSoft).clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClose,
        ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                .background(PardisColors.surface)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                .statusBarsPadding()
                .padding(horizontal = PardisSpacing.lg)
                .padding(top = PardisSpacing.sm, bottom = PardisSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.padding(vertical = PardisSpacing.sm).width(40.dp).height(4.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.border))
            Box(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(PardisRadius.lg))
                    .background(PardisColors.indigoTint)
                    .border(1.dp, PardisColors.indigoSoft, RoundedCornerShape(PardisRadius.lg)),
            ) {
                PardisPatternOverlay(PardisMotif.Paisley, PardisColors.indigo, alpha = 0.08f, fade = PardisPatternFade.Edges, modifier = Modifier.matchParentSize())
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 22.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PersianReaderInline(vocab.fa, style = MaterialTheme.typography.displayLarge, color = PardisColors.indigoDeep)
                    Text(vocab.translit, style = MaterialTheme.typography.bodyMedium, color = PardisColors.indigo)
                }
            }
            Spacer(Modifier.height(PardisSpacing.md))
            Text("“${vocab.en}”", style = MaterialTheme.typography.headlineSmall, color = PardisColors.ink, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            if (storyTitle.isNotEmpty()) {
                Text("from $storyTitle", style = MaterialTheme.typography.bodySmall, color = PardisColors.inkMuted)
            }
            Spacer(Modifier.height(PardisSpacing.md))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm)) {
                Row(
                    Modifier.weight(1f).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.saffronSoft).clickable(enabled = onHear != null) { onHear?.invoke() }.padding(vertical = 13.dp),
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
                ) {
                    PardisIcon(PardisIconKind.Volume, contentDescription = null, tint = PardisColors.saffronDeep, size = 18.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Hear it", style = MaterialTheme.typography.titleMedium, color = PardisColors.saffronDeep, fontWeight = FontWeight.Bold)
                }
                Row(
                    Modifier.weight(1f).clip(RoundedCornerShape(PardisRadius.full)).background(if (added) PardisColors.mint else PardisColors.ink).clickable { added = true }.padding(vertical = 13.dp),
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
                ) {
                    PardisIcon(if (added) PardisIconKind.Check else PardisIconKind.Sprout, contentDescription = null, tint = PardisColors.inkOnDark, size = 18.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(if (added) "In your garden" else "Add to garden", style = MaterialTheme.typography.titleMedium, color = PardisColors.inkOnDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ReaderTopBar(title: String, page: Int, total: Int, onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = PardisSpacing.md, vertical = PardisSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
    ) {
        ReaderIconButton(PardisIconKind.ChevRight, "Close", onBack, rotate = true)
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = PardisColors.ink, fontWeight = FontWeight.ExtraBold, maxLines = 1)
            Text("PAGE $page OF $total", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkMuted)
        }
        ReaderIconButton(PardisIconKind.Bookmark, "Bookmark", {})
    }
}

@Composable
private fun ReaderIconButton(icon: PardisIconKind, label: String, onClick: () -> Unit, rotate: Boolean = false) {
    Box(
        Modifier.size(42.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.backgroundAlt).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // ChevRight rotated 90° points down (close); otherwise drawn as-is.
        PardisIcon(
            icon,
            contentDescription = label,
            tint = PardisColors.ink,
            size = 20.dp,
            modifier = if (rotate) Modifier.rotate(90f) else Modifier,
        )
    }
}

@Composable
private fun ReaderPageDots(total: Int, current: Int, onGoTo: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = PardisSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 0 until total) {
            val color = when {
                i == current -> PardisColors.saffron
                i < current -> PardisColors.saffronSoft
                else -> PardisColors.border
            }
            Box(
                Modifier
                    .height(5.dp)
                    .width(if (i == current) 22.dp else 5.dp)
                    .clip(RoundedCornerShape(PardisRadius.full))
                    .background(color)
                    .clickable { onGoTo(i) },
            )
        }
    }
}

@Composable
private fun ReaderDock(
    progress: Float,
    leftLabel: String,
    rightLabel: String,
    displayLang: String,
    onLangChange: (String) -> Unit,
    playing: Boolean,
    onPlayPause: () -> Unit,
    onPrev: () -> Unit,
    prevEnabled: Boolean,
    onNext: () -> Unit,
    nextIsFinish: Boolean,
) {
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(PardisColors.surface)
            .padding(horizontal = PardisSpacing.lg)
            .padding(top = PardisSpacing.md)
            .padding(bottom = PardisSpacing.lg),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(leftLabel, style = MaterialTheme.typography.labelSmall, color = PardisColors.inkMuted)
            PardisProgressBar(value = progress, modifier = Modifier.weight(1f), height = 5)
            Text(rightLabel, style = MaterialTheme.typography.labelSmall, color = PardisColors.inkMuted)
        }
        Spacer(Modifier.height(PardisSpacing.md))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            ReaderSegmented(displayLang, onLangChange)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReaderIconButtonDim(PardisIconKind.ChevRight, "Previous", { if (prevEnabled) onPrev() }, flip = true, dim = !prevEnabled)
                Box(
                    Modifier.size(58.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.saffron).clickable(onClick = onPlayPause),
                    contentAlignment = Alignment.Center,
                ) {
                    PardisIcon(if (playing) PardisIconKind.Pause else PardisIconKind.Play, contentDescription = if (playing) "Pause" else "Play", tint = PardisColors.inkOnDark, size = 24.dp)
                }
                ReaderIconButton(if (nextIsFinish) PardisIconKind.Check else PardisIconKind.ChevRight, if (nextIsFinish) "Finish" else "Next", onNext)
            }
        }
    }
}

// Variant supporting flip (prev arrow) + dim (disabled look) used inside the dock.
@Composable
private fun ReaderIconButtonDim(icon: PardisIconKind, label: String, onClick: () -> Unit, flip: Boolean, dim: Boolean) {
    Box(
        Modifier.size(42.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.backgroundAlt).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        PardisIcon(
            icon,
            contentDescription = label,
            tint = if (dim) PardisColors.inkFaint else PardisColors.ink,
            size = 20.dp,
            modifier = if (flip) Modifier.rotate(180f) else Modifier,
        )
    }
}

@Composable
private fun ReaderSegmented(value: String, onChange: (String) -> Unit) {
    val options = listOf("en" to "EN", "both" to "Both", "fa" to "فا")
    Row(
        Modifier.clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.backgroundAlt).padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEach { (key, label) ->
            val selected = value == key
            Box(
                Modifier.clip(RoundedCornerShape(PardisRadius.full)).background(if (selected) PardisColors.surface else Color.Transparent).clickable { onChange(key) }.padding(horizontal = 13.dp, vertical = 7.dp),
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge, color = if (selected) PardisColors.ink else PardisColors.inkMuted, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ReaderVideoControls(state: ReaderUiState, onAction: (ReaderAction) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm)) {
        PardisFilterPill(
            label = if (state.isVideoMode) "Read text" else "Watch video",
            selected = state.isVideoMode,
            onClick = { onAction(ReaderAction.ToggleVideo) },
        )
        if (state.isVideoMode) {
            val hasLocal = state.localVideoUrlFa != null || state.localVideoUrlEn != null
            if (!hasLocal) {
                PardisFilterPill(
                    label = state.downloadProgress ?: if (state.isDownloadingVideo) "Downloading…" else "Save offline",
                    selected = false,
                    onClick = { if (!state.isDownloadingVideo) onAction(ReaderAction.DownloadVideo("fa")) },
                )
            } else {
                PardisMetaPill("Saved offline", PardisColors.mintSoft, PardisColors.mintDeep)
                PardisFilterPill(label = "Clear", selected = false, onClick = { onAction(ReaderAction.ClearAssets) })
            }
        }
    }
}

/** Renders a Farsi paragraph with glossary words styled + tappable (opens the word sheet). */
@Composable
private fun FarsiGlossaryText(faText: String, vocab: List<VocabItem>, onTap: (VocabItem) -> Unit) {
    val annotated = buildAnnotatedString {
        var remaining = faText
        var guard = 0
        while (remaining.isNotEmpty() && guard++ < 400) {
            var hitIdx = -1
            var hitVocab: VocabItem? = null
            vocab.forEach { v ->
                val idx = remaining.indexOf(v.fa)
                if (v.fa.isNotEmpty() && idx != -1 && (hitIdx == -1 || idx < hitIdx)) {
                    hitIdx = idx; hitVocab = v
                }
            }
            val v = hitVocab
            if (hitIdx == -1 || v == null) {
                append(remaining); break
            }
            if (hitIdx > 0) append(remaining.substring(0, hitIdx))
            pushStringAnnotation("vocab", v.fa)
            withStyle(SpanStyle(color = PardisColors.indigoDeep, fontWeight = FontWeight.SemiBold, textDecoration = TextDecoration.Underline)) {
                append(v.fa)
            }
            pop()
            remaining = remaining.substring(hitIdx + v.fa.length)
        }
    }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ClickableText(
            text = annotated,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleMedium.copy(
                color = PardisColors.inkSoft,
                fontFamily = PardisFonts.persian,
                textAlign = TextAlign.Start,
            ),
            onClick = { offset ->
                annotated.getStringAnnotations("vocab", offset, offset).firstOrNull()?.let { ann ->
                    vocab.firstOrNull { it.fa == ann.item }?.let(onTap)
                }
            },
        )
    }
}
