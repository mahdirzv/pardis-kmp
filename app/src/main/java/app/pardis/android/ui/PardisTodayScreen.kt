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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.pardis.design.PardisFonts
import app.pardis.design.PardisRadius
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import app.pardis.design.PardisColors
import app.pardis.design.PardisGradients
import app.pardis.design.PardisSpacing
import app.pardis.shared.library.LibraryAction
import app.pardis.shared.library.LibraryUiState
import androidx.compose.runtime.getValue

@Composable
internal fun TodayScreen(
    activeName: String,
    state: LibraryUiState,
    onAction: (LibraryAction) -> Unit,
    onOpenStory: (String) -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenBedtime: () -> Unit,
    bottomContentPadding: androidx.compose.ui.unit.Dp,
) {
    val gutter = PardisSpacing.lg
    val featured = state.stories.firstOrNull()
    Box(Modifier.fillMaxSize()) {
    PardisPatternOverlay(
        motif = PardisMotif.Paisley,
        color = PardisColors.saffronDeep,
        alpha = 0.05f,
        fade = PardisPatternFade.Top,
        modifier = Modifier.fillMaxWidth().height(260.dp).align(Alignment.TopCenter),
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = PardisSpacing.xl, bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(PardisSpacing.md),
    ) {
        item { TodayGreeting(activeName = activeName, modifier = Modifier.padding(horizontal = gutter).semantics { heading() }) }
        item { TodayStreakStrip(words = state.stories.sumOf { it.vocabCount }, modifier = Modifier.padding(horizontal = gutter)) }
        item {
            Column(Modifier.padding(horizontal = gutter)) {
                Text("CONTINUE READING", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkMuted)
                Spacer(Modifier.height(PardisSpacing.xs))
                if (featured != null) {
                    PardisFeaturedStoryCard(
                        titleEn = featured.titleEn,
                        titleFa = featured.titleFa,
                        ageBand = featured.ageBand,
                        minutes = featured.minutes,
                        vocabCount = featured.vocabCount,
                        coverUrl = state.localCoverUrls[featured.slug] ?: featured.coverUrl,
                        eyebrow = "Continue reading",
                        actionLabel = "Start reading",
                        onOpen = { onOpenStory(featured.slug) },
                    )
                } else {
                    PardisPanel {
                        Text(
                            text = if (state.isLoading) "Loading today's stories..." else "Refresh Library to load today's reading list.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = PardisColors.inkSoft,
                        )
                    }
                }
            }
        }
        item { TonightBedtimeCard(modifier = Modifier.padding(horizontal = gutter), onOpen = onOpenBedtime) }
        item {
            Column {
                PardisSectionHeader(
                    title = "New this week",
                    subtitle = "تازه‌ها",
                    actionLabel = "See all",
                    onAction = onOpenLibrary,
                    modifier = Modifier.padding(horizontal = gutter),
                )
                Spacer(Modifier.height(PardisSpacing.sm))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
                ) {
                    Spacer(Modifier.width(gutter - PardisSpacing.sm))
                    state.stories.forEach { s ->
                        TodayShelfCover(
                            titleEn = s.titleEn,
                            titleFa = s.titleFa,
                            coverUrl = state.localCoverUrls[s.slug] ?: s.coverUrl,
                            onClick = { onOpenStory(s.slug) },
                        )
                    }
                    Spacer(Modifier.width(gutter - PardisSpacing.sm))
                }
            }
        }
        item { WordOfDayCard(modifier = Modifier.padding(horizontal = gutter)) }
        item {
            Column {
                PardisSectionHeader(
                    title = "Explore collections",
                    subtitle = "مجموعه‌ها",
                    modifier = Modifier.padding(horizontal = gutter),
                )
                Spacer(Modifier.height(PardisSpacing.sm))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
                ) {
                    Spacer(Modifier.width(gutter - PardisSpacing.sm))
                    listOf(
                        Triple("Shahnameh Heroes", "پهلوانان", 0),
                        Triple("Creatures of Myth", "هیولاها", 5),
                        Triple("Voyages", "سفرها", 4),
                    ).forEach { (name, fa, variant) ->
                        CollectionCard(name = name, fa = fa, sceneVariant = variant, onClick = onOpenLibrary)
                    }
                    Spacer(Modifier.width(gutter - PardisSpacing.sm))
                }
            }
        }
        item {
            Text(
                "پایانِ امروز · فردا قصه‌ای تازه",
                style = MaterialTheme.typography.bodySmall,
                color = PardisColors.inkFaint,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = PardisSpacing.md),
            )
        }
    }
    }
}

@Composable
private fun TodayGreeting(activeName: String, modifier: Modifier = Modifier) {
    val cal = java.util.Calendar.getInstance()
    val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }
    val weekday = java.text.SimpleDateFormat("EEEE", java.util.Locale.US).format(java.util.Date())
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            // Eyebrow is greeting + weekday (no name); the name goes on the Salâm title.
            Text("$greeting · $weekday".uppercase(), style = MaterialTheme.typography.labelSmall, color = PardisColors.saffronDeep)
            Text("Salâm, $activeName", style = MaterialTheme.typography.displayLarge, color = PardisColors.ink, fontWeight = FontWeight.ExtraBold)
            PersianReaderInline("سلام، روزت پر از قصه", style = MaterialTheme.typography.bodyMedium, color = PardisColors.inkMuted)
        }
        Box(
            Modifier.size(46.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.saffron),
            contentAlignment = Alignment.Center,
        ) {
            Text(activeName.take(1), style = MaterialTheme.typography.titleMedium, color = PardisColors.inkOnDark, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TodayStreakStrip(words: Int, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm)) {
        StreakTile(Modifier.weight(1f), PardisIconKind.Flame, "7 nights", "reading streak", PardisColors.saffronTint, PardisColors.saffronSoft, PardisColors.saffronDeep)
        StreakTile(Modifier.weight(1f), PardisIconKind.Feather, "$words words", "collected", PardisColors.indigoTint, PardisColors.indigoSoft, PardisColors.indigoDeep)
    }
}

