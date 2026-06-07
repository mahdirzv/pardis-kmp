package app.pardis.android.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pardis.core.model.VocabItem
import app.pardis.design.PardisColors
import app.pardis.design.PardisGradients
import app.pardis.design.PardisRadius
import app.pardis.design.PardisSpacing
import app.pardis.shared.finish.StoryFinishAction
import app.pardis.shared.finish.StoryFinishViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun StoryFinishRoute(
    slug: String,
    onReadAgain: (String) -> Unit,
    onDone: () -> Unit,
    viewModel: StoryFinishViewModel = koinViewModel(),
) {
    LaunchedEffect(slug) { viewModel.onAction(StoryFinishAction.Load(slug)) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StoryFinishScreen(
        words = state.words,
        onReadAgain = { onReadAgain(slug) },
        onDone = onDone,
    )
}

@Composable
fun StoryFinishScreen(
    words: List<VocabItem>,
    onReadAgain: () -> Unit,
    onDone: () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(PardisGradients.night)) {
        PardisPatternOverlay(PardisMotif.Rosette, PardisColors.inkOnDark, alpha = 0.10f, modifier = Modifier.matchParentSize())
        SparkleField(Modifier.matchParentSize())

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().padding(horizontal = PardisSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(40.dp))
            // Trophy badge
            Box(
                Modifier.size(108.dp).clip(RoundedCornerShape(30.dp)).background(PardisColors.surfaceOnDark),
                contentAlignment = Alignment.Center,
            ) {
                PardisIcon(PardisIconKind.Trophy, contentDescription = null, tint = PardisColors.sun, size = 54.dp)
            }
            Spacer(Modifier.height(22.dp))
            Text("CHAPTER COMPLETE", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkOnDarkMuted)
            Spacer(Modifier.height(8.dp))
            Text(
                "Âfarin, you did it!",
                style = MaterialTheme.typography.displayLarge,
                color = PardisColors.inkOnDark,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            PersianReaderInline("آفرین! یک قصه تمام شد", style = MaterialTheme.typography.titleMedium, color = PardisColors.inkOnDarkMuted)

            Spacer(Modifier.height(26.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm)) {
                FinishStat(PardisIconKind.Flame, "+1", "night streak", PardisColors.saffron)
                FinishStat(PardisIconKind.Feather, "+${words.size}", "new words", PardisColors.lilac)
                FinishStat(PardisIconKind.Star, "+20", "stars", PardisColors.sun)
            }

            if (words.isNotEmpty()) {
                Spacer(Modifier.height(30.dp))
                Text(
                    "WORDS ADDED TO YOUR GARDEN",
                    style = MaterialTheme.typography.labelSmall,
                    color = PardisColors.inkOnDarkFaint,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )
                Spacer(Modifier.height(10.dp))
                WordGardenChips(words.take(8))
            }
            Spacer(Modifier.height(110.dp))
        }

        // CTA bar
        Row(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(horizontal = PardisSpacing.lg, vertical = PardisSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
        ) {
            Row(
                Modifier.weight(1f).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.inkOnDark).clickable(onClick = onReadAgain).padding(vertical = 15.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PardisIcon(PardisIconKind.Refresh, contentDescription = null, tint = PardisColors.indigoDeep, size = 18.dp)
                Spacer(Modifier.width(8.dp))
                Text("Read again", style = MaterialTheme.typography.titleMedium, color = PardisColors.indigoDeep, fontWeight = FontWeight.Bold)
            }
            Box(
                Modifier.width(110.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.surfaceOnDark).clickable(onClick = onDone).padding(vertical = 15.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Done", style = MaterialTheme.typography.titleMedium, color = PardisColors.inkOnDark, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FinishStat(icon: PardisIconKind, value: String, label: String, iconTint: Color) {
    Column(
        Modifier.width(96.dp).clip(RoundedCornerShape(20.dp)).background(PardisColors.surfaceOnDark).padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PardisIcon(icon, contentDescription = null, tint = iconTint, size = 26.dp)
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.headlineSmall, color = PardisColors.inkOnDark, fontWeight = FontWeight.ExtraBold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = PardisColors.inkOnDarkMuted, textAlign = TextAlign.Center)
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun WordGardenChips(words: List<VocabItem>) {
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
    ) {
        words.forEach { w ->
            Row(
                Modifier.clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.surfaceOnDark).padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PersianReaderInline(w.fa, style = MaterialTheme.typography.titleMedium, color = PardisColors.inkOnDark)
                Text(w.translit, style = MaterialTheme.typography.labelSmall, color = PardisColors.inkOnDarkFaint)
            }
        }
    }
}

/** Twinkling sparkles scattered over the celebration, mirroring the prototype's confetti. */
@Composable
private fun SparkleField(modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "sparkles")
    val tints = listOf(PardisColors.sun, PardisColors.saffron, PardisColors.lilac, PardisColors.inkOnDark)
    Box(modifier) {
        repeat(14) { i ->
            val phase = transition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2400, delayMillis = i * 130),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "tw$i",
            )
            val bx = ((i * 37) % 100) / 100f * 2f - 1f
            val by = ((i * 53) % 90) / 100f * 2f - 1f
            PardisIcon(
                PardisIconKind.Sparkle,
                contentDescription = null,
                tint = tints[i % tints.size],
                size = (if (i % 3 == 0) 22 else 15).dp,
                modifier = Modifier.align(BiasAlignment(bx, by)).alpha(phase.value),
            )
        }
    }
}
