package app.pardis.android.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlin.math.roundToInt

@Composable
fun StoryFinishRoute(
    slug: String,
    onNextStory: (String) -> Unit,
    onDone: () -> Unit,
    viewModel: StoryFinishViewModel = koinViewModel(),
) {
    LaunchedEffect(slug) { viewModel.onAction(StoryFinishAction.Load(slug)) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StoryFinishScreen(
        sceneSeed = state.title.ifEmpty { slug },
        title = state.title,
        words = state.words,
        onNextStory = { state.nextSlug?.let(onNextStory) ?: onDone() },
        onDone = onDone,
    )
}

@Composable
fun StoryFinishScreen(
    sceneSeed: String,
    title: String,
    words: List<VocabItem>,
    onNextStory: () -> Unit,
    onDone: () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(PardisGradients.night)) {
        // nightsky motif faded from the top
        PardisPatternOverlay(
            motif = PardisMotif.Nightsky,
            color = PardisColors.inkOnDark,
            alpha = 0.07f,
            fade = PardisPatternFade.Top,
            modifier = Modifier.fillMaxWidth().height(540.dp).align(Alignment.TopCenter),
        )
        // radial glow behind the medallion
        Box(
            Modifier.size(460.dp).align(Alignment.TopCenter).offset(y = (-40).dp).background(
                Brush.radialGradient(
                    0.0f to Color(0x33F4B53A),
                    0.38f to Color(0x1A8B6FE6),
                    0.66f to Color.Transparent,
                ),
                shape = CircleShape,
            ),
        )
        ConfettiBurst(Modifier.matchParentSize())

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().padding(horizontal = PardisSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(28.dp))
            HeroSealMedallion(sceneSeed)
            Spacer(Modifier.height(18.dp))
            StarRow()
            Spacer(Modifier.height(18.dp))
            Text("CHAPTER COMPLETE", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkOnDarkMuted)
            Spacer(Modifier.height(7.dp))
            Text(
                "Âfarin, you did it!",
                style = MaterialTheme.typography.displayLarge,
                color = PardisColors.inkOnDark,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            PersianReaderInline("آفرین! یک قصه‌ی دیگر تمام شد", style = MaterialTheme.typography.titleMedium, color = PardisColors.inkOnDarkMuted)
            if (title.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text("You finished $title", style = MaterialTheme.typography.bodyMedium, color = PardisColors.inkOnDarkFaint)
            }

            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm)) {
                FinishStat(PardisIconKind.Flame, "+1", "night streak", PardisColors.saffron, Modifier.weight(1f))
                FinishStat(PardisIconKind.Feather, "+${words.size}", "new words", PardisColors.lilac, Modifier.weight(1f))
                FinishStat(PardisIconKind.StarFill, "+20", "stars", PardisColors.sun, Modifier.weight(1f))
            }

            if (words.isNotEmpty()) {
                Spacer(Modifier.height(26.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    PardisIcon(PardisIconKind.Sprout, contentDescription = null, tint = PardisColors.mint, size = 16.dp)
                    Text("NEW WORDS IN YOUR GARDEN", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkOnDarkMuted)
                }
                Spacer(Modifier.height(11.dp))
                GardenChips(words.take(6))
            }
            Spacer(Modifier.height(110.dp))
        }

        // CTA bar
        Row(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(horizontal = PardisSpacing.lg, vertical = PardisSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
        ) {
            Row(
                Modifier.weight(1f).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.inkOnDark).clickable(onClick = onNextStory).padding(vertical = 15.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Next story", style = MaterialTheme.typography.titleMedium, color = PardisColors.indigoDeep, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                PardisIcon(PardisIconKind.ChevRight, contentDescription = null, tint = PardisColors.indigoDeep, size = 18.dp)
            }
            Box(
                Modifier.width(116.dp).clip(RoundedCornerShape(PardisRadius.full)).border(1.5.dp, PardisColors.inkOnDarkFaint, RoundedCornerShape(PardisRadius.full)).clickable(onClick = onDone).padding(vertical = 15.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Done", style = MaterialTheme.typography.titleMedium, color = PardisColors.inkOnDark, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HeroSealMedallion(sceneSeed: String) {
    val spin = rememberInfiniteTransition(label = "seal")
    val angle by spin.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(26000, easing = LinearEasing)),
        label = "spin",
    )
    Box(Modifier.size(188.dp), contentAlignment = Alignment.Center) {
        // rotating dashed gold ring
        Canvas(Modifier.size(188.dp).rotate(angle)) {
            drawCircle(
                color = Color(0x73F4B53A),
                radius = size.minDimension / 2f - 1.dp.toPx(),
                style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f, 11f), 0f)),
            )
        }
        // static white ring
        Box(Modifier.size(162.dp).border(1.dp, PardisColors.surfaceOnDark, CircleShape))
        // scene art seal
        Box(Modifier.size(142.dp).clip(CircleShape)) {
            PardisSceneArt(seed = sceneSeed, modifier = Modifier.matchParentSize())
            PardisPatternOverlay(PardisMotif.Rosette, PardisColors.inkOnDark, alpha = 0.18f, fade = PardisPatternFade.Edges, modifier = Modifier.matchParentSize())
            Box(
                Modifier.matchParentSize().background(
                    Brush.radialGradient(0.3f to Color.Transparent, 1.0f to Color(0x73140C28)),
                ),
            )
        }
        // gold check seal
        Box(
            Modifier.align(Alignment.BottomCenter).offset(y = (-2).dp).size(50.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(PardisColors.sun, PardisColors.saffronDeep)))
                .border(2.dp, PardisColors.inkOnDark, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            PardisIcon(PardisIconKind.Check, contentDescription = null, tint = PardisColors.inkOnDark, size = 26.dp)
        }
    }
}

