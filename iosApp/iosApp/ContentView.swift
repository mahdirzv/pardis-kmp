import SwiftUI
import Shared
import AVKit
import AVFoundation

private struct ReaderRoute: Hashable {
    let slug: String
}

private struct SelectedVocab: Identifiable {
    let vocab: VocabItem
    var id: String { "\(vocab.fa)-\(vocab.translit)-\(vocab.en)" }
}

private enum PardisRootTab: CaseIterable {
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

    var subtitle: String {
        switch self {
        case .today: return "Daily reading rhythm"
        case .library: return "Persian heritage stories"
        case .bedtime: return "Calmer stories for later"
        case .rewards: return "Reading progress and badges"
        case .you: return "Family profile and preferences"
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
    @State private var selectedRoute: ReaderRoute? = nil
    @State private var selectedTab: PardisRootTab = .library

    var body: some View {
        NavigationStack {
            RootShellView(
                selectedTab: $selectedTab,
                onSelectStory: { slug in selectedRoute = ReaderRoute(slug: slug) }
            )
                .navigationDestination(item: $selectedRoute) { route in
                    ReaderScreen(slug: route.slug)
                }
        }
    }
}

private struct RootShellView: View {
    @Binding var selectedTab: PardisRootTab
    @State private var libraryModel = LibrarySharedViewModel()
    let onSelectStory: (String) -> Void

    private let tabs = PardisRootTab.allCases

    var body: some View {
        ZStack(alignment: .bottom) {
            switch selectedTab {
            case .today:
                TodayScreen(
                    model: libraryModel,
                    onSelect: onSelectStory,
                    onOpenLibrary: { selectedTab = .library },
                    bottomContentPadding: 116
                )
            case .library:
                LibraryScreen(model: libraryModel, onSelect: onSelectStory, bottomContentPadding: 116)
            case .bedtime, .rewards, .you:
                PardisPlaceholderTabScreen(tab: selectedTab)
                    .padding(.bottom, 116)
            }

            PardisBottomTabBar(
                items: tabs.map { PardisTabItem(label: $0.title, icon: $0.icon) },
                selectedIndex: tabs.firstIndex(of: selectedTab) ?? 0,
                onSelect: { selectedTab = tabs[$0] }
            )
            .padding(PardisSpacing.md)
        }
        .pardisScreenBackground()
        .task {
            await libraryModel.activate()
        }
    }
}

private struct TodayScreen: View {
    let model: LibrarySharedViewModel
    let onSelect: (String) -> Void
    let onOpenLibrary: () -> Void
    var bottomContentPadding: CGFloat

    private var featuredStory: Story? {
        model.stories.first
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: PardisSpacing.md) {
                PardisScreenHeader(title: "Today", subtitle: "A calm reading rhythm for the family")
                    .accessibilityAddTraits(.isHeader)

                PardisMetricStrip(metrics: [
                    PardisMetric(
                        value: model.stories.isEmpty && model.isLoading ? "..." : "\(model.stories.count)",
                        label: "Stories",
                        tone: .saffron
                    ),
                    PardisMetric(
                        value: "\(model.cachedStorySlugs.count)",
                        label: model.totalCachedLabel.isEmpty ? "Offline" : model.totalCachedLabel,
                        tone: .mint
                    ),
                    PardisMetric(
                        value: "\(model.ageBands.count)",
                        label: "Age bands",
                        tone: .indigo
                    )
                ])

                if let story = featuredStory {
                    let coverUrlStr = model.localCoverUrls[story.slug] ?? story.coverUrl
                    PardisFeaturedStoryCard(
                        titleEn: story.titleEn,
                        titleFa: story.titleFa,
                        ageBand: story.ageBand,
                        minutes: story.minutes,
                        vocabCount: story.vocabCount,
                        coverUrl: coverUrlStr.flatMap(URL.init(string:)),
                        onOpen: { onSelect(story.slug) },
                        eyebrow: "Continue reading",
                        blurb: story.blurbEn,
                        actionLabel: "Open story"
                    )
                } else {
                    PardisPanel {
                        Text(model.isLoading ? "Loading today's stories..." : "Refresh Library to load today's reading list.")
                            .font(PardisFonts.body(size: PardisTypography.base, weight: .regular))
                            .foregroundStyle(PardisColors.inkSoft)
                    }
                }

                PardisSectionHeader(
                    title: "For later",
                    subtitle: "Short stories that work well before bedtime",
                    actionLabel: "Library",
                    action: onOpenLibrary
                )

                ForEach(Array(model.stories.dropFirst().prefix(3)), id: \.slug) { story in
                    let coverUrlStr = model.localCoverUrls[story.slug] ?? story.coverUrl
                    PardisStoryCard(
                        titleEn: story.titleEn,
                        titleFa: story.titleFa,
                        ageBand: story.ageBand,
                        minutes: story.minutes,
                        vocabCount: story.vocabCount,
                        coverUrl: coverUrlStr.flatMap(URL.init(string:)),
                        downloadProgress: model.downloadProgress[story.slug],
                        downloadedSizeLabel: model.downloadedSizeLabels[story.slug],
                        isFailed: model.failedDownloads.contains(story.slug),
                        onSelect: { onSelect(story.slug) },
                        onDownload: { model.downloadStory(story.slug) },
                        onCancel: { model.cancelDownload(story.slug) },
                        onRemove: { model.removeDownload(story.slug) }
                    )
                }

                PardisPanel {
                    HStack(spacing: PardisSpacing.sm) {
                        PardisIcon(kind: .star, color: PardisColors.saffronDeep)
                        VStack(alignment: .leading, spacing: PardisSpacing.xxs) {
                            Text("Vocabulary focus")
                                .font(PardisFonts.display(size: PardisTypography.base, weight: .bold))
                                .foregroundStyle(PardisColors.ink)
                            Text(featuredStory.map { "\($0.vocabCount) words are ready inside \($0.titleEn)." } ?? "Open a story to start collecting new Persian words.")
                                .font(PardisFonts.body(size: PardisTypography.sm, weight: .regular))
                                .foregroundStyle(PardisColors.inkSoft)
                        }
                    }
                }
            }
            .padding()
            .safeAreaPadding(.bottom, bottomContentPadding)
        }
    }
}

