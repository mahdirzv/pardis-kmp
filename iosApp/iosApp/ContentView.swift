import SwiftUI
import Shared

private struct LullabyRoute: Hashable {
    let index: Int
}

private struct CharacterRoute: Hashable {
    let index: Int
}

private enum PardisRootTab: CaseIterable, Hashable {
    case today
    case library
    case bedtime
    case rewards
    case you

    var title: String {
        switch self {
        case .today: return "Today"
        case .library: return "Library"
        case .bedtime: return "Bedtime"
        case .rewards: return "Rewards"
        case .you: return "You"
        }
    }

    var icon: PardisIconKind {
        switch self {
        case .today: return .home
        case .library: return .book
        case .bedtime: return .moon
        case .rewards: return .star
        case .you: return .user
        }
    }
}

struct ContentView: View {
    // App-lifetime theme preference; drives the whole app's light/dark appearance.
    @State private var themeModel = ThemeSharedViewModel()

    var body: some View {
        RootShellView(onSetDark: { themeModel.setPreference($0 ? .dark : .light) })
            // Invisible probe carrying the font-load status so the UI test can read + log it.
            .overlay(alignment: .topLeading) {
                Text(PardisFontRegistry.summary)
                    .opacity(0.0001)
                    .allowsHitTesting(false)
                    .accessibilityIdentifier("pardis-fonts")
            }
            // Apply the persisted preference (nil = follow system). The dynamic PardisColors tokens
            // resolve against whatever scheme this sets.
            .preferredColorScheme(themeModel.colorScheme)
            .task { await themeModel.activate() }
    }
}

/// Profile gate, mirroring Android `PardisApp`: blocks the main shell until a profile is
/// selected, shows the onboarding picker on first launch, and presents the switch-profile
/// picker (isSwitch) as a cover. `ProfileSharedViewModel` is app-lifetime here.
private struct RootShellView: View {
    let onSetDark: (Bool) -> Void
    @State private var profileModel = ProfileSharedViewModel()
    @State private var showSwitchProfile = false

    var body: some View {
        Group {
            if profileModel.isLoading {
                PardisColors.background.ignoresSafeArea()
            } else if profileModel.selectedProfile == nil {
                OnboardingView(
                    profiles: profileModel.profiles,
                    onSelect: { profileModel.select($0.id) }
                )
            } else {
                MainShellView(
                    activeProfile: profileModel.selectedProfile!,
                    onSwitchProfile: { showSwitchProfile = true },
                    onSetDark: onSetDark
                )
                .fullScreenCover(isPresented: $showSwitchProfile) {
                    OnboardingView(
                        profiles: profileModel.profiles,
                        isSwitch: true,
                        onSelect: {
                            profileModel.select($0.id)
                            showSwitchProfile = false
                        },
                        onBack: { showSwitchProfile = false }
                    )
                }
            }
        }
        .task { await profileModel.activate() }
    }
}

private struct MainShellView: View {
    let activeProfile: ChildProfile
    let onSwitchProfile: () -> Void
    let onSetDark: (Bool) -> Void

    @State private var selectedTab: PardisRootTab = .library
    @State private var libraryModel = LibrarySharedViewModel()
    // Per-tab navigation routes — each content tab owns its own stack (recommended SwiftUI pattern),
    // so opening a story pushes within that tab rather than over the whole shell.
    // Opening a story goes Detail → Reader → Finish in both Today and Library, mirroring
    // Android's nav graph (single onOpenStory → "detail/{slug}").
    @State private var todayPath = NavigationPath()
    @State private var libraryPath = NavigationPath()
    @State private var lullabyRoute: LullabyRoute? = nil
    @State private var characterRoute: CharacterRoute? = nil