@Composable
private fun StreakTile(modifier: Modifier, icon: PardisIconKind, value: String, label: String, bg: Color, border: Color, fg: Color) {
    Row(
        modifier.clip(RoundedCornerShape(PardisRadius.base)).background(bg)
            .border(1.dp, border, RoundedCornerShape(PardisRadius.base))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
    ) {
        PardisIcon(icon, contentDescription = null, tint = fg)
        Column {
            Text(value, style = MaterialTheme.typography.titleMedium, color = fg, fontWeight = FontWeight.ExtraBold)
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = fg)
        }
    }
}

@Composable
private fun TonightBedtimeCard(modifier: Modifier = Modifier, onOpen: () -> Unit) {
    val onNight = PardisColors.inkOnDarkMuted
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(PardisRadius.lg))
            .background(PardisGradients.night)
            .clickable(onClick = onOpen),
    ) {
        PardisPatternOverlay(PardisMotif.Rosette, PardisColors.inkOnDark, alpha = 0.10f, modifier = Modifier.matchParentSize())
    Row(
        Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PardisSceneArt(seed = "tonight", forcedVariant = 6, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(PardisRadius.base)))
        Column(Modifier.weight(1f)) {
            Text("TONIGHT'S BEDTIME", style = MaterialTheme.typography.labelSmall, color = onNight)
            Text("Laay Laay, Little Star", style = MaterialTheme.typography.titleMedium, color = PardisColors.inkOnDark, fontWeight = FontWeight.Bold)
            Text("12 min · sleep timer ready", style = MaterialTheme.typography.bodySmall, color = onNight)
        }
        Box(
            Modifier.size(44.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.surfaceOnDark),
            contentAlignment = Alignment.Center,
        ) {
            PardisIcon(PardisIconKind.Moon, contentDescription = null, tint = PardisColors.inkOnDark)
        }
    }
    }
}

@Composable
private fun TodayShelfCover(titleEn: String, titleFa: String, coverUrl: String?, onClick: () -> Unit) {
    Column(Modifier.width(150.dp).clickable(onClick = onClick)) {
        PardisRemoteImageFrame(
            imageUrl = coverUrl,
            contentDescription = titleEn,
            modifier = Modifier.fillMaxWidth().height(132.dp),
        )
        Spacer(Modifier.height(PardisSpacing.xs))
        Text(titleEn, style = MaterialTheme.typography.titleMedium, color = PardisColors.ink, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        PersianReaderInline(titleFa, style = MaterialTheme.typography.bodySmall, color = PardisColors.inkMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun WordOfDayCard(modifier: Modifier = Modifier) {
    Box(
        modifier.fillMaxWidth().clip(RoundedCornerShape(PardisRadius.lg)).background(PardisColors.lilacSoft)
            .border(1.dp, PardisColors.lilac.copy(alpha = 0.22f), RoundedCornerShape(PardisRadius.lg)),
    ) {
    PardisPatternOverlay(PardisMotif.Vine, PardisColors.lilacDeep, alpha = 0.10f, fade = PardisPatternFade.TopRight, modifier = Modifier.matchParentSize())
    Column(Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("WORD OF THE DAY", style = MaterialTheme.typography.labelSmall, color = PardisColors.lilacDeep)
            PardisIcon(PardisIconKind.Volume, contentDescription = null, tint = PardisColors.lilacDeep)
        }
        Spacer(Modifier.height(PardisSpacing.xs))
        // Hero word is the handoff's literal 40sp (no nearer type token); translit is mono.
        PersianReaderParagraph("دلیر", style = MaterialTheme.typography.displayLarge.copy(fontSize = 40.sp), color = PardisColors.lilacDeep)
        Text("delir — \"brave\"", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PardisFonts.mono), color = PardisColors.lilacDeep)
        Spacer(Modifier.height(PardisSpacing.xs))
        Text(
            "A delir heart fears nothing.",
            style = MaterialTheme.typography.bodyMedium,
            color = PardisColors.inkSoft,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
        )
    }
    }
}

@Composable
private fun CollectionCard(name: String, fa: String, sceneVariant: Int, onClick: () -> Unit) {
    Box(
        Modifier.width(168.dp).height(110.dp).clip(RoundedCornerShape(PardisRadius.lg)).clickable(onClick = onClick),
    ) {
        PardisSceneArt(seed = name, forcedVariant = sceneVariant, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, PardisColors.scrim))))
        Column(Modifier.align(Alignment.BottomStart).padding(13.dp)) {
            Text(name, style = MaterialTheme.typography.titleMedium, color = PardisColors.inkOnDark, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            PersianReaderInline(fa, style = MaterialTheme.typography.bodySmall, color = PardisColors.inkOnDarkSoft, maxLines = 1)
        }
    }
}
