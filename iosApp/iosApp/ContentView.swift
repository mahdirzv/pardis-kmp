import SwiftUI
import Shared
import AVKit
import AVFoundation

private struct ReaderRoute: Hashable {
    let slug: String
}

private struct LullabyRoute: Hashable {
    let index: Int
}

private struct CharacterRoute: Hashable {
    let index: Int
}

private struct SelectedVocab: Identifiable {
    let vocab: VocabItem
    var id: String { "\(vocab.fa)-\(vocab.translit)-\(vocab.en)" }
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
    var body: some View {
        RootShellView()
            // Invisible probe carrying the font-load status so the UI test can read + log it.
            .overlay(alignment: .topLeading) {
                Text(PardisFontRegistry.summary)
                    .opacity(0.0001)
                    .allowsHitTesting(false)
                    .accessibilityIdentifier("pardis-fonts")
            }
    }
}

/// Profile gate, mirroring Android `PardisApp`: blocks the main shell until a profile is
/// selected, shows the onboarding picker on first launch, and presents the switch-profile
/// picker (isSwitch) as a cover. `ProfileSharedViewModel` is app-lifetime here.
private struct RootShellView: View {
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
                    onSwitchProfile: { showSwitchProfile = true }
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

    @State private var selectedTab: PardisRootTab = .library
    @State private var libraryModel = LibrarySharedViewModel()
    // Per-tab navigation routes — each content tab owns its own stack (recommended SwiftUI pattern),
    // so opening a story pushes within that tab rather than over the whole shell.
    @State private var todayRoute: ReaderRoute? = nil
    @State private var libraryRoute: ReaderRoute? = nil
    @State private var lullabyRoute: LullabyRoute? = nil
    @State private var characterRoute: CharacterRoute? = nil