    var body: some View {
        // Native SwiftUI TabView — gets Liquid Glass automatically on iOS 26 and platform-correct
        // behavior elsewhere. Brand accent via .tint; tab items use the same SF Symbols as PardisIcon.
        TabView(selection: $selectedTab) {
            NavigationStack(path: $todayPath) {
                TodayView(
                    model: libraryModel,
                    activeName: activeProfile.name,
                    onOpenStory: { todayPath.append(DetailRoute(slug: $0)) },
                    onOpenLibrary: { selectedTab = .library },
                    onOpenBedtime: { selectedTab = .bedtime }
                )
                .pardisScreenBackground()
                .storyFlowDestinations(path: $todayPath)
            }
            .tabItem { Label(PardisRootTab.today.title, systemImage: PardisRootTab.today.icon.systemName) }
            .tag(PardisRootTab.today)

            NavigationStack(path: $libraryPath) {
                LibraryView(
                    model: libraryModel,
                    onOpenStory: { libraryPath.append(DetailRoute(slug: $0)) }
                )
                .pardisScreenBackground()
                .storyFlowDestinations(path: $libraryPath)
            }
            .tabItem { Label(PardisRootTab.library.title, systemImage: PardisRootTab.library.icon.systemName) }
            .tag(PardisRootTab.library)

            NavigationStack {
                BedtimeView(onOpenLullaby: { index in lullabyRoute = LullabyRoute(index: index) })
                    .navigationDestination(item: $lullabyRoute) { route in
                        LullabyView(
                            lullaby: RivanaContent.shared.lullabies[route.index],
                            onBack: { lullabyRoute = nil }
                        )
                    }
            }
            .tabItem { Label(PardisRootTab.bedtime.title, systemImage: PardisRootTab.bedtime.icon.systemName) }
            .tag(PardisRootTab.bedtime)

            NavigationStack {
                RewardsView(
                    storyCount: libraryModel.stories.count,
                    onOpenCharacter: { index in characterRoute = CharacterRoute(index: index) }
                )
                .navigationDestination(item: $characterRoute) { route in
                    CharacterView(
                        character: RivanaContent.shared.characters[route.index],
                        onBack: { characterRoute = nil }
                    )
                }
            }
            .tabItem { Label(PardisRootTab.rewards.title, systemImage: PardisRootTab.rewards.icon.systemName) }
            .tag(PardisRootTab.rewards)

            NavigationStack {
                YouView(
                    activeProfile: activeProfile,
                    downloadCount: libraryModel.cachedStorySlugs.count,
                    onSwitchProfile: onSwitchProfile,
                    onSetDark: onSetDark
                )
                .pardisScreenBackground()
            }
            .tabItem { Label(PardisRootTab.you.title, systemImage: PardisRootTab.you.icon.systemName) }
            .tag(PardisRootTab.you)
        }
        .tint(PardisColors.saffronDeep)
        .task {
            await libraryModel.activate()
        }
    }
}

// Story-flow routes (Hashable values pushed onto a tab's NavigationPath).
private struct DetailRoute: Hashable { let slug: String }
private struct ReadRoute: Hashable { let slug: String }
private struct FinishRoute: Hashable { let slug: String }

/// Detail → Reader → Finish destinations, mirroring Android's nav graph:
/// - finish REPLACES the reader in the stack (back from finish doesn't re-enter the last page)
/// - "Next story" replaces finish with the next story's detail
/// - "Done" pops to the tab root
private extension View {
    func storyFlowDestinations(path: Binding<NavigationPath>) -> some View {
        self
            .navigationDestination(for: DetailRoute.self) { route in
                DetailScreen(
                    slug: route.slug,
                    onRead: { path.wrappedValue.append(ReadRoute(slug: $0)) },
                    onBack: { path.wrappedValue.removeLast() }
                )
            }
            .navigationDestination(for: ReadRoute.self) { route in
                ReaderScreen(
                    slug: route.slug,
                    onFinish: { finishedSlug in
                        path.wrappedValue.removeLast() // pop the reader…
                        path.wrappedValue.append(FinishRoute(slug: finishedSlug)) // …and replace with finish
                    }
                )
            }
            .navigationDestination(for: FinishRoute.self) { route in
                FinishScreen(
                    slug: route.slug,
                    onNextStory: { nextSlug in
                        path.wrappedValue.removeLast()
                        path.wrappedValue.append(DetailRoute(slug: nextSlug))
                    },
                    onDone: { path.wrappedValue = NavigationPath() }
                )
            }
    }
}

extension Collection {
    // Use Index (not Int) so this compiles for any Collection; Array's Index is Int, so call sites
    // like pages[safe: 3] still work.
    subscript(safe index: Index) -> Element? {
        indices.contains(index) ? self[index] : nil
    }
}
