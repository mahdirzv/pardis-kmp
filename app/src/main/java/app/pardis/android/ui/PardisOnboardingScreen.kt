package app.pardis.android.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.pardis.core.model.ChildProfile
import app.pardis.core.model.ProfileTone
import app.pardis.design.PardisColors
import app.pardis.design.PardisRadius
import app.pardis.design.PardisSpacing

/** Maps a profile tone to its avatar gradient using design tokens (no raw colors). */
internal fun toneGradient(tone: ProfileTone): Brush = when (tone) {
    ProfileTone.Saffron -> Brush.linearGradient(listOf(PardisColors.saffron, PardisColors.saffronDeep))
    ProfileTone.Lapis -> Brush.linearGradient(listOf(PardisColors.indigo, PardisColors.indigoDeep))
    ProfileTone.Lilac -> Brush.linearGradient(listOf(PardisColors.lilac, PardisColors.lilacDeep))
}

/**
 * "Who's reading tonight?" profile picker. Used both as the first-launch gate and, with
 * [isSwitch] = true, as a switch-profile screen reached from the You tab (shows a back chevron).
 */
@Composable
internal fun PardisOnboardingScreen(
    profiles: List<ChildProfile>,
    isSwitch: Boolean,
    onSelect: (ChildProfile) -> Unit,
    onBack: () -> Unit,
    onComingSoon: () -> Unit,
) {
    if (isSwitch) BackHandler { onBack() }

    Box(Modifier.fillMaxSize().background(PardisColors.background)) {
        PardisPatternOverlay(
            motif = PardisMotif.Paisley,
            color = PardisColors.indigo,
            alpha = 0.05f,
            fade = PardisPatternFade.Top,
            modifier = Modifier.fillMaxWidth().height(420.dp).align(Alignment.TopCenter),
        )

        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            if (isSwitch) {
                Box(Modifier.padding(start = PardisSpacing.lg, top = PardisSpacing.sm)) {
                    Box(
                        Modifier.size(40.dp).clip(RoundedCornerShape(PardisRadius.full))
                            .background(PardisColors.surface).clickable(onClick = onBack),
                        contentAlignment = Alignment.Center,
                    ) {
                        PardisIcon(PardisIconKind.Back, contentDescription = "Back", tint = PardisColors.ink, size = 20.dp)
                    }
                }
            }

            Column(
                Modifier.fillMaxWidth().padding(top = if (isSwitch) 4.dp else 36.dp, start = PardisSpacing.lg, end = PardisSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("Rivana", style = MaterialTheme.typography.headlineSmall, color = PardisColors.ink, fontWeight = FontWeight.ExtraBold)
                    PersianReaderInline("ریوانا", style = MaterialTheme.typography.titleMedium, color = PardisColors.inkSoft)
                }
                Spacer(Modifier.height(22.dp))
                Text("Who's reading tonight?", style = MaterialTheme.typography.headlineMedium, color = PardisColors.ink, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(6.dp))
                PersianReaderInline("امشب کی قصه می‌خواند؟", style = MaterialTheme.typography.bodyLarge, color = PardisColors.inkSoft)
            }

            Spacer(Modifier.height(30.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = PardisSpacing.lg),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(profiles, key = { it.id }) { profile ->
                    ProfilePickCard(profile = profile, onClick = { onSelect(profile) })
                }
                item {
                    AddChildCard(onClick = onComingSoon)
                }
            }

            Box(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(vertical = PardisSpacing.lg),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    Modifier.clickable(onClick = onComingSoon).padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    PardisIcon(PardisIconKind.Shield, contentDescription = null, tint = PardisColors.indigo, size = 17.dp)
                    Text("I'm a parent", style = MaterialTheme.typography.labelLarge, color = PardisColors.inkSoft, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ProfilePickCard(profile: ChildProfile, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(PardisRadius.xl))
            .background(PardisColors.surface)
            .border(1.dp, PardisColors.border, RoundedCornerShape(PardisRadius.xl))
            .clickable(onClick = onClick).padding(vertical = 24.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(80.dp).clip(RoundedCornerShape(PardisRadius.full)).background(toneGradient(profile.tone)),
            contentAlignment = Alignment.Center,
        ) {
            Text(profile.name.take(1), style = MaterialTheme.typography.displayLarge, color = PardisColors.inkOnDark, fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(profile.name, style = MaterialTheme.typography.titleMedium, color = PardisColors.ink, fontWeight = FontWeight.ExtraBold)
            Text("AGE ${profile.age}", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkSoft)
        }
    }
}

@Composable
private fun AddChildCard(onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().heightIn(min = 168.dp).clip(RoundedCornerShape(PardisRadius.xl))
            .border(2.dp, PardisColors.border, RoundedCornerShape(PardisRadius.xl))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(56.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.backgroundAlt),
            contentAlignment = Alignment.Center,
        ) {
            Text("+", style = MaterialTheme.typography.headlineSmall, color = PardisColors.inkSoft, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        Text("Add child", style = MaterialTheme.typography.labelLarge, color = PardisColors.inkSoft, fontWeight = FontWeight.Bold)
    }
}
