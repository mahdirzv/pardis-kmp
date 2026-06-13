package app.pardis.android.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pardis.design.PardisColors
import app.pardis.design.PardisSpacing
import app.pardis.design.PardisTheme
import app.pardis.design.pardisScreenBackground
import app.pardis.shared.library.LibraryViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.koin.compose.viewmodel.koinViewModel
import app.pardis.core.model.ChildProfile
import app.pardis.core.model.RivanaContent
import app.pardis.core.model.ThemePreference
import app.pardis.shared.profile.ProfileAction
import app.pardis.shared.profile.ProfileViewModel
import app.pardis.shared.theme.ThemeAction
import app.pardis.shared.theme.ThemeViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable

private enum class PardisRootTab(
    val title: String,
    val icon: PardisIconKind,
) {
    Today("Today", PardisIconKind.Home),
    Library("Library", PardisIconKind.Book),
    Bedtime("Bedtime", PardisIconKind.Moon),
    Rewards("Rewards", PardisIconKind.Star),
    You("You", PardisIconKind.User),
}

@Composable
fun PardisApp(
    profileViewModel: ProfileViewModel = koinViewModel(),
    themeViewModel: ThemeViewModel = koinViewModel(),
) {
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val themeState by themeViewModel.uiState.collectAsStateWithLifecycle()

    // Resolve the effective dark flag from the persisted preference (SYSTEM follows the OS).
    val darkTheme = when (themeState.preference) {
        ThemePreference.DARK -> true
        ThemePreference.LIGHT -> false
        else -> isSystemInDarkTheme()
    }
    // on=DARK, off=LIGHT (explicit). The binary toggle never stores SYSTEM; SYSTEM is just the
    // pre-interaction default.
    val onSetDark: (Boolean) -> Unit = {
        themeViewModel.onAction(ThemeAction.SetPreference(if (it) ThemePreference.DARK else ThemePreference.LIGHT))
    }

    // Tint the system status/navigation bars to the themed background + flip their icon appearance,
    // so the system chrome follows dark/light instead of staying light. (Direct window colours keep
    // the current non-edge-to-edge layout; the screens don't apply status-bar insets.)
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        val barColor = (if (darkTheme) PardisColors.darkBackground else PardisColors.background).toArgb()
        SideEffect {
            @Suppress("DEPRECATION") run { window.statusBarColor = barColor; window.navigationBarColor = barColor }
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    PardisTheme(darkTheme = darkTheme) {
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
                        isDark = darkTheme,
                        onSetDark = onSetDark,
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
    isDark: Boolean,
    onSetDark: (Boolean) -> Unit,
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
                isDark = isDark,
                onSetDark = onSetDark,
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
            val lullaby = RivanaContent.lullabies.getOrElse(index) { RivanaContent.lullabies.first() }
            LullabyPlayerScreen(lullaby = lullaby, onBack = { navController.popBackStack() })
        }
        composable(
            "character/{index}",
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("index") ?: 0
            val character = RivanaContent.characters.getOrElse(index) { RivanaContent.characters.first() }
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
    isDark: Boolean,
    onSetDark: (Boolean) -> Unit,
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
                    isDark = isDark,
                    onSetDark = onSetDark,
                )
            }
        }
    }
}

