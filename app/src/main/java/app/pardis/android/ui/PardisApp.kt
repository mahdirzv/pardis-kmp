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
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.pardis.design.PardisRadius
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pardis.design.PardisColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.milliseconds
import app.pardis.design.PardisSpacing
import app.pardis.design.PardisTheme
import app.pardis.design.pardisScreenBackground
import app.pardis.shared.library.LibraryAction
import app.pardis.shared.library.LibraryUiState
import app.pardis.shared.library.LibraryViewModel
import app.pardis.shared.reader.ReaderAction
import app.pardis.shared.reader.ReaderUiState
import app.pardis.shared.reader.ReaderViewModel
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

private enum class PardisRootTab(
    val title: String,
    val subtitle: String,
    val icon: PardisIconKind,
) {
    Today("Today", "Daily reading rhythm", PardisIconKind.Home),
    Library("Library", "Persian heritage stories", PardisIconKind.Book),
    Bedtime("Bedtime", "Calmer stories for later", PardisIconKind.Moon),
    Rewards("Rewards", "Reading progress and badges", PardisIconKind.Star),
    You("You", "Family profile and preferences", PardisIconKind.User),
}

@Composable
fun PardisApp() {
    PardisTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "library") {
                composable("library") {
                    RootShellRoute(
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

@Composable
private fun RootShellRoute(
    onOpenStory: (String) -> Unit,
    viewModel: LibraryViewModel = koinViewModel(),
) {
    var selectedTab by remember { mutableStateOf(PardisRootTab.Library) }
    val tabs = remember { PardisRootTab.entries.toList() }
    val libraryState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pardisScreenBackground(),
    ) {
        when (selectedTab) {
            PardisRootTab.Today -> TodayScreen(
                state = libraryState,
                onAction = viewModel::onAction,
                onOpenStory = onOpenStory,
                onOpenLibrary = { selectedTab = PardisRootTab.Library },
                onOpenBedtime = { selectedTab = PardisRootTab.Bedtime },
                bottomContentPadding = PardisSpacing.xxl + PardisSpacing.xl,
            )
            PardisRootTab.Library -> LibraryScreen(
                state = libraryState,
                onAction = viewModel::onAction,
                onOpenStory = onOpenStory,
                bottomContentPadding = PardisSpacing.xxl + PardisSpacing.xl,
            )
            PardisRootTab.Bedtime -> BedtimeScreen(
                bottomContentPadding = PardisSpacing.xxl + PardisSpacing.xl,
            )
            PardisRootTab.Rewards -> RewardsScreen(
                storyCount = libraryState.stories.size,
                bottomContentPadding = PardisSpacing.xxl + PardisSpacing.xl,
            )
            PardisRootTab.You -> YouScreen(
                downloadCount = libraryState.cachedStorySlugs.size,
                bottomContentPadding = PardisSpacing.xxl + PardisSpacing.xl,
            )
            else -> PardisPlaceholderTabScreen(
                title = selectedTab.title,
                subtitle = selectedTab.subtitle,
                icon = selectedTab.icon,
            )
        }
        PardisBottomTabBar(
            items = tabs.map { PardisTabItem(label = it.title, icon = it.icon) },
            selectedIndex = tabs.indexOf(selectedTab),
            onSelect = { selectedTab = tabs[it] },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(PardisSpacing.md),
        )
    }
}

@Composable
private fun TodayScreen(
    state: LibraryUiState,
    onAction: (LibraryAction) -> Unit,
    onOpenStory: (String) -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenBedtime: () -> Unit,
    bottomContentPadding: androidx.compose.ui.unit.Dp,
) {
    val gutter = PardisSpacing.lg
    val featured = state.stories.firstOrNull()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = PardisSpacing.xl, bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(PardisSpacing.md),
    ) {
        item { TodayGreeting(Modifier.padding(horizontal = gutter).semantics { heading() }) }
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

@Composable
private fun TodayGreeting(modifier: Modifier = Modifier) {
    val cal = java.util.Calendar.getInstance()
    val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }
    val weekday = java.text.SimpleDateFormat("EEEE", java.util.Locale.ENGLISH).format(cal.time)
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("$greeting · $weekday".uppercase(), style = MaterialTheme.typography.labelSmall, color = PardisColors.saffronDeep)
            Text("Salâm", style = MaterialTheme.typography.displayLarge, color = PardisColors.ink, fontWeight = FontWeight.ExtraBold)
            PersianReaderInline("سلام، روزت پر از قصه", style = MaterialTheme.typography.bodyMedium, color = PardisColors.inkMuted)
        }
        Box(
            Modifier.size(46.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.saffron),
            contentAlignment = Alignment.Center,
        ) {
            Text("R", style = MaterialTheme.typography.titleMedium, color = PardisColors.inkOnDark, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TodayStreakStrip(words: Int, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm)) {
        StreakTile(Modifier.weight(1f), PardisIconKind.Flame, "7 nights", "reading streak", PardisColors.saffronTint, PardisColors.saffronDeep)
        StreakTile(Modifier.weight(1f), PardisIconKind.Feather, "$words words", "collected", PardisColors.indigoTint, PardisColors.indigoDeep)
    }
}

@Composable
private fun StreakTile(modifier: Modifier, icon: PardisIconKind, value: String, label: String, bg: Color, fg: Color) {
    Row(
        modifier.clip(RoundedCornerShape(PardisRadius.base)).background(bg).padding(horizontal = 14.dp, vertical = 11.dp),
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
    val onNight = Color(0x99FFFFFF)
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(PardisRadius.lg))
            .background(Brush.verticalGradient(listOf(Color(0xFF1A256E), Color(0xFF2436A1), Color(0xFF4F2EB5))))
            .clickable(onClick = onOpen)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PardisSceneArt(seed = "tonight", forcedVariant = 6, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(PardisRadius.base)))
        Column(Modifier.weight(1f)) {
            Text("TONIGHT'S BEDTIME", style = MaterialTheme.typography.labelSmall, color = onNight)
            Text("Laay Laay, Little Star", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text("12 min · sleep timer ready", style = MaterialTheme.typography.bodySmall, color = onNight)
        }
        Box(
            Modifier.size(44.dp).clip(RoundedCornerShape(PardisRadius.full)).background(Color(0x29FFFFFF)),
            contentAlignment = Alignment.Center,
        ) {
            PardisIcon(PardisIconKind.Moon, contentDescription = null, tint = Color.White)
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
        PersianReaderInline(titleFa, style = MaterialTheme.typography.bodySmall, color = PardisColors.indigo, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun WordOfDayCard(modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().clip(RoundedCornerShape(PardisRadius.lg)).background(PardisColors.lilacSoft).padding(18.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("WORD OF THE DAY", style = MaterialTheme.typography.labelSmall, color = PardisColors.lilacDeep)
            PardisIcon(PardisIconKind.Volume, contentDescription = null, tint = PardisColors.lilacDeep)
        }
        PersianReaderInline("دلیر", style = MaterialTheme.typography.displayLarge, color = PardisColors.lilacDeep)
        Text("delir — \"brave\"", style = MaterialTheme.typography.bodyMedium, color = PardisColors.lilacDeep)
        Spacer(Modifier.height(PardisSpacing.xs))
        Text(
            "A delir heart fears nothing.",
            style = MaterialTheme.typography.bodyMedium,
            color = PardisColors.inkSoft,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
        )
    }
}

@Composable
private fun CollectionCard(name: String, fa: String, sceneVariant: Int, onClick: () -> Unit) {
    Box(
        Modifier.width(168.dp).height(110.dp).clip(RoundedCornerShape(PardisRadius.lg)).clickable(onClick = onClick),
    ) {
        PardisSceneArt(seed = name, forcedVariant = sceneVariant, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0x99000000)))))
        Column(Modifier.align(Alignment.BottomStart).padding(13.dp)) {
            Text(name, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            PersianReaderInline(fa, style = MaterialTheme.typography.bodySmall, color = Color(0xCCFFFFFF), maxLines = 1)
        }
    }
}

private data class Lullaby(
    val title: String,
    val titleFa: String,
    val minutes: Int,
    val origin: String,
    val sceneVariant: Int,
    val plays: String,
)

private val rivanaLullabies = listOf(
    Lullaby("Moon Over Damavand", "ماه بر فرازِ دماوند", 18, "Traditional · Mazandaran", 6, "2.1k"),
    Lullaby("Laay Laay, Little Star", "لای‌لای، ستاره‌ی کوچک", 12, "Folk lullaby", 0, "4.8k"),
    Lullaby("The Sleepy River", "رودِ خواب‌آلود", 22, "Original · Rivana", 6, "1.3k"),
    Lullaby("Garden of Dreams", "باغِ رؤیاها", 15, "Traditional · Shiraz", 0, "3.6k"),
)

@Composable
private fun BedtimeScreen(bottomContentPadding: androidx.compose.ui.unit.Dp) {
    val gutter = PardisSpacing.lg
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A256E), Color(0xFF0F1330), Color(0xFF0A0E22)))),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = PardisSpacing.xl, bottom = bottomContentPadding),
            verticalArrangement = Arrangement.spacedBy(PardisSpacing.md),
        ) {
            item {
                Column(Modifier.padding(horizontal = gutter)) {
                    Text("SWEET DREAMS", style = MaterialTheme.typography.labelSmall, color = Color(0xBFFFE9D2))
                    Text("Bedtime", style = MaterialTheme.typography.displayLarge, color = Color.White, fontWeight = FontWeight.ExtraBold)
                    PersianReaderInline("وقتِ خواب · لای‌لای", style = MaterialTheme.typography.bodyMedium, color = Color(0x80FFFFFF))
                }
            }
            item { BedtimeFeatured(rivanaLullabies[0], Modifier.padding(horizontal = gutter)) }
            item { WindDownCard(Modifier.padding(horizontal = gutter)) }
            item {
                Text(
                    "Lullaby shelf",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = gutter, vertical = PardisSpacing.xs),
                )
            }
            items(rivanaLullabies.drop(1)) { l -> LullabyRow(l, Modifier.padding(horizontal = gutter)) }
            item {
                Text(
                    "شب بخیر · خواب‌های خوش",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0x4DFFFFFF),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = PardisSpacing.md),
                )
            }
        }
    }
}

