package app.pardis.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.pardis.design.PardisRadius
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pardis.design.PardisColors
import app.pardis.design.PardisGradients
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
import app.pardis.core.model.ChildProfile
import app.pardis.shared.profile.ProfileAction
import app.pardis.shared.profile.ProfileViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable

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
fun PardisApp(
    profileViewModel: ProfileViewModel = koinViewModel(),
) {
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()

    PardisTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                profileState.isLoading -> {
                    Box(Modifier.fillMaxSize().background(PardisColors.background))
                }
                profileState.selectedProfile == null -> {
                    PardisOnboardingScreen(
                        profiles = profileState.profiles,
                        isSwitch = false,
                        onSelect = { profileViewModel.onAction(ProfileAction.Select(it.id)) },
                        onBack = {},
                        onComingSoon = {},
                    )
                }
                else -> {
                    PardisAppShell(
                        activeProfile = profileState.selectedProfile!!,
                        profiles = profileState.profiles,
                        onSelectProfile = { profileViewModel.onAction(ProfileAction.Select(it.id)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PardisAppShell(
    activeProfile: ChildProfile,
    profiles: List<ChildProfile>,
    onSelectProfile: (ChildProfile) -> Unit,
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "library") {
        composable("library") {
            RootShellRoute(
                activeProfile = activeProfile,
                onOpenStory = { slug -> navController.navigate("detail/$slug") },
                onOpenLullaby = { index -> navController.navigate("lullaby/$index") },
                onOpenCharacter = { index -> navController.navigate("character/$index") },
                onSwitchProfile = { navController.navigate("onboarding") },
            )
        }
        composable("onboarding") {
            PardisOnboardingScreen(
                profiles = profiles,
                isSwitch = true,
                onSelect = {
                    onSelectProfile(it)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() },
                onComingSoon = {},
            )
        }
        composable(
            "detail/{slug}",
            arguments = listOf(navArgument("slug") { type = NavType.StringType })
        ) { backStackEntry ->
            val slug = backStackEntry.arguments?.getString("slug") ?: ""
            StoryDetailRoute(
                slug = slug,
                onRead = { navController.navigate("reader/$it") },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            "reader/{slug}",
            arguments = listOf(navArgument("slug") { type = NavType.StringType })
        ) { backStackEntry ->
            val slug = backStackEntry.arguments?.getString("slug") ?: ""
            ReaderRoute(
                slug = slug,
                onBack = { navController.popBackStack() },
                onFinish = { finishedSlug ->
                    // Replace the reader with the celebration so back doesn't re-enter the last page.
                    navController.navigate("finish/$finishedSlug") {
                        popUpTo("reader/$slug") { inclusive = true }
                    }
                },
            )
        }
        composable(
            "finish/{slug}",
            arguments = listOf(navArgument("slug") { type = NavType.StringType })
        ) { backStackEntry ->
            val slug = backStackEntry.arguments?.getString("slug") ?: ""
            StoryFinishRoute(
                slug = slug,
                onNextStory = { nextSlug ->
                    navController.navigate("detail/$nextSlug") {
                        popUpTo("finish/$slug") { inclusive = true }
                    }
                },
                onDone = { navController.popBackStack("library", inclusive = false) },
            )
        }
        composable(
            "lullaby/{index}",
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("index") ?: 0
            val lullaby = rivanaLullabies.getOrElse(index) { rivanaLullabies.first() }
            LullabyPlayerScreen(lullaby = lullaby, onBack = { navController.popBackStack() })
        }
        composable(
            "character/{index}",
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("index") ?: 0
            val character = rivanaCharacters.getOrElse(index) { rivanaCharacters.first() }
            CharacterScreen(character = character, onBack = { navController.popBackStack() })
        }
    }
}

@Composable
private fun RootShellRoute(
    activeProfile: ChildProfile,
    onOpenStory: (String) -> Unit,
    onOpenLullaby: (Int) -> Unit,
    onOpenCharacter: (Int) -> Unit,
    onSwitchProfile: () -> Unit,
    viewModel: LibraryViewModel = koinViewModel(),
) {
    // rememberSaveable so the selected tab survives the switch-profile route round-trip
    // (navigate to "onboarding" and back) instead of resetting to Library.
    var selectedTab by rememberSaveable { mutableStateOf(PardisRootTab.Library) }
    val tabs = remember { PardisRootTab.entries.toList() }
    val libraryState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            PardisBottomTabBar(
                items = tabs.map { PardisTabItem(label = it.title, icon = it.icon) },
                selectedIndex = tabs.indexOf(selectedTab),
                onSelect = { selectedTab = tabs[it] },
            )
        },
    ) { innerPadding ->
        // Reserve space for the native bar (its height + system nav inset) so scroll content
        // clears it, with a little breathing room.
        val bottomReserve = innerPadding.calculateBottomPadding() + PardisSpacing.sm
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pardisScreenBackground(),
        ) {
            when (selectedTab) {
                PardisRootTab.Today -> TodayScreen(
                    activeName = activeProfile.name,
                    state = libraryState,
                    onAction = viewModel::onAction,
                    onOpenStory = onOpenStory,
                    onOpenLibrary = { selectedTab = PardisRootTab.Library },
                    onOpenBedtime = { onOpenLullaby(1) },
                    bottomContentPadding = bottomReserve,
                )
                PardisRootTab.Library -> LibraryScreen(
                    state = libraryState,
                    onAction = viewModel::onAction,
                    onOpenStory = onOpenStory,
                    bottomContentPadding = bottomReserve,
                )
                PardisRootTab.Bedtime -> BedtimeScreen(
                    onOpenLullaby = onOpenLullaby,
                    bottomContentPadding = bottomReserve,
                )
                PardisRootTab.Rewards -> RewardsScreen(
                    storyCount = libraryState.stories.size,
                    onOpenCharacter = onOpenCharacter,
                    bottomContentPadding = bottomReserve,
                )
                PardisRootTab.You -> YouScreen(
                    activeProfile = activeProfile,
                    onSwitchProfile = onSwitchProfile,
                    downloadCount = libraryState.cachedStorySlugs.size,
                    bottomContentPadding = bottomReserve,
                )
                else -> PardisPlaceholderTabScreen(
                    title = selectedTab.title,
                    subtitle = selectedTab.subtitle,
                    icon = selectedTab.icon,
                )
            }
        }
    }
}

@Composable
private fun TodayScreen(
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
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("$greeting, $activeName".uppercase(), style = MaterialTheme.typography.labelSmall, color = PardisColors.saffronDeep)
            Text("Salâm", style = MaterialTheme.typography.displayLarge, color = PardisColors.ink, fontWeight = FontWeight.ExtraBold)
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
        PersianReaderInline(titleFa, style = MaterialTheme.typography.bodySmall, color = PardisColors.indigo, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun WordOfDayCard(modifier: Modifier = Modifier) {
    Box(
        modifier.fillMaxWidth().clip(RoundedCornerShape(PardisRadius.lg)).background(PardisColors.lilacSoft)
            .border(1.dp, PardisColors.lilac.copy(alpha = 0.22f), RoundedCornerShape(PardisRadius.lg)),
    ) {
    PardisPatternOverlay(PardisMotif.Vine, PardisColors.lilacDeep, alpha = 0.10f, fade = PardisPatternFade.TopRight, modifier = Modifier.matchParentSize())
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 22.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("WORD OF THE DAY", style = MaterialTheme.typography.labelSmall, color = PardisColors.lilacDeep)
            PardisIcon(PardisIconKind.Volume, contentDescription = null, tint = PardisColors.lilacDeep)
        }
        Spacer(Modifier.height(PardisSpacing.xs))
        PersianReaderParagraph("دلیر", style = MaterialTheme.typography.displayLarge, color = PardisColors.lilacDeep)
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

internal data class Lullaby(
    val title: String,
    val titleFa: String,
    val minutes: Int,
    val origin: String,
    val sceneVariant: Int,
    val plays: String,
)

internal val rivanaLullabies = listOf(
    Lullaby("Moon Over Damavand", "ماه بر فرازِ دماوند", 18, "Traditional · Mazandaran", 6, "2.1k"),
    Lullaby("Laay Laay, Little Star", "لای‌لای، ستاره‌ی کوچک", 12, "Folk lullaby", 0, "4.8k"),
    Lullaby("The Sleepy River", "رودِ خواب‌آلود", 22, "Original · Rivana", 6, "1.3k"),
    Lullaby("Garden of Dreams", "باغِ رؤیاها", 15, "Traditional · Shiraz", 0, "3.6k"),
)

/**
 * Static cast of Shahnameh figures for the Character screen. The app has no character
 * model yet, so this content is hardcoded from the design's roster (data.js).
 */
internal data class RivanaCharacter(
    val name: String,
    val nameFa: String,
    val role: String,
    val bio: String,
    val sceneVariant: Int,
    val tone: String,
    val collected: Boolean,
    val appearances: Int,
)

internal val rivanaCharacters = listOf(
    RivanaCharacter("Rostam", "رستم", "Champion of Persia", "The mightiest of all Persian heroes — strong as a mountain, loyal to his king, and rider of the great horse Rakhsh.", 1, "saffron", true, 3),
    RivanaCharacter("Simurgh", "سیمرغ", "The Wise Bird", "A vast, kind bird whose feathers glow like dawn. She heals the wounded and raises lost children on Mount Damavand.", 3, "lilac", true, 2),
    RivanaCharacter("Anahita", "آناهیتا", "Keeper of Waters", "The radiant guardian of rivers, rain, and wisdom — bringer of life to every field in Persia.", 0, "lapis", true, 1),
    RivanaCharacter("Kaveh", "کاوه", "The Blacksmith", "An ordinary blacksmith whose courage sparked a revolt and whose apron became the banner of a free Persia.", 5, "rose", true, 1),
    RivanaCharacter("Zal", "زال", "Child of the Simurgh", "Born with snow-white hair and raised by the Simurgh, Zal grows into a wise and gentle prince.", 3, "sun", false, 1),
    RivanaCharacter("Sindbad", "سندباد", "The Sea Traveller", "A curious voyager who sails toward every horizon and returns with tales of wonder.", 4, "mint", true, 1),
)

@Composable
private fun BedtimeScreen(onOpenLullaby: (Int) -> Unit, bottomContentPadding: androidx.compose.ui.unit.Dp) {
    val gutter = PardisSpacing.lg
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PardisColors.indigoDeep, Color(0xFF0F1330), Color(0xFF0A0E22)))),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = PardisSpacing.xl, bottom = bottomContentPadding),
            verticalArrangement = Arrangement.spacedBy(PardisSpacing.md),
        ) {
            item {
                Column(Modifier.padding(horizontal = gutter)) {
                    Text("SWEET DREAMS", style = MaterialTheme.typography.labelSmall, color = Color(0xBFFFE9D2))
                    Text("Bedtime", style = MaterialTheme.typography.displayLarge, color = PardisColors.inkOnDark, fontWeight = FontWeight.ExtraBold)
                    PersianReaderInline("وقتِ خواب · لای‌لای", style = MaterialTheme.typography.bodyMedium, color = PardisColors.inkOnDarkFaint)
                }
            }
            item { BedtimeFeatured(rivanaLullabies[0], onClick = { onOpenLullaby(0) }, Modifier.padding(horizontal = gutter)) }
            item { WindDownCard(Modifier.padding(horizontal = gutter)) }
            item {
                Text(
                    "Lullaby shelf",
                    style = MaterialTheme.typography.titleLarge,
                    color = PardisColors.inkOnDark,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = gutter, vertical = PardisSpacing.xs),
                )
            }
            itemsIndexed(rivanaLullabies.drop(1)) { i, l -> LullabyRow(l, onClick = { onOpenLullaby(i + 1) }, Modifier.padding(horizontal = gutter)) }
            item {
                Text(
                    "شب بخیر · خواب‌های خوش",
                    style = MaterialTheme.typography.bodySmall,
                    color = PardisColors.inkOnDarkFaint,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = PardisSpacing.md),
                )
            }
        }
    }
}