@Composable
private fun StarRow() {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        PardisIcon(PardisIconKind.StarFill, contentDescription = null, tint = PardisColors.sun, size = 32.dp)
        PardisIcon(PardisIconKind.StarFill, contentDescription = null, tint = PardisColors.sun, size = 40.dp, modifier = Modifier.offset(y = (-6).dp))
        PardisIcon(PardisIconKind.StarFill, contentDescription = null, tint = PardisColors.sun, size = 32.dp)
    }
}

@Composable
private fun FinishStat(icon: PardisIconKind, value: String, label: String, iconTint: Color, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(20.dp)).background(PardisColors.surfaceOnDark).padding(vertical = 15.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(PardisColors.surfaceOnDarkSoft),
            contentAlignment = Alignment.Center,
        ) {
            PardisIcon(icon, contentDescription = null, tint = iconTint, size = 21.dp)
        }
        Spacer(Modifier.height(8.dp))
        Text(value, style = MaterialTheme.typography.headlineSmall, color = PardisColors.inkOnDark, fontWeight = FontWeight.ExtraBold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = PardisColors.inkOnDarkMuted, textAlign = TextAlign.Center)
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun GardenChips(words: List<VocabItem>) {
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
    ) {
        words.forEach { w ->
            Row(
                Modifier.clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.surfaceOnDark).padding(horizontal = 13.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PersianReaderInline(w.fa, style = MaterialTheme.typography.titleMedium, color = PardisColors.inkOnDark)
                Text(w.translit, style = MaterialTheme.typography.labelSmall, color = PardisColors.inkOnDarkFaint)
            }
        }
    }
}

/** One-shot confetti burst: mixed sparkles, discs and bars falling once on entry. */
@Composable
private fun ConfettiBurst(modifier: Modifier) {
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) { t.animateTo(1f, tween(durationMillis = 3200, easing = LinearEasing)) }
    val colors = listOf(PardisColors.sun, PardisColors.saffron, PardisColors.lilac, PardisColors.mint, PardisColors.inkOnDark)
    BoxWithConstraints(modifier) {
        val w = maxWidth
        val h = maxHeight
        repeat(24) { i ->
            val leftFrac = ((i * 38 + 6) % 100) / 100f
            val delayFrac = (i % 9) * 0.05f
            val durFrac = 0.55f + (i % 5) * 0.1f
            val local = ((t.value - delayFrac) / durFrac).coerceIn(0f, 1f)
            val drift = (((i % 5) - 2) * 26).dp
            val sizeDp = (6 + (i % 4) * 2).dp
            val kind = i % 3
            val yFrac = -0.06f + (1.18f - (-0.06f)) * local
            val a = when {
                local <= 0f || local >= 1f -> 0f
                local < 0.12f -> local / 0.12f
                else -> 1f
            }
            val color = colors[i % colors.size]
            val mod = Modifier
                .offset(x = w * (leftFrac - 0.5f) * 2f * 0.5f + drift * local, y = h * yFrac)
                .align(Alignment.TopCenter)
                .rotate(540f * local)
                .alpha(a)
            when (kind) {
                0 -> PardisIcon(PardisIconKind.Sparkle, contentDescription = null, tint = color, size = sizeDp + 8.dp, modifier = mod)
                1 -> Box(mod.size(width = sizeDp, height = sizeDp / 2).background(color))
                else -> Box(mod.size(sizeDp).clip(CircleShape).background(color))
            }
        }
    }
}