@Composable
private fun BedtimeFeatured(l: Lullaby, modifier: Modifier) {
    Box(modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(PardisRadius.xl))) {
        PardisSceneArt(seed = l.title, forcedVariant = l.sceneVariant, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xB30F0C1E)))))
        Column(Modifier.align(Alignment.BottomStart).padding(18.dp)) {
            Row(
                Modifier.clip(RoundedCornerShape(PardisRadius.full)).background(Color(0x2EFFFFFF)).padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                PardisIcon(PardisIconKind.Moon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Text("Lullaby of the night", style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
            Spacer(Modifier.height(9.dp))
            Text(l.title, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
            PersianReaderInline(l.titleFa, style = MaterialTheme.typography.titleMedium, color = Color(0xA6FFFFFF))
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier.size(46.dp).clip(RoundedCornerShape(PardisRadius.full)).background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    PardisIcon(PardisIconKind.Play, contentDescription = "Play ${l.title}", tint = PardisColors.indigoDeep)
                }
                Text("${l.minutes} min · ${l.origin}", style = MaterialTheme.typography.bodySmall, color = Color(0xCCFFFFFF))
            }
        }
    }
}

@Composable
private fun WindDownCard(modifier: Modifier) {
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(PardisRadius.lg)).background(Color(0x0FFFFFFF)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box(
            Modifier.size(46.dp).clip(RoundedCornerShape(PardisRadius.base)).background(Color(0x2EF4B53A)),
            contentAlignment = Alignment.Center,
        ) {
            PardisIcon(PardisIconKind.Clock, contentDescription = null, tint = Color(0xFFF4B53A))
        }
        Column(Modifier.weight(1f)) {
            Text("Wind-down routine", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text("One short story, then a lullaby. ~25 min.", style = MaterialTheme.typography.bodySmall, color = Color(0x8CFFFFFF))
        }
        Box(
            Modifier.clip(RoundedCornerShape(PardisRadius.full)).background(Color(0x24FFFFFF)).padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text("Start", style = MaterialTheme.typography.labelLarge, color = Color.White)
        }
    }
}

