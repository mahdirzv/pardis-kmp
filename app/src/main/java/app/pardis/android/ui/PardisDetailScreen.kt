package app.pardis.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pardis.core.model.Story
import app.pardis.design.PardisColors
import app.pardis.design.PardisRadius
import app.pardis.design.PardisSpacing
import app.pardis.shared.detail.StoryDetailAction
import app.pardis.shared.detail.StoryDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun StoryDetailRoute(
    slug: String,
    onRead: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: StoryDetailViewModel = koinViewModel(),
) {
    LaunchedEffect(slug) {
        viewModel.onAction(StoryDetailAction.LoadStory(slug))
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StoryDetailScreen(
        story = state.story,
        canResume = state.canResume,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onRead = onRead,
        onBack = onBack,
        onRetry = { viewModel.onAction(StoryDetailAction.Retry) },
    )
}

@Composable
fun StoryDetailScreen(
    story: Story?,
    canResume: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onRead: (String) -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(PardisColors.background)) {
        when {
            story != null -> {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    DetailHero(story, onBack)
                    DetailMetaChips(story, Modifier.padding(horizontal = PardisSpacing.lg, vertical = PardisSpacing.sm))
                    DetailSynopsis(story, Modifier.padding(horizontal = PardisSpacing.lg))
                    DetailNarratorNote(Modifier.padding(PardisSpacing.lg))
                    Spacer(Modifier.height(96.dp))
                }
                DetailCtaBar(
                    label = if (canResume) "Continue reading" else "Start reading",
                    onRead = { onRead(story.slug) },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
            isLoading -> CircularProgressIndicator(
                color = PardisColors.saffron,
                modifier = Modifier.align(Alignment.Center),
            )
            else -> Column(
                Modifier.align(Alignment.Center).padding(PardisSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
            ) {
                Text(errorMessage ?: "Couldn't load this story.", style = MaterialTheme.typography.bodyLarge, color = PardisColors.inkSoft)
                Row(
                    Modifier.clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.saffronSoft).clickable(onClick = onRetry).padding(horizontal = 16.dp, vertical = 9.dp),
                ) {
                    Text("Retry", style = MaterialTheme.typography.labelLarge, color = PardisColors.saffronDeep, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun DetailHero(story: Story, onBack: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(360.dp)) {
        PardisRemoteImageFrame(
            imageUrl = story.coverUrl,
            contentDescription = story.titleEn,
            modifier = Modifier.fillMaxSize(),
        )
        PardisPatternOverlay(PardisMotif.Paisley, PardisColors.inkOnDark, alpha = 0.12f, fade = PardisPatternFade.BottomLeft, modifier = Modifier.matchParentSize())
        // Scrim: subtle dark at top (for the controls), strong fade to page bg at bottom (for the title).
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(
                    0.0f to PardisColors.scrimSoft,
                    0.28f to androidx.compose.ui.graphics.Color.Transparent,
                    0.62f to androidx.compose.ui.graphics.Color.Transparent,
                    1.0f to PardisColors.background,
                ),
            ),
        )
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = PardisSpacing.md, vertical = PardisSpacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            HeroCircleButton(PardisIconKind.Back, "Back", onBack)
            HeroCircleButton(PardisIconKind.Heart, "Save", {})
        }
        Column(
            Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(horizontal = PardisSpacing.lg, vertical = PardisSpacing.md),
        ) {
            Text(story.titleEn, style = MaterialTheme.typography.displayLarge, color = PardisColors.ink, fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            PersianReaderInline(story.titleFa, style = MaterialTheme.typography.titleMedium, color = PardisColors.inkSoft)
        }
    }
}

@Composable
private fun HeroCircleButton(icon: PardisIconKind, label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.surface).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        PardisIcon(icon, contentDescription = label, tint = PardisColors.ink, size = 19.dp)
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun DetailMetaChips(story: Story, modifier: Modifier) {
    FlowRow(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
    ) {
        MetaChip(PardisIconKind.Clock, "${story.minutes} min")
        MetaChip(PardisIconKind.Book, "${story.pageCount} pages")
        MetaChip(PardisIconKind.User, "Age ${story.ageBand}")
        MetaChip(PardisIconKind.Feather, "${story.vocabCount} words")
    }
}

@Composable
private fun MetaChip(icon: PardisIconKind, label: String) {
    Row(
        Modifier.clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.surface).padding(horizontal = 11.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        PardisIcon(icon, contentDescription = null, tint = PardisColors.inkMuted, size = 13.dp)
        Text(label, style = MaterialTheme.typography.labelLarge, color = PardisColors.inkSoft)
    }
}

@Composable
private fun DetailSynopsis(story: Story, modifier: Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(PardisSpacing.sm)) {
        Text(
            story.blurbEn ?: "A new Persian tale to read together.",
            style = MaterialTheme.typography.titleMedium,
            color = PardisColors.ink,
            fontWeight = FontWeight.Normal,
        )
        story.blurbFa?.let {
            PersianReaderInline(it, style = MaterialTheme.typography.bodyMedium, color = PardisColors.inkMuted)
        }
    }
}

@Composable
private fun DetailNarratorNote(modifier: Modifier) {
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(PardisRadius.lg)).background(PardisColors.indigoTint).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.indigo),
            contentAlignment = Alignment.Center,
        ) {
            PardisIcon(PardisIconKind.Mic, contentDescription = null, tint = PardisColors.inkOnDark, size = 18.dp)
        }
        Column(Modifier.weight(1f)) {
            Text("Narrated in English & Persian", style = MaterialTheme.typography.titleSmall, color = PardisColors.indigoDeep, fontWeight = FontWeight.Bold)
            Text("Word-by-word read-along · tap any Persian word", style = MaterialTheme.typography.bodySmall, color = PardisColors.indigoDeep)
        }
        PardisIcon(PardisIconKind.Languages, contentDescription = null, tint = PardisColors.indigoDeep, size = 20.dp)
    }
}

@Composable
private fun DetailCtaBar(label: String, onRead: () -> Unit, modifier: Modifier) {
    Box(
        modifier.fillMaxWidth().background(
            Brush.verticalGradient(
                0f to androidx.compose.ui.graphics.Color.Transparent,
                0.3f to PardisColors.background,
            ),
        ).navigationBarsPadding().padding(horizontal = PardisSpacing.lg, vertical = PardisSpacing.md),
    ) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.saffron).clickable(onClick = onRead).padding(vertical = 15.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PardisIcon(PardisIconKind.Play, contentDescription = null, tint = PardisColors.inkOnDark, size = 18.dp)
            Spacer(Modifier.size(8.dp))
            Text(label, style = MaterialTheme.typography.titleMedium, color = PardisColors.inkOnDark, fontWeight = FontWeight.Bold)
        }
    }
}