@Composable
private fun BedtimeFeatured(l: Lullaby, onClick: () -> Unit, modifier: Modifier) {
    Box(modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(PardisRadius.xl)).clickable(onClick = onClick)) {
        PardisSceneArt(seed = l.title, forcedVariant = l.sceneVariant, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xB30F0C1E)))))
        Column(Modifier.align(Alignment.BottomStart).padding(18.dp)) {
            Row(
                Modifier.clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.surfaceOnDark).padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                PardisIcon(PardisIconKind.Moon, contentDescription = null, tint = PardisColors.inkOnDark, size = 14.dp)
                Text("Lullaby of the night", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkOnDark)
            }
            Spacer(Modifier.height(9.dp))
            Text(l.title, style = MaterialTheme.typography.headlineSmall, color = PardisColors.inkOnDark, fontWeight = FontWeight.Bold)
            PersianReaderInline(l.titleFa, style = MaterialTheme.typography.titleMedium, color = PardisColors.inkOnDarkMuted)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier.size(46.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.inkOnDark),
                    contentAlignment = Alignment.Center,
                ) {
                    PardisIcon(PardisIconKind.Play, contentDescription = "Play ${l.title}", tint = PardisColors.indigoDeep)
                }
                Text("${l.minutes} min · ${l.origin}", style = MaterialTheme.typography.bodySmall, color = PardisColors.inkOnDarkSoft)
            }
        }
    }
}