@Composable
private fun LullabyRow(l: Lullaby, modifier: Modifier) {
    Row(
        modifier.fillMaxWidth().padding(vertical = PardisSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        PardisSceneArt(seed = l.title, forcedVariant = l.sceneVariant, modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)))
        Column(Modifier.weight(1f)) {
            Text(l.title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            PersianReaderInline(l.titleFa, style = MaterialTheme.typography.bodySmall, color = Color(0x80FFFFFF), maxLines = 1)
            Text("${l.minutes} min · ${l.plays} plays", style = MaterialTheme.typography.labelSmall, color = Color(0x66FFFFFF))
        }
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(PardisRadius.full)).background(Color(0x1FFFFFFF)),
            contentAlignment = Alignment.Center,
        ) {
            PardisIcon(PardisIconKind.Play, contentDescription = "Play ${l.title}", tint = Color.White)
        }
    }
}

private data class RBadge(val label: String, val desc: String, val icon: PardisIconKind, val tone: String, val earned: Boolean, val progress: Float = 0f)

private val rivanaBadges = listOf(
    RBadge("First Voyage", "Finished your first story", PardisIconKind.Compass, "mint", true),
    RBadge("7-Night Streak", "Read 7 nights in a row", PardisIconKind.Flame, "saffron", true),
    RBadge("Word Collector", "Learned 25 Persian words", PardisIconKind.Feather, "lapis", true),
    RBadge("Hero of Persia", "Met 5 Shahnameh heroes", PardisIconKind.Crown, "lilac", false, 0.6f),
    RBadge("Night Owl", "Listen to 10 lullabies", PardisIconKind.Moon, "lilac", false, 0.4f),
    RBadge("Storyteller", "Read aloud 3 times", PardisIconKind.Mic, "rose", false, 0.33f),
)

private data class RWord(val fa: String, val tr: String, val en: String, val mastery: Float)

private val rivanaWords = listOf(
    RWord("پهلوان", "pahlavân", "champion", 1f), RWord("شیر", "shir", "lion", 1f), RWord("ستاره", "setâre", "star", 0.66f),
    RWord("آتش", "âtash", "fire", 0.66f), RWord("پرنده", "parande", "bird", 1f), RWord("دریا", "daryâ", "sea", 0.33f),
    RWord("آب", "âb", "water", 1f), RWord("کوه", "kuh", "mountain", 0.33f), RWord("آسمان", "âsemân", "sky", 0.66f),
)

private fun toneColors(t: String): Pair<Color, Color> = when (t) {
    "mint" -> PardisColors.mintSoft to PardisColors.mintDeep
    "saffron" -> PardisColors.saffronSoft to PardisColors.saffronDeep
    "lapis" -> PardisColors.indigoSoft to PardisColors.indigoDeep
    else -> PardisColors.lilacSoft to PardisColors.lilacDeep
}

private fun toneBase(t: String): Color = when (t) {
    "mint" -> PardisColors.mint
    "saffron" -> PardisColors.saffron
    "lapis" -> PardisColors.indigo
    else -> PardisColors.lilac
}

