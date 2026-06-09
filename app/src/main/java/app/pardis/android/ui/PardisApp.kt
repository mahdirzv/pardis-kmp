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