private struct PardisPlaceholderTabScreen: View {
    let tab: PardisRootTab

    var body: some View {
        VStack(alignment: .leading, spacing: PardisSpacing.md) {
            PardisScreenHeader(title: tab.title, subtitle: tab.subtitle)
            PardisPanel {
                HStack(spacing: PardisSpacing.sm) {
                    PardisIcon(kind: tab.icon, color: PardisColors.indigo)
                    Text("\(tab.title) is ready for its shared state contract.")
                        .font(PardisFonts.body(size: PardisTypography.base, weight: .regular))
                        .foregroundStyle(PardisColors.inkSoft)
                }
            }
            Spacer(minLength: 0)
        }
        .padding()
    }
}

struct LibraryScreen: View {
    let model: LibrarySharedViewModel
    var onSelect: (String) -> Void
    var bottomContentPadding: CGFloat = 0

    private var storySectionSubtitle: String {
        if let band = model.selectedAgeBand {
            return "Filtered for \(band)"
        }
        return "All available stories"
    }

    var body: some View {
        VStack(alignment: .leading, spacing: PardisSpacing.sm) {
            PardisScreenHeader(title: "Pardis", subtitle: "Persian heritage stories")
                .accessibilityAddTraits(.isHeader)

            PardisMetricStrip(metrics: [
                PardisMetric(
                    value: "\(model.stories.count)",
                    label: "Stories",
                    tone: .saffron
                ),
                PardisMetric(
                    value: "\(model.ageBands.count)",
                    label: "Age bands",
                    tone: .indigo
                ),
                PardisMetric(
                    value: "\(model.cachedStorySlugs.count)",
                    label: model.totalCachedLabel.isEmpty ? "Offline" : model.totalCachedLabel,
                    tone: .mint
                )
            ])

            if let story = model.stories.first {
                let coverUrlStr = model.localCoverUrls[story.slug] ?? story.coverUrl
                PardisFeaturedStoryCard(
                    titleEn: story.titleEn,
                    titleFa: story.titleFa,
                    ageBand: story.ageBand,
                    minutes: story.minutes,
                    vocabCount: story.vocabCount,
                    coverUrl: coverUrlStr.flatMap(URL.init(string:)),
                    onOpen: { onSelect(story.slug) },
                    blurb: story.blurbEn
                )
            }

            PardisPanel {
                HStack(spacing: PardisSpacing.sm) {
                    PardisIcon(kind: .search, size: 16, color: PardisColors.inkMuted)
                    TextField(
                        "Search stories",
                        text: Binding(
                            get: { model.searchQuery },
                            set: { query in
                                model.searchQuery = query
                                model.search(query: query)
                            }
                        )
                    )
                        .textFieldStyle(.plain)
                }
                .padding(.horizontal, PardisSpacing.sm)
                .frame(height: 44)
                .background(PardisColors.backgroundAlt)
                .clipShape(RoundedRectangle(cornerRadius: PardisRadius.sm, style: .continuous))
            }

            if !model.ageBands.isEmpty {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: PardisSpacing.sm) {
                        PardisFilterPill(title: "All ages", selected: model.selectedAgeBand == nil) {
                            model.setAgeBand(nil)
                        }
                        ForEach(model.ageBands, id: \.self) { band in
                            PardisFilterPill(title: band, selected: model.selectedAgeBand == band) {
                                model.setAgeBand(model.selectedAgeBand == band ? nil : band)
                            }
                        }
                    }
                }
            }