@Composable
private fun WindDownCard(modifier: Modifier) {
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(PardisRadius.lg)).background(PardisColors.surfaceOnDarkFaint).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box(
            Modifier.size(46.dp).clip(RoundedCornerShape(PardisRadius.base)).background(Color(0x2EF4B53A)),
            contentAlignment = Alignment.Center,
        ) {
            PardisIcon(PardisIconKind.Clock, contentDescription = null, tint = PardisColors.sun)
        }
        Column(Modifier.weight(1f)) {
            Text("Wind-down routine", style = MaterialTheme.typography.titleMedium, color = PardisColors.inkOnDark, fontWeight = FontWeight.Bold)
            Text("One short story, then a lullaby. ~25 min.", style = MaterialTheme.typography.bodySmall, color = PardisColors.inkOnDarkFaint)
        }
        Box(
            Modifier.clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.surfaceOnDarkSoft).padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text("Start", style = MaterialTheme.typography.labelLarge, color = PardisColors.inkOnDark)
        }
    }
}

@Composable
private fun LullabyRow(l: Lullaby, onClick: () -> Unit, modifier: Modifier) {
    Row(
        modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = PardisSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        PardisSceneArt(seed = l.title, forcedVariant = l.sceneVariant, modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)))
        Column(Modifier.weight(1f)) {
            Text(l.title, style = MaterialTheme.typography.titleMedium, color = PardisColors.inkOnDark, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            PersianReaderInline(l.titleFa, style = MaterialTheme.typography.bodySmall, color = PardisColors.inkOnDarkFaint, maxLines = 1)
            Text("${l.minutes} min · ${l.plays} plays", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkOnDarkFaint)
        }
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.surfaceOnDarkSoft),
            contentAlignment = Alignment.Center,
        ) {
            PardisIcon(PardisIconKind.Play, contentDescription = "Play ${l.title}", tint = PardisColors.inkOnDark)
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
private fun RewardsScreen(storyCount: Int, onOpenCharacter: (Int) -> Unit, bottomContentPadding: androidx.compose.ui.unit.Dp) {
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
                rivanaBadges.filter { !it.earned }.maxByOrNull { it.progress }?.let { NextBadgeCard(it) }
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
                    rivanaCharacters.forEachIndexed { i, c ->
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
                PardisIcon(PardisIconKind.Sprout, contentDescription = null, tint = PardisColors.inkOnDark)
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
                PardisIcon(b.icon, contentDescription = b.label, tint = deep, size = 32.dp)
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

private data class SettingsItem(val icon: PardisIconKind, val tone: String, val label: String, val detail: String? = null)

@Composable
private fun YouScreen(activeProfile: ChildProfile, onSwitchProfile: () -> Unit, downloadCount: Int, bottomContentPadding: androidx.compose.ui.unit.Dp) {
    val gutter = PardisSpacing.lg
    Box(Modifier.fillMaxSize().background(PardisColors.background)) {
    PardisPatternOverlay(
        motif = PardisMotif.Rosette,
        color = PardisColors.indigo,
        alpha = 0.05f,
        fade = PardisPatternFade.Top,
        modifier = Modifier.fillMaxWidth().height(220.dp).align(Alignment.TopCenter),
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = PardisSpacing.xl, bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(PardisSpacing.md),
    ) {
        item {
            Text("You", style = MaterialTheme.typography.displayLarge, color = PardisColors.ink, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = gutter))
        }
        item { YouProfileCard(activeProfile = activeProfile, onSwitchProfile = onSwitchProfile, modifier = Modifier.padding(horizontal = gutter)) }
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
}

@Composable
private fun YouProfileCard(activeProfile: ChildProfile, onSwitchProfile: () -> Unit, modifier: Modifier) {
    Box(
        modifier.fillMaxWidth().clip(RoundedCornerShape(PardisRadius.xl))
            .background(PardisGradients.dawn)
            .border(1.dp, PardisColors.border, RoundedCornerShape(PardisRadius.xl)),
    ) {
    PardisPatternOverlay(PardisMotif.Paisley, PardisColors.indigo, alpha = 0.07f, fade = PardisPatternFade.TopRight, modifier = Modifier.matchParentSize())
    Row(
        Modifier.fillMaxWidth().padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            Modifier.size(72.dp).clip(RoundedCornerShape(PardisRadius.full))
                .background(toneGradient(activeProfile.tone)),
            contentAlignment = Alignment.Center,
        ) {
            Text(activeProfile.name.take(1), style = MaterialTheme.typography.displayLarge, color = PardisColors.inkOnDark, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f)) {
            Text(activeProfile.name, style = MaterialTheme.typography.headlineSmall, color = PardisColors.ink, fontWeight = FontWeight.ExtraBold)
            Text("Age ${activeProfile.age} · ${activeProfile.streak}-night streak", style = MaterialTheme.typography.bodySmall, color = PardisColors.inkSoft)
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.surface).clickable(onClick = onSwitchProfile).padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                PardisIcon(PardisIconKind.User, contentDescription = null, tint = PardisColors.ink, size = 15.dp)
                Text("Switch reader", style = MaterialTheme.typography.labelLarge, color = PardisColors.ink)
            }
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
                PardisIcon(PardisIconKind.Moon, contentDescription = null, tint = PardisColors.lilacDeep, size = 18.dp)
            }
            Text("Dark mode", style = MaterialTheme.typography.bodyLarge, color = PardisColors.ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            // Decorative off toggle (dark mode not wired yet)
            Box(
                Modifier.size(width = 46.dp, height = 28.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.border),
                contentAlignment = Alignment.CenterStart,
            ) {
                Box(Modifier.padding(start = 3.dp).size(22.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.inkOnDark))
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
            PardisIcon(item.icon, contentDescription = null, tint = deep, size = 18.dp)
        }
        Text(item.label, style = MaterialTheme.typography.bodyLarge, color = PardisColors.ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (item.detail != null) {
            Text(item.detail, style = MaterialTheme.typography.bodySmall, color = PardisColors.inkMuted)
        }
        PardisIcon(PardisIconKind.ChevRight, contentDescription = null, tint = PardisColors.inkFaint, size = 17.dp)
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

@Composable
fun ReaderRoute(
    slug: String,
    onBack: () -> Unit,
    onFinish: (String) -> Unit,
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
        onFinish = onFinish,
    )
}
