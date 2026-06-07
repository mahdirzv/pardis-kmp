package app.pardis.android.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.pardis.design.PardisColors
import app.pardis.design.PardisRadius
import app.pardis.design.PardisSpacing
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@Composable
internal fun LullabyPlayerScreen(lullaby: Lullaby, onBack: () -> Unit) {
    var playing by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf(0.28f) }
    var timerMinutes by remember { mutableStateOf(30) }
    var showTimer by remember { mutableStateOf(false) }
    var rain by remember { mutableStateOf(true) }

    // Simulated transport — the app has no real lullaby audio yet, so playback is decorative.
    LaunchedEffect(playing) {
        while (playing) {
            delay(60)
            progress = if (progress >= 1f) 0f else progress + 0.0015f
        }
    }

    val totalSec = lullaby.minutes * 60
    fun fmt(s: Float): String {
        val t = s.toInt()
        return "${t / 60}:${(t % 60).toString().padStart(2, '0')}"
    }

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                0.0f to PardisColors.indigoDeep,
                0.6f to PardisColors.indigoDarker,
                1.0f to Color(0xFF0A0E22),
            ),
        ),
    ) {
        PardisPatternOverlay(PardisMotif.Rosette, PardisColors.inkOnDark, alpha = 0.06f, modifier = Modifier.matchParentSize())

        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            // top bar
            Row(
                Modifier.fillMaxWidth().padding(horizontal = PardisSpacing.lg, vertical = PardisSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LullabyGhostButton(PardisIconKind.ChevRight, "Close", onBack, rotate = true)
                Text("NOW PLAYING", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkOnDarkMuted)
                LullabyGhostButton(PardisIconKind.Heart, "Save", {})
            }

            // album art
            Box(Modifier.fillMaxWidth().padding(top = 26.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(280.dp).clip(RoundedCornerShape(36.dp)).border(1.dp, PardisColors.surfaceOnDark, RoundedCornerShape(36.dp))) {
                    PardisSceneArt(seed = lullaby.title, forcedVariant = lullaby.sceneVariant, modifier = Modifier.matchParentSize())
                    PardisPatternOverlay(PardisMotif.Paisley, PardisColors.inkOnDark, alpha = 0.12f, fade = PardisPatternFade.Edges, modifier = Modifier.matchParentSize())
                    MoonGlow(playing)
                }
            }

            Spacer(Modifier.weight(1f))

            Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = PardisSpacing.lg).padding(bottom = 22.dp)) {
                // meta
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(lullaby.title, style = MaterialTheme.typography.headlineSmall, color = PardisColors.inkOnDark, fontWeight = FontWeight.ExtraBold)
                    PersianReaderInline(lullaby.titleFa, style = MaterialTheme.typography.titleMedium, color = PardisColors.inkOnDarkMuted)
                    Spacer(Modifier.height(6.dp))
                    Text(lullaby.origin.uppercase(), style = MaterialTheme.typography.labelSmall, color = PardisColors.inkOnDarkFaint)
                }

                // waveform
                Spacer(Modifier.height(22.dp))
                Waveform(progress = progress, playing = playing, modifier = Modifier.fillMaxWidth().height(38.dp))
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(fmt(progress * totalSec), style = MaterialTheme.typography.labelSmall, color = PardisColors.inkOnDarkFaint)
                    Text("-${fmt(totalSec - progress * totalSec)}", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkOnDarkFaint)
                }

                // transport
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(26.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
                    LullabyGhostButton(PardisIconKind.ChevRight, "Previous", {}, rotate = false, flip = true, big = true)
                    Box(
                        Modifier.size(80.dp).clip(CircleShape).background(PardisColors.inkOnDark).clickable { playing = !playing },
                        contentAlignment = Alignment.Center,
                    ) {
                        PardisIcon(if (playing) PardisIconKind.Pause else PardisIconKind.Play, contentDescription = if (playing) "Pause" else "Play", tint = PardisColors.indigoDeep, size = 32.dp)
                    }
                    LullabyGhostButton(PardisIconKind.ChevRight, "Next", {}, big = true)
                }

                // sleep timer + ambient
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm)) {
                    LullabyPill(
                        icon = PardisIconKind.Clock,
                        iconTint = PardisColors.sun,
                        label = if (timerMinutes > 0) "Sleep in ${timerMinutes}m" else "Sleep timer",
                        active = false,
                        modifier = Modifier.weight(1f),
                        onClick = { showTimer = true },
                    )
                    LullabyPill(
                        icon = PardisIconKind.Volume,
                        iconTint = if (rain) PardisColors.sun else PardisColors.inkOnDarkMuted,
                        label = if (rain) "Rain on" else "Rain off",
                        active = rain,
                        modifier = Modifier.weight(1f),
                        onClick = { rain = !rain },
                    )
                }
            }
        }

        if (showTimer) {
            // System back closes the sheet rather than popping the lullaby route.
            BackHandler { showTimer = false }
            SleepTimerSheet(
                selected = timerMinutes,
                onSelect = { timerMinutes = it; showTimer = false },
                onDismiss = { showTimer = false },
            )
        }
    }
}

