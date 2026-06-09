package app.pardis.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.pardis.design.PardisRadius
import app.pardis.design.PardisColors
import app.pardis.design.PardisGradients
import app.pardis.design.PardisSpacing
import app.pardis.core.model.RivanaBadge
import app.pardis.core.model.RivanaCharacter
import app.pardis.core.model.RivanaContent
import app.pardis.core.model.RivanaWord
import androidx.compose.runtime.getValue

@Composable
internal fun RewardsScreen(storyCount: Int, onOpenCharacter: (Int) -> Unit, bottomContentPadding: androidx.compose.ui.unit.Dp) {
    val gutter = PardisSpacing.lg
    val mastered = RivanaContent.words.count { it.mastery >= 1f }
    val growing = RivanaContent.words.size - mastered
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(PardisColors.background),
        contentPadding = PaddingValues(top = PardisSpacing.xl, bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(PardisSpacing.md),
    ) {
        item {
            Row(
                Modifier.padding(horizontal = gutter).fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("Rewards", style = MaterialTheme.typography.displayLarge, color = PardisColors.ink, fontWeight = FontWeight.ExtraBold)
                    PersianReaderInline("جایزه‌ها و دستاوردها", style = MaterialTheme.typography.bodyMedium, color = PardisColors.inkMuted)
                }
                Row(
                    Modifier.clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.saffronSoft).padding(horizontal = 13.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    PardisIcon(PardisIconKind.Star, contentDescription = null, tint = PardisColors.saffronDeep, size = 16.dp)
                    Text("320", style = MaterialTheme.typography.titleMedium, color = PardisColors.saffronDeep, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
        item { RewardLevelHero(Modifier.padding(horizontal = gutter)) }
        item { RewardStatsStrip(words = mastered, stories = storyCount, modifier = Modifier.padding(horizontal = gutter)) }
        item { StreakCalendar(Modifier.padding(horizontal = gutter)) }
        item {
            Column(Modifier.padding(horizontal = gutter)) {
                Text("ALMOST THERE", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkMuted)
                Spacer(Modifier.height(PardisSpacing.sm))
                RivanaContent.badges.filter { !it.earned }.maxByOrNull { it.progress }?.let { NextBadgeCard(it) }
            }
        }
        item { WordGarden(mastered, growing, Modifier.padding(horizontal = gutter)) }
        item {
            Column {
                PardisSectionHeader(title = "Heroes met", subtitle = "پهلوانانِ آشنا", modifier = Modifier.padding(horizontal = gutter))
                Spacer(Modifier.height(PardisSpacing.sm))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(PardisSpacing.md),
                ) {
                    Spacer(Modifier.width(gutter - PardisSpacing.md))
                    RivanaContent.characters.forEachIndexed { i, c ->
                        HeroTile(c, onClick = { onOpenCharacter(i) })
                    }
                    Spacer(Modifier.width(gutter - PardisSpacing.md))
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = gutter)) {
                PardisSectionHeader(title = "Badges", subtitle = "نشان‌ها")
                Spacer(Modifier.height(PardisSpacing.sm))
                BadgesGrid()
            }
        }
    }
}

@Composable
private fun HeroTile(c: RivanaCharacter, onClick: () -> Unit) {
    Column(
        Modifier.width(84.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(84.dp).clip(RoundedCornerShape(22.dp))) {
            PardisSceneArt(seed = c.name, forcedVariant = c.sceneVariant, modifier = Modifier.matchParentSize())
            PardisPatternOverlay(PardisMotif.Rosette, PardisColors.inkOnDark, alpha = 0.14f, modifier = Modifier.matchParentSize())
            if (!c.collected) {
                Box(Modifier.matchParentSize().background(PardisColors.scrimSoft), contentAlignment = Alignment.Center) {
                    PardisIcon(PardisIconKind.Lock, contentDescription = null, tint = PardisColors.inkOnDark, size = 20.dp)
                }
            }
        }
        Spacer(Modifier.height(7.dp))
        Text(c.name, style = MaterialTheme.typography.labelLarge, color = if (c.collected) PardisColors.ink else PardisColors.inkMuted, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        PersianReaderInline(c.nameFa, style = MaterialTheme.typography.labelSmall, color = PardisColors.inkMuted, maxLines = 1)
    }
}

@Composable
private fun RewardLevelHero(modifier: Modifier) {
    Box(
        modifier.fillMaxWidth().clip(RoundedCornerShape(PardisRadius.xl))
            .background(PardisGradients.lapis),
    ) {
    PardisPatternOverlay(PardisMotif.Star8, PardisColors.inkOnDark, alpha = 0.12f, fade = PardisPatternFade.TopRight, modifier = Modifier.matchParentSize())
    Row(
        Modifier.fillMaxWidth().padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        PardisRing(progress = 320f / 500f, ringColor = PardisColors.sun, trackColor = PardisColors.surfaceOnDark, strokeWidthDp = 6f, modifier = Modifier.size(88.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("LVL", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkOnDarkMuted)
                Text("3", style = MaterialTheme.typography.displayLarge, color = PardisColors.inkOnDark, fontWeight = FontWeight.ExtraBold)
            }
        }
        Column(Modifier.weight(1f)) {
            Text("YOUR RANK", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkOnDarkMuted)
            Text("Story Keeper", style = MaterialTheme.typography.headlineSmall, color = PardisColors.inkOnDark, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("320 / 500 XP", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkOnDarkSoft)
                Text("180 to Lvl 4", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkOnDarkSoft)
            }
            Spacer(Modifier.height(5.dp))
            PardisProgressBar(value = 320f / 500f, height = 6, color = PardisColors.sun)
        }
    }
    }
}

@Composable
private fun RewardStatsStrip(words: Int, stories: Int, modifier: Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm)) {
        RewardStat(Modifier.weight(1f), PardisIconKind.Flame, "7", "streak", PardisColors.saffronDeep)
        RewardStat(Modifier.weight(1f), PardisIconKind.Feather, "$words", "words", PardisColors.mintDeep)
        RewardStat(Modifier.weight(1f), PardisIconKind.Crown, "4", "heroes", PardisColors.lilacDeep)
        RewardStat(Modifier.weight(1f), PardisIconKind.Book, "$stories", "stories", PardisColors.indigoDeep)
    }
}

@Composable
private fun RewardStat(modifier: Modifier, icon: PardisIconKind, value: String, label: String, deep: Color) {
    Column(
        modifier.clip(RoundedCornerShape(PardisRadius.base)).background(PardisColors.surface)
            .border(1.dp, PardisColors.border, RoundedCornerShape(PardisRadius.base)).padding(vertical = 13.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PardisIcon(icon, contentDescription = null, tint = deep)
        Text(value, style = MaterialTheme.typography.titleLarge, color = PardisColors.ink, fontWeight = FontWeight.ExtraBold)
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = PardisColors.inkMuted)
    }
}

@Composable
private fun StreakCalendar(modifier: Modifier) {
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    Box(
        modifier.fillMaxWidth().clip(RoundedCornerShape(PardisRadius.lg))
            .background(PardisGradients.saffron),
    ) {
    PardisPatternOverlay(PardisMotif.Paisley, PardisColors.inkOnDark, alpha = 0.14f, fade = PardisPatternFade.TopRight, modifier = Modifier.matchParentSize())
    Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                PardisIcon(PardisIconKind.Flame, contentDescription = null, tint = PardisColors.inkOnDark)
                Text("7-night streak", style = MaterialTheme.typography.titleMedium, color = PardisColors.inkOnDark, fontWeight = FontWeight.ExtraBold)
            }
            Text("Keep it lit!", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkOnDarkSoft)
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEach { d ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(
                        Modifier.size(32.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.inkOnDark),
                        contentAlignment = Alignment.Center,
                    ) {
                        PardisIcon(PardisIconKind.Flame, contentDescription = null, tint = PardisColors.saffronDeep, size = 16.dp)
                    }
                    Text(d, style = MaterialTheme.typography.labelSmall, color = PardisColors.inkOnDarkMuted)
                }
            }
        }
    }
    }
}

