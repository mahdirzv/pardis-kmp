package app.pardis.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.pardis.design.PardisRadius
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pardis.design.PardisColors
import app.pardis.design.PardisSpacing
import app.pardis.design.pardisScreenBackground
import app.pardis.shared.library.LibraryAction
import app.pardis.shared.library.LibraryUiState
import app.pardis.shared.library.LibraryViewModel
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.runtime.getValue

@Composable
fun LibraryRoute(
    onOpenStory: (String) -> Unit,
    bottomContentPadding: androidx.compose.ui.unit.Dp = PardisSpacing.none,
    viewModel: LibraryViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LibraryScreen(
        state = state,
        onAction = viewModel::onAction,
        onOpenStory = onOpenStory,
        bottomContentPadding = bottomContentPadding,
    )
}

@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onAction: (LibraryAction) -> Unit,
    onOpenStory: (String) -> Unit,
    bottomContentPadding: androidx.compose.ui.unit.Dp = PardisSpacing.none,
) {
    var grid by remember { mutableStateOf(true) }
    val gutter = PardisSpacing.lg
    Box(Modifier.fillMaxSize().pardisScreenBackground()) {
        PardisPatternOverlay(
            motif = PardisMotif.Vine,
            color = PardisColors.indigo,
            alpha = 0.05f,
            modifier = Modifier.fillMaxWidth().height(170.dp).align(Alignment.TopCenter),
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = PardisSpacing.xl, bottom = bottomContentPadding),
            verticalArrangement = Arrangement.spacedBy(PardisSpacing.md),
        ) {
            item {
                Row(
                    Modifier.padding(horizontal = gutter).fillMaxWidth().semantics { heading() },
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text("Library", style = MaterialTheme.typography.displayLarge, color = PardisColors.ink, fontWeight = FontWeight.ExtraBold)
                        PersianReaderInline("کتابخانه‌ی قصه‌ها", style = MaterialTheme.typography.bodyMedium, color = PardisColors.inkMuted)
                    }
                    GridListToggle(grid) { grid = it }
                }
            }
            item { LibrarySearchPill(state.searchQuery, { onAction(LibraryAction.Search(query = it)) }, Modifier.padding(horizontal = gutter)) }
            item {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = gutter),
                    horizontalArrangement = Arrangement.spacedBy(PardisSpacing.xs),
                ) {
                    PardisFilterPill("All ages", state.selectedAgeBand == null && !state.showOnlyCached, onClick = { onAction(LibraryAction.SetAgeBand(null)) })
                    state.ageBands.forEach { band ->
                        PardisFilterPill(band, state.selectedAgeBand == band, onClick = {
                            onAction(LibraryAction.SetAgeBand(if (state.selectedAgeBand == band) null else band))
                        })
                    }
                    PardisFilterPill(if (state.totalCachedLabel.isNotEmpty()) "Offline · ${state.totalCachedLabel}" else "Offline", state.showOnlyCached, onClick = {
                        onAction(LibraryAction.ToggleShowOnlyCached)
                    })
                }
            }
            if (state.isLoading && state.stories.isEmpty()) {
                item { Box(Modifier.fillMaxWidth().padding(gutter), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PardisColors.saffron) } }
            }
            state.errorMessage?.let { err ->
                item {
                    Column(Modifier.padding(horizontal = gutter)) {
                        Text("Error: $err", style = MaterialTheme.typography.bodyMedium, color = PardisColors.error)
                        Spacer(Modifier.height(PardisSpacing.xs))
                        Button(onClick = { onAction(LibraryAction.Refresh) }, colors = ButtonDefaults.buttonColors(containerColor = PardisColors.saffronSoft, contentColor = PardisColors.saffronDeep)) { Text("Retry") }
                    }
                }
            }
            if (grid) {
                items(state.stories.chunked(2)) { row ->
                    Row(Modifier.fillMaxWidth().padding(horizontal = gutter), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        row.forEach { s -> LibraryCover(s, state, onAction, onOpenStory, Modifier.weight(1f)) }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            } else {
                items(state.stories, key = { it.slug }) { s ->
                    LibraryListRow(s, state, onOpenStory, Modifier.padding(horizontal = gutter))
                }
            }
            if (state.ageBands.isNotEmpty()) {
                item {
                    Column(Modifier.padding(horizontal = gutter, vertical = PardisSpacing.sm)) {
                        PardisSectionHeader(title = "By age", subtitle = "بر اساس سن")
                        Spacer(Modifier.height(PardisSpacing.sm))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            val labels = listOf("Little ones", "Readers", "Explorers")
                            val tones = listOf("mint", "saffron", "lapis")
                            state.ageBands.take(3).forEachIndexed { i, band ->
                                LibraryAgeTile(band, labels.getOrElse(i) { "" }, tones[i % 3], Modifier.weight(1f)) {
                                    onAction(LibraryAction.SetAgeBand(if (state.selectedAgeBand == band) null else band))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GridListToggle(grid: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.backgroundAlt).padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SegmentBtn(grid, PardisIconKind.Grid) { onChange(true) }
        SegmentBtn(!grid, PardisIconKind.ListView) { onChange(false) }
    }
}

@Composable
private fun SegmentBtn(active: Boolean, icon: PardisIconKind, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(PardisRadius.full)).background(if (active) PardisColors.surface else Color.Transparent).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        PardisIcon(icon, contentDescription = null, tint = if (active) PardisColors.ink else PardisColors.inkMuted, size = 18.dp)
    }
}

@Composable
private fun LibrarySearchPill(query: String, onQuery: (String) -> Unit, modifier: Modifier) {
    var tfv by remember { mutableStateOf(TextFieldValue(query)) }
    LaunchedEffect(query) { if (query != tfv.text) tfv = TextFieldValue(query) }
    Row(
        modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.surface)
            .border(1.dp, PardisColors.border, RoundedCornerShape(PardisRadius.full)).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PardisIcon(PardisIconKind.Search, contentDescription = null, tint = PardisColors.inkMuted, size = 19.dp)
        Box(Modifier.weight(1f)) {
            if (tfv.text.isEmpty()) Text("Search heroes, words, voyages…", style = MaterialTheme.typography.bodyMedium, color = PardisColors.inkFaint)
            androidx.compose.foundation.text.BasicTextField(
                value = tfv,
                onValueChange = { tfv = it; onQuery(it.text) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = PardisColors.ink),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        PardisIcon(PardisIconKind.Mic, contentDescription = null, tint = PardisColors.saffron, size = 18.dp)
    }
}

@Composable
private fun LibraryCover(
    story: app.pardis.core.model.Story,
    state: LibraryUiState,
    onAction: (LibraryAction) -> Unit,
    onOpenStory: (String) -> Unit,
    modifier: Modifier,
) {
    val cover = state.localCoverUrls[story.slug] ?: story.coverUrl
    val cached = state.downloadedSizeLabels[story.slug] != null
    val progress = state.downloadProgress[story.slug]
    val failed = state.failedDownloads.contains(story.slug)
    Column(modifier.clickable { onOpenStory(story.slug) }) {
        Box(Modifier.fillMaxWidth().aspectRatio(0.78f).clip(RoundedCornerShape(PardisRadius.md))) {
            PardisRemoteImageFrame(imageUrl = cover, contentDescription = story.titleEn, modifier = Modifier.fillMaxSize())
            Box(
                Modifier.align(Alignment.TopEnd).padding(8.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.scrimSoft).padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text("${story.minutes}m", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkOnDark)
            }
            if (cached) {
                Box(
                    Modifier.align(Alignment.TopStart).padding(8.dp).size(24.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.mint),
                    contentAlignment = Alignment.Center,
                ) {
                    PardisIcon(PardisIconKind.Check, contentDescription = "Offline", tint = PardisColors.inkOnDark, size = 14.dp)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(story.titleEn, style = MaterialTheme.typography.titleMedium, color = PardisColors.ink, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        PersianReaderInline(story.titleFa, style = MaterialTheme.typography.bodySmall, color = PardisColors.indigo, maxLines = 1)
        Spacer(Modifier.height(6.dp))
        when {
            progress != null -> Text(progress, style = MaterialTheme.typography.labelSmall, color = PardisColors.inkMuted)
            cached -> Text("✓ Offline", style = MaterialTheme.typography.labelSmall, color = PardisColors.mintDeep, fontWeight = FontWeight.SemiBold)
            else -> ClickPill(if (failed) "Retry" else "Download") { onAction(LibraryAction.DownloadStory(story.slug)) }
        }
    }
}

@Composable
private fun ClickPill(label: String, onClick: () -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.saffronSoft).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        PardisIcon(PardisIconKind.Download, contentDescription = null, tint = PardisColors.saffronDeep, size = 14.dp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = PardisColors.saffronDeep, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LibraryListRow(
    story: app.pardis.core.model.Story,
    state: LibraryUiState,
    onOpenStory: (String) -> Unit,
    modifier: Modifier,
) {
    val cover = state.localCoverUrls[story.slug] ?: story.coverUrl
    Row(
        modifier.fillMaxWidth().clickable { onOpenStory(story.slug) }.padding(vertical = PardisSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        PardisRemoteImageFrame(imageUrl = cover, contentDescription = story.titleEn, modifier = Modifier.size(width = 60.dp, height = 78.dp))
        Column(Modifier.weight(1f)) {
            Text(story.titleEn, style = MaterialTheme.typography.titleMedium, color = PardisColors.ink, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            PersianReaderInline(story.titleFa, style = MaterialTheme.typography.bodySmall, color = PardisColors.indigo, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Text("${story.ageBand} · ${story.minutes}m · ${story.vocabCount} words", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkMuted)
        }
        PardisIcon(PardisIconKind.ChevRight, contentDescription = null, tint = PardisColors.inkFaint, size = 18.dp)
    }
}

@Composable
private fun LibraryAgeTile(band: String, label: String, tone: String, modifier: Modifier, onClick: () -> Unit) {
    val (soft, deep) = toneColors(tone)
    Column(
        modifier.clip(RoundedCornerShape(PardisRadius.base)).background(soft).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 16.dp),
    ) {
        Text(band, style = MaterialTheme.typography.titleLarge, color = deep, fontWeight = FontWeight.ExtraBold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = deep)
    }
}