@Composable
private fun RewardsScreen(storyCount: Int, bottomContentPadding: androidx.compose.ui.unit.Dp) {
    val gutter = PardisSpacing.lg
    val mastered = rivanaWords.count { it.mastery >= 1f }
    val growing = rivanaWords.size - mastered
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
                    PardisIcon(PardisIconKind.Star, contentDescription = null, tint = PardisColors.saffronDeep, modifier = Modifier.size(16.dp))
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
                rivanaBadges.filter { !it.earned }.maxByOrNull { it.progress }?.let { NextBadgeCard(it) }
            }
        }
        item { WordGarden(mastered, growing, Modifier.padding(horizontal = gutter)) }
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
private fun RewardLevelHero(modifier: Modifier) {
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(PardisRadius.xl))
            .background(Brush.linearGradient(listOf(Color(0xFF2436A1), Color(0xFF4F2EB5), Color(0xFF1A256E))))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        PardisRing(progress = 320f / 500f, ringColor = Color(0xFFF4B53A), trackColor = Color(0x33FFFFFF), strokeWidthDp = 6f, modifier = Modifier.size(88.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("LVL", style = MaterialTheme.typography.labelSmall, color = Color(0xB3FFFFFF))
                Text("3", style = MaterialTheme.typography.displayLarge, color = Color.White, fontWeight = FontWeight.ExtraBold)
            }
        }
        Column(Modifier.weight(1f)) {
            Text("YOUR RANK", style = MaterialTheme.typography.labelSmall, color = Color(0xB3FFFFFF))
            Text("Story Keeper", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("320 / 500 XP", style = MaterialTheme.typography.labelSmall, color = Color(0xD9FFFFFF))
                Text("180 to Lvl 4", style = MaterialTheme.typography.labelSmall, color = Color(0xD9FFFFFF))
            }
            Spacer(Modifier.height(5.dp))
            PardisProgressBar(value = 320f / 500f, height = 6, color = Color(0xFFF4B53A))
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
    Column(
        modifier.fillMaxWidth().clip(RoundedCornerShape(PardisRadius.lg))
            .background(Brush.linearGradient(listOf(Color(0xFFF08A2D), Color(0xFFF4B53A), Color(0xFFFFD08A))))
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                PardisIcon(PardisIconKind.Flame, contentDescription = null, tint = Color.White)
                Text("7-night streak", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.ExtraBold)
            }
            Text("Keep it lit!", style = MaterialTheme.typography.labelSmall, color = Color(0xD9FFFFFF))
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEach { d ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(
                        Modifier.size(32.dp).clip(RoundedCornerShape(PardisRadius.full)).background(Color.White),
                        contentAlignment = Alignment.Center,
                    ) {
                        PardisIcon(PardisIconKind.Flame, contentDescription = null, tint = PardisColors.saffronDeep, modifier = Modifier.size(16.dp))
                    }
                    Text(d, style = MaterialTheme.typography.labelSmall, color = Color(0xB3FFFFFF))
                }
            }
        }
    }
}