@Composable
private fun MoonGlow(playing: Boolean) {
    val transition = rememberInfiniteTransition(label = "glow")
    val a by transition.animateFloat(
        initialValue = if (playing) 0.5f else 0.7f,
        targetValue = if (playing) 1f else 0.7f,
        animationSpec = infiniteRepeatable(tween(2500), RepeatMode.Reverse),
        label = "glowAlpha",
    )
    Box(
        Modifier.fillMaxSize().alpha(if (playing) a else 0.6f).background(
            Brush.radialGradient(
                0.0f to PardisColors.inkOnDark.copy(alpha = 0.22f),
                0.6f to Color.Transparent,
            ),
        ),
    )
}

@Composable
private fun Waveform(progress: Float, playing: Boolean, modifier: Modifier) {
    val bars = 42
    val transition = rememberInfiniteTransition(label = "wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart),
        label = "wavePhase",
    )
    Canvas(modifier) {
        val gap = size.width / bars
        val barW = gap * 0.45f
        val played = (progress * bars).toInt()
        for (i in 0 until bars) {
            // deterministic base height + gentle animated wobble while playing
            val base = 0.25f + 0.75f * abs(sin(i * 1.7f))
            val wobble = if (playing) 0.12f * sin(phase + i * 0.5f) else 0f
            val h = (size.height * (base + wobble)).coerceIn(size.height * 0.12f, size.height)
            val x = i * gap + (gap - barW) / 2f
            val color = if (i <= played) PardisColors.inkOnDark else PardisColors.inkOnDark.copy(alpha = 0.28f)
            drawRoundRect(
                color = color,
                topLeft = Offset(x, (size.height - h) / 2f),
                size = Size(barW, h),
                cornerRadius = CornerRadius(barW / 2f, barW / 2f),
            )
        }
    }
}

@Composable
private fun LullabyGhostButton(icon: PardisIconKind, label: String, onClick: () -> Unit, rotate: Boolean = false, flip: Boolean = false, big: Boolean = false) {
    val s = if (big) 50.dp else 44.dp
    Box(
        Modifier.size(s).clip(CircleShape).background(PardisColors.surfaceOnDark).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        PardisIcon(
            icon,
            contentDescription = label,
            tint = PardisColors.inkOnDark,
            size = if (big) 22.dp else 20.dp,
            modifier = when {
                rotate -> Modifier.rotate(90f)
                flip -> Modifier.rotate(180f)
                else -> Modifier
            },
        )
    }
}

@Composable
private fun LullabyPill(icon: PardisIconKind, iconTint: Color, label: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier.height(46.dp).clip(RoundedCornerShape(PardisRadius.full))
            .background(if (active) PardisColors.sun.copy(alpha = 0.18f) else PardisColors.surfaceOnDark)
            .border(1.dp, PardisColors.surfaceOnDark, RoundedCornerShape(PardisRadius.full))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        PardisIcon(icon, contentDescription = null, tint = iconTint, size = 18.dp)
        Spacer(Modifier.size(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = PardisColors.inkOnDark, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SleepTimerSheet(selected: Int, onSelect: (Int) -> Unit, onDismiss: () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(PardisColors.scrimSoft).clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)).background(Color(0xFF171B3A))
                .navigationBarsPadding().padding(horizontal = PardisSpacing.lg).padding(top = PardisSpacing.sm, bottom = PardisSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.padding(vertical = PardisSpacing.sm).size(width = 40.dp, height = 4.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.surfaceOnDark))
            Text("Sleep timer", style = MaterialTheme.typography.titleLarge, color = PardisColors.inkOnDark, fontWeight = FontWeight.Bold)
            PersianReaderInline("زمان‌سنجِ خواب", style = MaterialTheme.typography.bodySmall, color = PardisColors.inkOnDarkFaint)
            Spacer(Modifier.height(PardisSpacing.md))
            listOf(15, 30, 45, 0).forEach { m ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp).height(52.dp).clip(RoundedCornerShape(PardisRadius.base))
                        .background(if (selected == m) PardisColors.surfaceOnDark else Color.Transparent)
                        .border(1.dp, PardisColors.surfaceOnDarkSoft, RoundedCornerShape(PardisRadius.base))
                        .clickable { onSelect(m) }.padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(if (m > 0) "$m minutes" else "End of lullaby", style = MaterialTheme.typography.titleMedium, color = PardisColors.inkOnDark, fontWeight = FontWeight.Bold)
                    if (selected == m) PardisIcon(PardisIconKind.Check, contentDescription = null, tint = PardisColors.sun, size = 20.dp)
                }
            }
        }
    }
}