            PardisPanel {
                Button(model.showOnlyCached ? "Show all stories" : "Show only offline cached") {
                    model.toggleShowOnlyCached()
                }

                if !model.totalCachedLabel.isEmpty {
                    Text("Cached offline: \(model.totalCachedLabel)")
                        .font(.caption)
                        .foregroundStyle(PardisColors.inkSoft)
                }
            }

            PardisSectionHeader(
                title: "Stories",
                subtitle: storySectionSubtitle,
                actionLabel: "Refresh",
                action: { model.refresh() }
            )

            if model.isLoading && model.stories.isEmpty {
                ProgressView().tint(PardisColors.saffron)
            }

            List(model.stories, id: \.slug) { story in
                let coverUrlStr = model.localCoverUrls[story.slug] ?? story.coverUrl
                PardisStoryCard(
                    titleEn: story.titleEn,
                    titleFa: story.titleFa,
                    ageBand: story.ageBand,
                    minutes: story.minutes,
                    vocabCount: story.vocabCount,
                    coverUrl: coverUrlStr.flatMap(URL.init(string:)),
                    downloadProgress: model.downloadProgress[story.slug],
                    downloadedSizeLabel: model.downloadedSizeLabels[story.slug],
                    isFailed: model.failedDownloads.contains(story.slug),
                    onSelect: { onSelect(story.slug) },
                    onDownload: { model.downloadStory(story.slug) },
                    onCancel: { model.cancelDownload(story.slug) },
                    onRemove: { model.removeDownload(story.slug) }
                )
                .listRowInsets(EdgeInsets(top: 4, leading: 0, bottom: 4, trailing: 0))
                .listRowBackground(Color.clear)
                .listRowSeparator(.hidden)
            }
            .listStyle(.plain)
            .scrollContentBackground(.hidden)
            .safeAreaPadding(.bottom, bottomContentPadding)
        }
        .padding()
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