@Composable
private fun NextBadgeCard(b: RBadge) {
    val (_, deep) = toneColors(b.tone)
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(PardisRadius.lg)).background(PardisColors.surface)
            .border(1.dp, PardisColors.border, RoundedCornerShape(PardisRadius.lg)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        PardisRing(progress = b.progress, ringColor = toneBase(b.tone), trackColor = PardisColors.backgroundAlt, strokeWidthDp = 5f, modifier = Modifier.size(60.dp)) {
            PardisIcon(b.icon, contentDescription = null, tint = deep)
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
                PardisIcon(PardisIconKind.Sprout, contentDescription = null, tint = Color.White)
            }
            Column(Modifier.weight(1f)) {
                Text("${rivanaWords.size} words growing", style = MaterialTheme.typography.titleMedium, color = PardisColors.mintDeep, fontWeight = FontWeight.ExtraBold)
                Text("$mastered mastered · $growing sprouting", style = MaterialTheme.typography.labelSmall, color = PardisColors.mintDeep)
            }
        }
        rivanaWords.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { w -> WordCell(w, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun WordCell(w: RWord, modifier: Modifier) {
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
        rivanaBadges.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm)) {
                row.forEach { b -> BadgeCell(b, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun BadgeCell(b: RBadge, modifier: Modifier) {
    val (soft, deep) = toneColors(b.tone)
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(PardisRadius.md))
                .background(if (b.earned) soft else PardisColors.backgroundAlt),
            contentAlignment = Alignment.Center,
        ) {
            if (b.earned) {
                PardisIcon(b.icon, contentDescription = b.label, tint = deep, modifier = Modifier.size(32.dp))
            } else {
                PardisIcon(PardisIconKind.Lock, contentDescription = "Locked: ${b.label}", tint = PardisColors.inkFaint, modifier = Modifier.size(24.dp))
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

private data class SettingsItem(val icon: PardisIconKind, val tone: String, val label: String, val detail: String? = null)

@Composable
private fun YouScreen(downloadCount: Int, bottomContentPadding: androidx.compose.ui.unit.Dp) {
    val gutter = PardisSpacing.lg
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(PardisColors.background),
        contentPadding = PaddingValues(top = PardisSpacing.xl, bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(PardisSpacing.md),
    ) {
        item {
            Text("You", style = MaterialTheme.typography.displayLarge, color = PardisColors.ink, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = gutter))
        }
        item { YouProfileCard(Modifier.padding(horizontal = gutter)) }
        item { AppearanceGroup(Modifier.padding(horizontal = gutter)) }
        item {
            SettingsGroup(
                "Reading",
                listOf(
                    SettingsItem(PardisIconKind.Languages, "lapis", "Story language", "English & فارسی"),
                    SettingsItem(PardisIconKind.Volume, "saffron", "Narration speed", "Normal"),
                    SettingsItem(PardisIconKind.Download, "mint", "Downloads", "$downloadCount stories"),
                ),
                Modifier.padding(horizontal = gutter),
            )
        }
        item {
            SettingsGroup(
                "Family",
                listOf(
                    SettingsItem(PardisIconKind.Shield, "lapis", "Parents' corner", "Locked"),
                    SettingsItem(PardisIconKind.Bell, "saffron", "Bedtime reminder", "8:00 PM"),
                    SettingsItem(PardisIconKind.Star, "lilac", "Rivana Plus", "Active"),
                ),
                Modifier.padding(horizontal = gutter),
            )
        }
        item {
            SettingsGroup(
                "About",
                listOf(
                    SettingsItem(PardisIconKind.Settings, "lapis", "Settings"),
                    SettingsItem(PardisIconKind.Heart, "rose", "Rate Rivana"),
                ),
                Modifier.padding(horizontal = gutter),
            )
        }
        item {
            Column(Modifier.fillMaxWidth().padding(top = PardisSpacing.md), horizontalAlignment = Alignment.CenterHorizontally) {
                PersianReaderInline(
                    "ریوانا · قصه‌های پارسی برای کودکان",
                    style = MaterialTheme.typography.bodySmall,
                    color = PardisColors.inkFaint,
                )
                Text(
                    "PARDIS · v1.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = PardisColors.inkFaint,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun YouProfileCard(modifier: Modifier) {
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(PardisRadius.xl))
            .background(Brush.linearGradient(listOf(Color(0xFFFFE9D2), Color(0xFFECE6FB), Color(0xFFE8EBFB))))
            .border(1.dp, PardisColors.border, RoundedCornerShape(PardisRadius.xl)).padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            Modifier.size(72.dp).clip(RoundedCornerShape(PardisRadius.full))
                .background(Brush.linearGradient(listOf(PardisColors.saffron, PardisColors.saffronDeep))),
            contentAlignment = Alignment.Center,
        ) {
            Text("R", style = MaterialTheme.typography.displayLarge, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f)) {
            Text("Roya", style = MaterialTheme.typography.headlineSmall, color = PardisColors.ink, fontWeight = FontWeight.ExtraBold)
            Text("Age 7 · 7-night streak", style = MaterialTheme.typography.bodySmall, color = PardisColors.inkSoft)
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.surface).padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                PardisIcon(PardisIconKind.User, contentDescription = null, tint = PardisColors.ink, modifier = Modifier.size(15.dp))
                Text("Switch reader", style = MaterialTheme.typography.labelLarge, color = PardisColors.ink)
            }
        }
    }
}

@Composable
private fun AppearanceGroup(modifier: Modifier) {
    Column(modifier) {
        Text("APPEARANCE", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkMuted, modifier = Modifier.padding(bottom = PardisSpacing.sm))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(PardisRadius.lg)).border(1.dp, PardisColors.border, RoundedCornerShape(PardisRadius.lg)).background(PardisColors.surface).padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Box(
                Modifier.size(34.dp).clip(RoundedCornerShape(PardisRadius.sm)).background(PardisColors.lilacSoft),
                contentAlignment = Alignment.Center,
            ) {
                PardisIcon(PardisIconKind.Moon, contentDescription = null, tint = PardisColors.lilacDeep, modifier = Modifier.size(18.dp))
            }
            Text("Dark mode", style = MaterialTheme.typography.bodyLarge, color = PardisColors.ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            // Decorative off toggle (dark mode not wired yet)
            Box(
                Modifier.size(width = 46.dp, height = 28.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.border),
                contentAlignment = Alignment.CenterStart,
            ) {
                Box(Modifier.padding(start = 3.dp).size(22.dp).clip(RoundedCornerShape(PardisRadius.full)).background(Color.White))
            }
        }
    }
}

@Composable
private fun SettingsGroup(label: String, items: List<SettingsItem>, modifier: Modifier) {
    Column(modifier) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = PardisColors.inkMuted, modifier = Modifier.padding(bottom = PardisSpacing.sm))
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(PardisRadius.lg)).border(1.dp, PardisColors.border, RoundedCornerShape(PardisRadius.lg)).background(PardisColors.surface),
        ) {
            items.forEachIndexed { i, item ->
                if (i > 0) Box(Modifier.padding(start = 63.dp).fillMaxWidth().height(1.dp).background(PardisColors.border))
                SettingsRow(item)
            }
        }
    }
}

