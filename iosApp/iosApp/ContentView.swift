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

struct ContentView: View {
    @State private var selectedRoute: ReaderRoute? = nil

    var body: some View {
        NavigationStack {
            LibraryScreen(onSelect: { slug in selectedRoute = ReaderRoute(slug: slug) })
                .navigationDestination(item: $selectedRoute) { route in
                    ReaderScreen(slug: route.slug)
                }
        }
    }
}

struct LibraryScreen: View {
    @State private var model = LibrarySharedViewModel()
    var onSelect: (String) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            PardisScreenHeader(title: "Pardis", subtitle: "Persian heritage stories")
                .accessibilityAddTraits(.isHeader)

            PardisPanel {
                TextField("Search stories", text: $model.searchQuery)
                    .textFieldStyle(.roundedBorder)
                    .onChange(of: model.searchQuery) {
                        model.search(query: model.searchQuery)
                    }
            }

            if !model.ageBands.isEmpty {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
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

            if model.isLoading && model.stories.isEmpty {
                ProgressView().tint(PardisColors.saffron)
            }

            List(model.stories, id: \.slug) { story in
                HStack {
                    let coverUrlStr = model.localCoverUrls[story.slug] ?? story.coverUrl
                    PardisAsyncImageFrame(
                        url: coverUrlStr.flatMap(URL.init(string:)),
                        accessibilityLabel: "Cover image for \(story.titleEn)",
                        width: 68,
                        height: 68,
                        placeholderText: "No cover"
                    )
                    .padding(.trailing, PardisSpacing.sm)
                    VStack(alignment: .leading, spacing: 4) {
                        Text(story.titleEn)
                            .font(.system(size: PardisTypography.base, weight: .bold, design: .rounded))
                        PersianReaderText(
                            text: story.titleFa,
                            font: .system(size: PardisTypography.sm, weight: .medium, design: .rounded),
                            color: PardisColors.indigo
                        )
                        HStack(spacing: 6) {
                            PardisMetaPill(text: "\(story.ageBand) • \(story.minutes)m", background: PardisColors.saffronTint, foreground: PardisColors.saffronDeep)
                            PardisMetaPill(text: "\(story.vocabCount) words", background: PardisColors.indigoTint, foreground: PardisColors.indigoDeep)
                        }
                        if let progress = model.downloadProgress[story.slug] {
                            HStack {
                                PardisMetaPill(text: progress, background: PardisColors.backgroundAlt, foreground: PardisColors.inkSoft)
                                Spacer()
                                Button("Cancel") { model.cancelDownload(story.slug) }
                                    .buttonStyle(.bordered).controlSize(.small)
                            }
                        } else if let size = model.downloadedSizeLabels[story.slug] {
                            HStack {
                                PardisMetaPill(text: "Offline • \(size)", background: PardisColors.mintSoft, foreground: PardisColors.mintDeep)
                                Spacer()
                                Button("Remove") { model.removeDownload(story.slug) }
                                    .buttonStyle(.bordered).controlSize(.small)
                            }
                        } else if model.failedDownloads.contains(story.slug) {
                            HStack {
                                Text("Download failed").font(.caption).foregroundStyle(.red)
                                Spacer()
                                Button("Retry") { model.downloadStory(story.slug) }
                                    .buttonStyle(.borderedProminent).controlSize(.small).tint(PardisColors.saffron)
                            }
                        } else {
                            Button("Download offline") { model.downloadStory(story.slug) }
                                .buttonStyle(.borderedProminent).controlSize(.small).tint(PardisColors.saffron)
                        }
                    }
                }
                .padding(PardisSpacing.md)
                .pardisCardSurface(cornerRadius: PardisRadius.lg)
                .contentShape(Rectangle())
                .onTapGesture { onSelect(story.slug) }
                .listRowInsets(EdgeInsets(top: 4, leading: 0, bottom: 4, trailing: 0))
                .listRowBackground(Color.clear)
                .listRowSeparator(.hidden)
            }
            .listStyle(.plain)
            .scrollContentBackground(.hidden)

            Button("Refresh") {
                model.refresh()
            }
            .buttonStyle(PardisPrimaryButtonStyle())
        }
        .padding()
        .pardisScreenBackground()
        .task {
            await model.activate()
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
                isOffline: hasOffline
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