    var body: some View {
        // Native SwiftUI TabView — gets Liquid Glass automatically on iOS 26 and platform-correct
        // behavior elsewhere. Brand accent via .tint; tab items use the same SF Symbols as PardisIcon.
        // Bedtime/Rewards remain placeholders until those features land; You is wired to the profile card.
        TabView(selection: $selectedTab) {
            NavigationStack {
                TodayView(
                    model: libraryModel,
                    activeName: activeProfile.name,
                    onOpenStory: { todayRoute = ReaderRoute(slug: $0) },
                    onOpenLibrary: { selectedTab = .library },
                    onOpenBedtime: { selectedTab = .bedtime }
                )
                .pardisScreenBackground()
                .navigationDestination(item: $todayRoute) { route in
                    ReaderScreen(slug: route.slug)
                }
            }
            .tabItem { Label(PardisRootTab.today.title, systemImage: PardisRootTab.today.icon.systemName) }
            .tag(PardisRootTab.today)

            NavigationStack {
                LibraryView(
                    model: libraryModel,
                    onOpenStory: { libraryRoute = ReaderRoute(slug: $0) }
                )
                .pardisScreenBackground()
                .navigationDestination(item: $libraryRoute) { route in
                    ReaderScreen(slug: route.slug)
                }
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
                    onSwitchProfile: onSwitchProfile
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

struct ReaderScreen: View {
    let slug: String
    @State private var model = ReaderSharedViewModel()
    @State private var selectedVocab: SelectedVocab? = nil
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            let hasOffline = model.localVideoUrlFa != nil || model.localVideoUrlEn != nil || !model.localIllustrationUrls.isEmpty || !model.localNarrationUrls.isEmpty
            PardisReaderHeaderBar(
                onBack: { dismiss() },
                pageLabel: !model.pages.isEmpty ? "\(model.currentPage + 1) / \(model.pages.count)" : "Reader",
                isOffline: hasOffline,
                backLabel: "← Library",
                offlineLabel: "Offline"
            )

            if model.isLoading && model.pages.isEmpty {
                ProgressView().tint(PardisColors.saffron)
            } else if let err = model.errorMessage {
                Text("Error: \(err)").foregroundStyle(.red)
            } else if let page = model.pages[safe: model.currentPage] {
                PardisMetaPill(text: "Page \(page.page)", background: PardisColors.backgroundAlt, foreground: PardisColors.inkMuted)

                if model.isVideoMode {
                    // Prefer local cached video file for offline playback (set by OfflineAssetCache + VM after DownloadVideo action).
                    // Falls back to remote Supabase MP4. This makes the fixed tall player + captions work fully offline.
                    let effectiveVideoUrl = model.localVideoUrlFa ?? model.localVideoUrlEn ?? model.videoUrlFa ?? model.videoUrlEn
                    if let videoUrl = effectiveVideoUrl {
                        // Video mode UX: tall player always visible at top, 
                        // separate scrollable area below for large readable synced captions/text.
                        VideoPlayerView(
                            videoUrl: videoUrl,
                            cues: model.cues,
                            currentPage: model.currentPage,
                            onPageChange: { newPage in
                                model.goToPage(Int32(newPage))
                            }
                        )
                        .frame(height: 380)
                        .pardisCardSurface(cornerRadius: PardisRadius.lg)
                    }

                    Spacer(minLength: 8)

                    PardisPanel {
                        ScrollView {
                            VStack(alignment: .leading, spacing: 12) {
                                PersianReaderText(
                                    text: page.paragraphsFa.joined(separator: "\n\n"),
                                    font: .system(size: PardisTypography.xl, weight: .bold, design: .rounded),
                                    color: PardisColors.ink
                                )
                                Text(page.paragraphsEn.joined(separator: "\n\n"))
                                    .font(.system(size: PardisTypography.base, weight: .regular, design: .rounded))
                                    .foregroundStyle(PardisColors.inkSoft)

                                if !page.vocabulary.isEmpty {
                                    Text("Vocab on this page")
                                        .font(.system(size: PardisTypography.sm, weight: .bold, design: .rounded))
                                        .foregroundStyle(PardisColors.inkMuted)
                                    ForEach(page.vocabulary.prefix(3), id: \.fa) { v in
                                        PardisVocabChipView(vocab: v) {
                                            model.showVocab(v)
                                            selectedVocab = SelectedVocab(vocab: v)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    ScrollView {
                        VStack(alignment: .leading, spacing: 12) {
                            let illoUrlStr = model.localIllustrationUrls[Int(page.page)] ?? page.illustrationUrl
                            PardisAsyncImageFrame(
                                url: illoUrlStr.flatMap(URL.init(string:)),
                                accessibilityLabel: "Illustration for page \(page.page)",
                                width: nil,
                                height: 220
                            )

                            PardisPanel {
                                PersianReaderText(
                                    text: page.paragraphsFa.joined(separator: "\n\n"),
                                    font: .system(size: PardisTypography.base, weight: .medium, design: .rounded),
                                    color: PardisColors.ink
                                )

                                Text(page.paragraphsEn.joined(separator: "\n\n"))
                                    .font(.system(size: PardisTypography.sm, weight: .regular, design: .rounded))
                                    .foregroundStyle(PardisColors.inkSoft)

                                if !page.vocabulary.isEmpty {
                                    Text("Vocab on this page")
                                        .font(.system(size: PardisTypography.sm, weight: .bold, design: .rounded))
                                        .foregroundStyle(PardisColors.inkMuted)
                                    ForEach(page.vocabulary.prefix(3), id: \.fa) { v in
                                        PardisVocabChipView(vocab: v) {
                                            model.showVocab(v)
                                            selectedVocab = SelectedVocab(vocab: v)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Text("Loading story \(slug)...")
            }

            PardisPanel {
                HStack {
                    Button("Prev") { model.prevPage() }.disabled(model.currentPage == 0)
                    Button("Next") { model.nextPage() }
                        .buttonStyle(.borderedProminent)
                        .tint(PardisColors.saffron)
                    Spacer()
                    if model.videoUrlFa != nil || model.videoUrlEn != nil {
                        Button(model.isVideoMode ? "Text" : "Video") { model.toggleVideo() }

                        if model.isVideoMode {
                            let hasLocal = model.localVideoUrlFa != nil || model.localVideoUrlEn != nil
                            if !hasLocal {
                                Button {
                                    model.downloadVideo(lang: "fa")
                                } label: {
                                    Text(model.downloadProgress ?? (model.isDownloadingVideo ? "Downloading video + assets..." : "Cache video + assets"))
                                }
                                .disabled(model.isDownloadingVideo)
                            } else {
                                PardisMetaPill(text: "Video cached", background: PardisColors.mintSoft, foreground: PardisColors.mintDeep)
                                Button("Clear") {
                                    model.clearAssets()
                                }
                                .foregroundStyle(.red)
                            }
                        }
                    }
                }

                if !model.isVideoMode {
                    Button("Play Audio") {
                        if let p = model.pages[safe: model.currentPage] {
                            let pageNum = p.page
                            let faKey = "fa-\(pageNum)"
                            let enKey = "en-\(pageNum)"
                            let localNar = if model.preferredNarrationLang == "fa" {
                                model.localNarrationUrls[faKey] ?? model.localNarrationUrls[enKey]
                            } else {
                                model.localNarrationUrls[enKey] ?? model.localNarrationUrls[faKey]
                            }
                            let urlStr = localNar ?? (model.preferredNarrationLang == "fa" ? (p.narrationFa?.url ?? p.narrationEn?.url) : (p.narrationEn?.url ?? p.narrationFa?.url))
                            if let u = urlStr {
                                model.playAudio(urlString: u, rate: model.playbackRate, autoAdvance: true)
                            }
                        }
                    }
                    PardisControlGroup(label: "Narration language") {
                        HStack(spacing: 4) {
                            Button(model.preferredNarrationLang == "fa" ? "FA ✓" : "FA") { model.setNarrationLang(lang: "fa") }
                            Button(model.preferredNarrationLang == "en" ? "EN ✓" : "EN") { model.setNarrationLang(lang: "en") }
                        }
                    }
                    PardisControlGroup(label: "Playback speed") {
                        HStack(spacing: 4) {
                            Button("0.5x") { model.setPlaybackRate(rate:0.5) }
                            Button("1x") { model.setPlaybackRate(rate:1.0) }
                            Button("1.5x") { model.setPlaybackRate(rate:1.5) }
                            Button("2x") { model.setPlaybackRate(rate:2.0) }
                        }
                    }
                    let hasLocal = model.localVideoUrlFa != nil || model.localVideoUrlEn != nil || !model.localIllustrationUrls.isEmpty || !model.localNarrationUrls.isEmpty
                    if hasLocal {
                        Button("Clear offline") {
                            model.clearAssets()
                        }
                        .foregroundStyle(.red)
                    }
                }
            }
        }
        .padding()
        .pardisScreenBackground()
        .sheet(item: $selectedVocab, onDismiss: { model.dismissVocab() }) { selection in
            let v = selection.vocab
            PardisVocabSheetContent(
                vocab: v,
                onPlayPronunciation: v.audioUrl != nil ? {
                    model.playAudio(urlString: v.audioUrl!, rate: 1.0, autoAdvance: false)
                } : nil,
                onClose: {
                    selectedVocab = nil
                    model.dismissVocab()
                }
            )
            .padding()
            .frame(maxWidth: .infinity, alignment: .leading)
            .presentationDetents([.medium, .large])
        }
        .task {
            await model.activate()
        }
        .onAppear {
            model.load(slug: slug)
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


/// Native AVPlayer wrapper for MP4 video mode in Reader.
/// Syncs playback time to cues to auto-advance pages (via onPageChange -> GoToPage).
/// Seeks on external currentPage changes (prev/next or from Android sync).
/// Basic custom "subtitles": the bilingual page text below player updates live as page changes.
struct VideoPlayerView: UIViewRepresentable {
    let videoUrl: String
    let cues: [SubtitleCue]
    let currentPage: Int
    let onPageChange: (Int) -> Void

    func makeUIView(context: Context) -> UIView {
        let view = UIView(frame: .zero)
        // Support remote http(s) URLs and local absolute file paths (offline asset cache from OfflineAssetCache).
        let playerURL: URL = {
            if videoUrl.hasPrefix("/") || videoUrl.hasPrefix("file:") {
                return URL(fileURLWithPath: videoUrl.replacingOccurrences(of: "file://", with: ""))
            } else {
                return URL(string: videoUrl)!
            }
        }()
        let player = AVPlayer(url: playerURL)
        let playerLayer = AVPlayerLayer(player: player)
        playerLayer.videoGravity = .resizeAspect
        view.layer.addSublayer(playerLayer)
        context.coordinator.player = player
        context.coordinator.playerLayer = playerLayer
        context.coordinator.cues = cues
        context.coordinator.onPageChange = onPageChange

        // Start playback
        player.play()

        // Periodic time observer for cue-driven page sync (every ~300ms)
        let interval = CMTime(seconds: 0.3, preferredTimescale: 600)
        context.coordinator.timeObserver = player.addPeriodicTimeObserver(forInterval: interval, queue: .main) { time in
            let pos = time.seconds
            if let matching = context.coordinator.cues.first(where: { pos >= $0.startSec && pos < $0.endSec }) {
                let page = Int(matching.pageIndex) // SubtitleCue.pageIndex is Kotlin Int -> Swift Int32
                if page != context.coordinator.lastSyncedPage {
                    context.coordinator.lastSyncedPage = page
                    context.coordinator.onPageChange(page)
                }
            }
        }

        return view
    }

    func updateUIView(_ uiView: UIView, context: Context) {
        // Update layer frame on size changes
        if let layer = context.coordinator.playerLayer {
            layer.frame = uiView.bounds
        }
        // Seek if currentPage changed externally (user prev/next or other sync)
        let player = context.coordinator.player
        if let player = player, let cue = cues.first(where: { Int($0.pageIndex) == currentPage }) {
            let target = CMTime(seconds: cue.startSec, preferredTimescale: 600)
            // Only seek if far from current pos (avoid feedback loop)
            let current = player.currentTime().seconds
            if abs(current - cue.startSec) > 1.5 {
                player.seek(to: target)
            }
        }
        // Update coordinator state
        context.coordinator.cues = cues
        context.coordinator.onPageChange = onPageChange
    }

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    final class Coordinator: NSObject {
        var player: AVPlayer?
        var playerLayer: AVPlayerLayer?
        var timeObserver: Any?
        var cues: [SubtitleCue] = []
        var onPageChange: (Int) -> Void = { _ in }
        var lastSyncedPage: Int = -1

        deinit {
            if let obs = timeObserver, let p = player {
                p.removeTimeObserver(obs)
            }
            player?.pause()
        }
    }
}