@Composable
private fun SettingsRow(item: SettingsItem) {
    val (soft, deep) = toneColors(item.tone)
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Box(
            Modifier.size(34.dp).clip(RoundedCornerShape(PardisRadius.sm)).background(soft),
            contentAlignment = Alignment.Center,
        ) {
            PardisIcon(item.icon, contentDescription = null, tint = deep, modifier = Modifier.size(18.dp))
        }
        Text(item.label, style = MaterialTheme.typography.bodyLarge, color = PardisColors.ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (item.detail != null) {
            Text(item.detail, style = MaterialTheme.typography.bodySmall, color = PardisColors.inkMuted)
        }
        PardisIcon(PardisIconKind.ChevRight, contentDescription = null, tint = PardisColors.inkFaint, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun PardisPlaceholderTabScreen(
    title: String,
    subtitle: String,
    icon: PardisIconKind,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PardisSpacing.md)
            .padding(bottom = PardisSpacing.xxl + PardisSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(PardisSpacing.md),
    ) {
        PardisScreenHeader(title = title, subtitle = subtitle)
        PardisPanel {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PardisIcon(icon, contentDescription = null, tint = PardisColors.indigo)
                Text(
                    text = "$title is ready for its shared state contract.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = PardisColors.inkSoft,
                )
            }
        }
    }
}

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .pardisScreenBackground()
            .padding(PardisSpacing.md)
    ) {
        PardisScreenHeader(
            title = "Pardis",
            subtitle = "Persian heritage stories",
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(PardisSpacing.sm))
        PardisMetricStrip(
            metrics = listOf(
                PardisMetric(
                    value = state.stories.size.toString(),
                    label = "Stories",
                    tone = PardisMetricTone.Saffron,
                ),
                PardisMetric(
                    value = state.ageBands.size.toString(),
                    label = "Age bands",
                    tone = PardisMetricTone.Indigo,
                ),
                PardisMetric(
                    value = state.cachedStorySlugs.size.toString(),
                    label = if (state.totalCachedLabel.isNotEmpty()) state.totalCachedLabel else "Offline",
                    tone = PardisMetricTone.Mint,
                ),
            ),
        )
        Spacer(Modifier.height(PardisSpacing.sm))
        state.stories.firstOrNull()?.let { story ->
            PardisFeaturedStoryCard(
                titleEn = story.titleEn,
                titleFa = story.titleFa,
                ageBand = story.ageBand,
                minutes = story.minutes,
                vocabCount = story.vocabCount,
                coverUrl = state.localCoverUrls[story.slug] ?: story.coverUrl,
                blurb = story.blurbEn,
                onOpen = { onOpenStory(story.slug) },
            )
            Spacer(Modifier.height(PardisSpacing.sm))
        }
        PardisPanel {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onAction(LibraryAction.Search(query = it)) },
                label = { Text("Search stories") },
                leadingIcon = {
                    PardisIcon(PardisIconKind.Search, contentDescription = null, tint = PardisColors.inkMuted)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        Spacer(Modifier.height(PardisSpacing.sm))
        // Filter chips: age bands + an offline-cached toggle. Tapping the active band again clears it.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(PardisSpacing.xs)
        ) {
            PardisFilterPill(
                label = "All ages",
                selected = state.selectedAgeBand == null && !state.showOnlyCached,
                onClick = { onAction(LibraryAction.SetAgeBand(null)) },
            )
            state.ageBands.forEach { band ->
                PardisFilterPill(
                    label = band,
                    selected = state.selectedAgeBand == band,
                    onClick = {
                        onAction(LibraryAction.SetAgeBand(if (state.selectedAgeBand == band) null else band))
                    },
                )
            }
            PardisFilterPill(
                label = if (state.totalCachedLabel.isNotEmpty()) "Offline · ${state.totalCachedLabel}" else "Offline",
                selected = state.showOnlyCached,
                onClick = { onAction(LibraryAction.ToggleShowOnlyCached) },
            )
        }
        Spacer(Modifier.height(PardisSpacing.md))
        PardisSectionHeader(
            title = "Stories",
            subtitle = if (state.selectedAgeBand == null) "All available stories" else "Filtered for ${state.selectedAgeBand}",
            actionLabel = "Refresh",
            onAction = { onAction(LibraryAction.Refresh) },
        )
        Spacer(Modifier.height(PardisSpacing.sm))

        if (state.isLoading && state.stories.isEmpty()) {
            CircularProgressIndicator(color = PardisColors.saffron)
        }

        state.errorMessage?.let { err ->
            Text("Error: $err", color = MaterialTheme.colorScheme.error)
            Button(onClick = { onAction(LibraryAction.Refresh) }) { Text("Retry") }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomContentPadding),
            verticalArrangement = Arrangement.spacedBy(PardisSpacing.sm)
        ) {
            items(state.stories, key = { it.slug }) { story ->
                PardisStoryCard(
                    titleEn = story.titleEn,
                    titleFa = story.titleFa,
                    ageBand = story.ageBand,
                    minutes = story.minutes,
                    vocabCount = story.vocabCount,
                    coverUrl = state.localCoverUrls[story.slug] ?: story.coverUrl,
                    downloadProgress = state.downloadProgress[story.slug],
                    downloadedSizeLabel = state.downloadedSizeLabels[story.slug],
                    isFailed = state.failedDownloads.contains(story.slug),
                    onClick = { onOpenStory(story.slug) },
                    onDownload = { onAction(LibraryAction.DownloadStory(story.slug)) },
                    onCancel = { onAction(LibraryAction.CancelDownload(story.slug)) },
                    onRemove = { onAction(LibraryAction.RemoveDownload(story.slug)) },
                )
            }
        }
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
    val context = LocalContext.current
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
            .pardisScreenBackground()
            .padding(PardisSpacing.md)
    ) {
        val hasOfflineAssets = state.localVideoUrlFa != null || state.localVideoUrlEn != null || state.localIllustrationUrls.isNotEmpty() || state.localNarrationUrls.isNotEmpty()
        PardisReaderHeaderBar(
            onBack = onBack,
            pageLabel = if (state.pages.isNotEmpty()) "${state.currentPage + 1} / ${state.pages.size}" else "Reader",
            isOffline = hasOfflineAssets,
            backLabel = "← Library",
            offlineLabel = "Offline",
        )
        Spacer(Modifier.height(PardisSpacing.sm))

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

                // Stable ExoPlayer instance for the reader session (created once, reused across text<->video toggles and remote->local after cache).
                // Updating via setMediaItem avoids full release/create which triggers noisy MediaCodec/BufferQueue detach/cancel logs.
                val exoPlayer = remember {
                    ExoPlayer.Builder(context).build().apply {
                        addListener(object : Player.Listener {
                            override fun onPlaybackStateChanged(playbackState: Int) {
                                if (playbackState == Player.STATE_ENDED) {
                                    // Video finished - go to last page / end of story
                                    onAction(ReaderAction.GoToPage(state.pages.lastIndex.coerceAtLeast(0)))
                                }
                            }
                        })
                    }
                }

                // React to effective video source (remote or local file path) changes: set on player (create once).
                // Also handles initial entry into video mode, and switch after successful "Cache video + assets".
                LaunchedEffect(videoUrl) {
                    if (videoUrl != null) {
                        exoPlayer.setMediaItem(MediaItem.fromUri(videoUrl))
                        exoPlayer.prepare()
                        exoPlayer.playWhenReady = true
                    } else {
                        exoPlayer.pause()
                    }
                }

                // Pause decode during explicit asset download (frees decoder / bandwidth while Ktor pulls large video + many assets).
                // After success the source-update effect above will re-prepare from local.
                LaunchedEffect(state.isDownloadingVideo, exoPlayer) {
                    if (state.isDownloadingVideo && exoPlayer.isPlaying) {
                        exoPlayer.pause()
                    }
                }

                // Retained per-page narration audio player (MediaPlayer for short clips; release prior on new)
                val narrationPlayer = remember { mutableStateOf<MediaPlayer?>(null) }

                // Drive page changes from video playback position using cues (basic ticker)
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

                // When the page changes, seek the video to that page's cue start — but ONLY for
                // user-initiated jumps (Prev/Next/restore). When the video itself drives the page
                // change (cue ticker above), the playback position is already inside the new cue,
                // so seeking would rewind the video to the cue start and stutter at every boundary.
                LaunchedEffect(state.currentPage, exoPlayer, state.isVideoMode) {
                    if (state.isVideoMode) {
                        val cue = state.cues.firstOrNull { it.pageIndex == state.currentPage }
                        if (cue != null) {
                            val posSec = exoPlayer.currentPosition / 1000.0
                            val alreadyInCue = posSec >= cue.startSec && posSec < cue.endSec
                            if (!alreadyInCue) {
                                exoPlayer.seekTo((cue.startSec * 1000).toLong().coerceAtLeast(0))
                            }
                        }
                    }
                }

                DisposableEffect(exoPlayer) {
                    onDispose {
                        exoPlayer.release()
                    }
                }

                DisposableEffect(narrationPlayer.value) {
                    onDispose {
                        narrationPlayer.value?.release()
                        narrationPlayer.value = null
                    }
                }

                PardisMetaPill(
                    text = "Page ${page.page}",
                    containerColor = PardisColors.backgroundAlt,
                    contentColor = PardisColors.inkMuted,
                )
                Spacer(Modifier.height(PardisSpacing.sm))

                if (videoUrl != null) {
                    // Video mode: player always visible at top (tall, prominent), 
                    // dedicated scrollable captions area below for readable synced text.
                    // Much better UX than cramped scroll + tiny overlay.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp)  // taller for better viewing
                    ) {
                        PardisCard(modifier = Modifier.fillMaxSize()) {
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
                    }

                    Spacer(Modifier.height(PardisSpacing.sm))

                    // Scrollable captions / current page text (large, readable while video plays)
                    PardisPanel(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        PersianReaderParagraph(
                            text = page.paragraphsFa.joinToString("\n\n").replace(Regex("\\s*\\[[^\\]]*]"), ""),
                            style = MaterialTheme.typography.titleMedium,
                            color = PardisColors.ink,
                        )
                        Text(
                            page.paragraphsEn.joinToString("\n\n").replace(Regex("\\s*\\[[^\\]]*]"), ""),
                            style = MaterialTheme.typography.bodyLarge,
                            color = PardisColors.inkSoft,
                        )

                        if (page.vocabulary.isNotEmpty()) {
                            Text("Vocab on this page", style = MaterialTheme.typography.labelMedium, color = PardisColors.inkMuted)
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
                        PardisRemoteImageFrame(
                            imageUrl = illoUrl,
                            contentDescription = "Illustration for page ${page.page} of story",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                        )

                        PardisPanel {
                            PersianReaderParagraph(
                                text = page.paragraphsFa.joinToString("\n\n").replace(Regex("\\s*\\[[^\\]]*]"), ""),
                                style = MaterialTheme.typography.bodyLarge,
                                color = PardisColors.ink,
                            )
                            Text(page.paragraphsEn.joinToString("\n\n").replace(Regex("\\s*\\[[^\\]]*]"), ""), style = MaterialTheme.typography.bodyMedium, color = PardisColors.inkSoft)

                            if (page.vocabulary.isNotEmpty()) {
                                Text("Vocab on this page", style = MaterialTheme.typography.labelMedium, color = PardisColors.inkMuted)
                                Column {
                                    page.vocabulary.take(3).forEach { v ->
                                        PardisVocabChip(vocab = v, onClick = { onAction(ReaderAction.ShowVocab(v)) })
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(PardisSpacing.md))

                // Transport (fixed at bottom) - split for better UX and accessibility
                PardisPanel {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
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
                        if (state.videoUrlFa != null || state.videoUrlEn != null) {
                            Button(onClick = { onAction(ReaderAction.ToggleVideo) }) {
                                Text(if (state.isVideoMode) "Text mode" else "Video mode")
                            }

                            if (state.isVideoMode) {
                                val hasLocal = state.localVideoUrlFa != null || state.localVideoUrlEn != null
                                if (!hasLocal) {
                                    Button(
                                        onClick = { onAction(ReaderAction.DownloadVideo("fa")) },
                                        enabled = !state.isDownloadingVideo
                                    ) {
                                        Text(state.downloadProgress ?: if (state.isDownloadingVideo) "Downloading video + assets..." else "Cache video + assets")
                                    }
                                } else {
                                    PardisMetaPill("Video cached", PardisColors.mintSoft, PardisColors.mintDeep)
                                    Button(
                                        onClick = { onAction(ReaderAction.ClearAssets) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                    ) {
                                        Text("Clear", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }

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
                                val localNar = if (state.preferredNarrationLang == "fa") state.localNarrationUrls[faKey] ?: state.localNarrationUrls[enKey]
                                               else state.localNarrationUrls[enKey] ?: state.localNarrationUrls[faKey]
                                val url = localNar ?: if (state.preferredNarrationLang == "fa") current?.narrationFa?.url ?: current?.narrationEn?.url
                                                      else current?.narrationEn?.url ?: current?.narrationFa?.url
                                url?.let {
                                    val mp = MediaPlayer().apply {
                                        setDataSource(it)
                                        setOnPreparedListener { prepared ->
                                            prepared.start()
                                            // API 23+ is guaranteed by minSdk 24; keep default rate if a device rejects it.
                                            try {
                                                prepared.playbackParams = prepared.playbackParams.setSpeed(state.playbackRate)
                                            } catch (_: Exception) { /* keep default rate */ }
                                        }
                                        setOnCompletionListener { completed ->
                                            completed.release()
                                            if (narrationPlayer.value == completed) narrationPlayer.value = null
                                            // Auto-advance to next page after narration clip ends (only in text mode)
                                            if (!state.isVideoMode && state.currentPage < (state.pages.lastIndex)) {
                                                onAction(ReaderAction.NextPage)
                                            }
                                        }
                                        setOnErrorListener { p, _, _ ->
                                            p.release()
                                            if (narrationPlayer.value == p) narrationPlayer.value = null
                                            true
                                        }
                                        // Async prepare: never block the UI thread on a (possibly remote) narration URL.
                                        prepareAsync()
                                    }
                                    narrationPlayer.value = mp
                                }
                            } catch (_: Exception) {
                                // Silent fail for demo; in real app surface error (e.g. no audio for this page)
                                narrationPlayer.value?.release()
                                narrationPlayer.value = null
                            }
                        }, colors = ButtonDefaults.buttonColors(containerColor = PardisColors.saffron, contentColor = PardisColors.inkOnDark), modifier = Modifier.fillMaxWidth()) {
                            PardisIcon(PardisIconKind.Play, contentDescription = null, tint = PardisColors.inkOnDark)
                            Spacer(Modifier.size(PardisSpacing.xs))
                            Text("Play narration")
                        }
                        PardisControlGroup(label = "Narration language") {
                            PardisFilterPill(label = "FA", selected = state.preferredNarrationLang == "fa", onClick = { onAction(ReaderAction.SetNarrationLang("fa")) })
                            PardisFilterPill(label = "EN", selected = state.preferredNarrationLang == "en", onClick = { onAction(ReaderAction.SetNarrationLang("en")) })
                        }
                        PardisControlGroup(label = "Playback speed") {
                            listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { r ->
                                PardisFilterPill(
                                    label = when (r) { 1.0f -> "1x"; 2.0f -> "2x"; else -> "${r}x" },
                                    selected = state.playbackRate == r,
                                    onClick = { onAction(ReaderAction.SetPlaybackRate(r)) },
                                )
                            }
                        }
                        // Clear cached assets if any
                        val hasLocal = state.localVideoUrlFa != null || state.localVideoUrlEn != null || state.localIllustrationUrls.isNotEmpty() || state.localNarrationUrls.isNotEmpty()
                        if (hasLocal) {
                            Button(onClick = { onAction(ReaderAction.ClearAssets) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                                Text("Clear offline", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                // Basic bottom "sheet" for selected vocab (tappable chips open this)
                state.selectedVocab?.let { v ->
                    Spacer(Modifier.height(PardisSpacing.sm))
                    PardisVocabSheet(
                        vocab = v,
                        modifier = Modifier.fillMaxWidth(),
                        onPlayPronunciation = if (v.audioUrl != null) {
                            {
                                val mp = MediaPlayer()
                                try {
                                    mp.setDataSource(v.audioUrl)
                                    mp.setOnPreparedListener { it.start() }
                                    mp.setOnCompletionListener { it.release() }
                                    mp.setOnErrorListener { p, _, _ -> p.release(); true }
                                    mp.prepareAsync()
                                } catch (_: Exception) {
                                    mp.release()
                                }
                            }
                        } else {
                            null
                        },
                        onClose = { onAction(ReaderAction.DismissVocab) },
                    )
                }
            }
        }
    }
}