@Composable
private fun NextBadgeCard(b: RivanaBadge) {
    val (_, deep) = toneColors(b.tone)
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(PardisRadius.lg)).background(PardisColors.surface)
            .border(1.dp, PardisColors.border, RoundedCornerShape(PardisRadius.lg)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        PardisRing(progress = b.progress, ringColor = toneBase(b.tone), trackColor = PardisColors.backgroundAlt, strokeWidthDp = 5f, modifier = Modifier.size(60.dp)) {
            PardisIcon(b.icon.toPardisIconKind(), contentDescription = null, tint = deep)
        }
        Column(Modifier.weight(1f)) {
            Text(b.label, style = MaterialTheme.typography.titleMedium, color = PardisColors.ink, fontWeight = FontWeight.Bold)
            Text(b.desc, style = MaterialTheme.typography.bodySmall, color = PardisColors.inkMuted)
            Text("${(b.progress * 100).toInt()}% complete", style = MaterialTheme.typography.labelSmall, color = deep)
        }
    }
}

@Composable
private fun WordGarden(mastered: Int, growing: Int, modifier: Modifier) {
    Column(
        modifier.fillMaxWidth().clip(RoundedCornerShape(PardisRadius.lg))
            .border(1.dp, PardisColors.border, RoundedCornerShape(PardisRadius.lg)).background(PardisColors.surface),
    ) {
        Row(
            Modifier.fillMaxWidth().background(PardisColors.mintSoft).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Box(
                Modifier.size(38.dp).clip(RoundedCornerShape(PardisRadius.sm)).background(PardisColors.mint),
                contentAlignment = Alignment.Center,
            ) {
                PardisIcon(PardisIconKind.Sprout, contentDescription = null, tint = PardisColors.inkOnDark)
            }
            Column(Modifier.weight(1f)) {
                Text("${RivanaContent.words.size} words growing", style = MaterialTheme.typography.titleMedium, color = PardisColors.mintDeep, fontWeight = FontWeight.ExtraBold)
                Text("$mastered mastered · $growing sprouting", style = MaterialTheme.typography.labelSmall, color = PardisColors.mintDeep)
            }
        }
        RivanaContent.words.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { w -> WordCell(w, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun WordCell(w: RivanaWord, modifier: Modifier) {
    Column(modifier.padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        PardisRing(
            progress = w.mastery,
            ringColor = if (w.mastery >= 1f) PardisColors.mint else PardisColors.saffron,
            trackColor = PardisColors.backgroundAlt,
            strokeWidthDp = 4f,
            modifier = Modifier.size(46.dp),
        ) {
            PersianReaderInline(w.fa, style = MaterialTheme.typography.titleMedium, color = PardisColors.ink)
        }
        Text(w.tr, style = MaterialTheme.typography.labelSmall, color = PardisColors.inkMuted, modifier = Modifier.padding(top = 6.dp))
        Text(w.en, style = MaterialTheme.typography.bodySmall, color = PardisColors.inkSoft, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BadgesGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(PardisSpacing.sm)) {
        RivanaContent.badges.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm)) {
                row.forEach { b -> BadgeCell(b, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun BadgeCell(b: RivanaBadge, modifier: Modifier) {
    val (soft, deep) = toneColors(b.tone)
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(PardisRadius.md))
                .background(if (b.earned) soft else PardisColors.backgroundAlt),
            contentAlignment = Alignment.Center,
        ) {
            if (b.earned) {
                PardisIcon(b.icon.toPardisIconKind(), contentDescription = b.label, tint = deep, size = 32.dp)
            } else {
                PardisIcon(PardisIconKind.Lock, contentDescription = "Locked: ${b.label}", tint = PardisColors.inkFaint, size = 24.dp)
                if (b.progress > 0f) {
                    Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 12.dp, vertical = 11.dp)) {
                        PardisProgressBar(value = b.progress, height = 4, color = toneBase(b.tone))
                    }
                }
            }
        }
        Text(
            b.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (b.earned) PardisColors.ink else PardisColors.inkMuted,
            modifier = Modifier.padding(top = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
