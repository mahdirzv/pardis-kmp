import SwiftUI
import Shared
import AVKit
import AVFoundation

struct ContentView: View {
    @State private var selectedSlug: String? = nil

    var body: some View {
        NavigationStack {
            LibraryScreen(onSelect: { slug in selectedSlug = slug })
                .navigationDestination(item: $selectedSlug) { slug in
                    ReaderScreen(slug: slug)
                }
        }
        .environment(\.layoutDirection, .rightToLeft) // RTL for Farsi/Persian content (bilingual handled per text)
    }
}

struct LibraryScreen: View {
    @State private var model = LibrarySharedViewModel()
    var onSelect: (String) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Pardis")
                .font(.largeTitle)
                .foregroundStyle(PardisColors.indigo)
                .accessibilityAddTraits(.isHeader)
            Text("Persian heritage stories")
                .foregroundStyle(PardisColors.inkSoft)

            // Search
            TextField("Search stories", text: $model.searchQuery)
                .textFieldStyle(.roundedBorder)
                .onChange(of: model.searchQuery) { newValue in
                    model.search(query: newValue)
                }

            // Age-band filter chips (derived from data; tap the active band again to clear).
            if !model.ageBands.isEmpty {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        Button(model.selectedAgeBand == nil ? "All ages ✓" : "All ages") {
                            model.setAgeBand(nil)
                        }
                        .buttonStyle(.bordered)
                        .tint(model.selectedAgeBand == nil ? PardisColors.indigo : PardisColors.inkSoft)
                        ForEach(model.ageBands, id: \.self) { band in
                            Button(model.selectedAgeBand == band ? "\(band) ✓" : band) {
                                model.setAgeBand(model.selectedAgeBand == band ? nil : band)
                            }
                            .buttonStyle(.bordered)
                            .tint(model.selectedAgeBand == band ? PardisColors.indigo : PardisColors.inkSoft)
                        }
                    }
                }
            }

            // Toggle cached only
            Button(model.showOnlyCached ? "Show all stories" : "Show only offline cached") {
                model.toggleShowOnlyCached()
            }

            if !model.totalCachedLabel.isEmpty {
                Text("Cached offline: \(model.totalCachedLabel)")
                    .font(.caption)
                    .foregroundStyle(PardisColors.inkSoft)
            }

            if model.isLoading && model.stories.isEmpty {
                ProgressView().tint(PardisColors.saffron)
            }

            List(model.stories, id: \.slug) { story in
                HStack {
                    let coverUrlStr = model.localCoverUrls[story.slug] ?? story.coverUrl
                    if let cover = coverUrlStr, let url = URL(string: cover) {
                        AsyncImage(url: url) { image in
                            image.resizable().scaledToFill()
                        } placeholder: {
                            Color(PardisColors.surfaceLilac)
                        }
                        .frame(width: 50, height: 50)
                        .cornerRadius(PardisRadius.sm)
                        .padding(.trailing, PardisSpacing.sm)
                        .accessibilityLabel("Cover image for \(story.titleEn)")
                    }
                    VStack(alignment: .leading, spacing: 4) {
                        Text(story.titleEn).font(.headline)
                        Text(story.titleFa).font(.subheadline).foregroundStyle(PardisColors.indigo)
                        Text("\(story.ageBand) • \(story.minutes)m • \(story.vocabCount) words")
                            .font(.caption)
                            .foregroundStyle(PardisColors.inkMuted)
                        if let progress = model.downloadProgress[story.slug] {
                            HStack {
                                Text(progress).font(.caption).foregroundStyle(PardisColors.inkSoft)
                                Spacer()
                                Button("Cancel") { model.cancelDownload(story.slug) }
                                    .buttonStyle(.bordered).controlSize(.small)
                            }
                        } else if let size = model.downloadedSizeLabels[story.slug] {
                            HStack {
                                Text("✓ Offline (\(size))").font(.caption).foregroundStyle(PardisColors.mint)
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
                .background(PardisColors.surface2)
                .cornerRadius(PardisRadius.md)
                .overlay(
                    RoundedRectangle(cornerRadius: PardisRadius.md)
                        .stroke(PardisColors.border, lineWidth: 1)
                )
                .contentShape(Rectangle())
                .onTapGesture { onSelect(story.slug) }
                // TODO: Extract to PardisCard view modifier / struct per Phase 3 design system plan
            }

            Button("Refresh") {
                model.refresh()
            }
            .buttonStyle(.borderedProminent)
            .tint(PardisColors.saffron)
        }
        .padding()
        .background(PardisColors.background.ignoresSafeArea())
        .task {
            await model.activate()
        }
    }
}

struct ReaderScreen: View {
    let slug: String
    @State private var model = ReaderSharedViewModel()
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Button("← Back") { dismiss() }
                    .foregroundStyle(PardisColors.indigo)
                Spacer()
                if !model.pages.isEmpty {
                    Text("\(model.currentPage + 1) / \(model.pages.count)")
                        .foregroundStyle(PardisColors.inkSoft)
                    let hasOffline = model.localVideoUrlFa != nil || model.localVideoUrlEn != nil || !model.localIllustrationUrls.isEmpty || !model.localNarrationUrls.isEmpty
                    if hasOffline {
                        Text("✓ Offline")
                            .font(.caption)
                            .foregroundStyle(PardisColors.mint)
                    }
                }
            }

            if model.isLoading && model.pages.isEmpty {
                ProgressView().tint(PardisColors.saffron)
            } else if let err = model.errorMessage {
                Text("Error: \(err)").foregroundStyle(.red)
            } else if let page = model.pages[safe: model.currentPage] {
                Text("Page \(page.page)")
                    .font(.caption)
                    .foregroundStyle(PardisColors.inkMuted)

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
                        .cornerRadius(12)
                    }

                    Spacer(minLength: 8)

                    ScrollView {
                        VStack(alignment: .leading, spacing: 12) {
                            Text(page.paragraphsFa.joined(separator: "\n\n"))
                                .font(.title3)
                            Text(page.paragraphsEn.joined(separator: "\n\n"))
                                .font(.body)
                                .foregroundStyle(PardisColors.inkSoft)

                            if !page.vocabulary.isEmpty {
                                Text("Vocab").font(.headline)
                                ForEach(page.vocabulary.prefix(3), id: \.fa) { v in
                                    Text("\(v.fa) (\(v.translit)) — \(v.en)")
                                        .font(.caption)
                                        .padding(4)
                                        .background(PardisColors.mintSoft)
                                        .cornerRadius(PardisRadius.sm)
                                        .onTapGesture { model.showVocab(v) }
                                        .accessibilityLabel("Vocabulary: \(v.fa) means \(v.en), transliteration \(v.translit)")
                                        .accessibilityAddTraits(.isButton)
                                }
                            }
                        }
                    }
                } else {
                    // Normal illustration + text mode
                    ScrollView {
                        VStack(alignment: .leading, spacing: 12) {
                            let illoUrlStr = model.localIllustrationUrls[Int(page.page)] ?? page.illustrationUrl
                            if let urlStr = illoUrlStr, let url = URL(string: urlStr) {
                                AsyncImage(url: url) { image in
                                    image.resizable().scaledToFill()
                                } placeholder: {
                                    Color(PardisColors.surfaceLilac)
                                }
                                .frame(height: 220)
                                .cornerRadius(12)
                                .accessibilityLabel("Illustration for page \(page.page)")
                            } else {
                                RoundedRectangle(cornerRadius: 12)
                                    .fill(PardisColors.surfaceLilac)
                                    .frame(height: 220)
                                    .overlay(Text("No illustration").foregroundStyle(PardisColors.inkSoft))
                            }

                            Text(page.paragraphsFa.joined(separator: "\n\n"))
                                .font(.body)

                            Text(page.paragraphsEn.joined(separator: "\n\n"))
                                .font(.callout)
                                .foregroundStyle(PardisColors.inkSoft)

                            if !page.vocabulary.isEmpty {
                                Text("Vocab").font(.headline)
                                ForEach(page.vocabulary.prefix(3), id: \.fa) { v in
                                    Text("\(v.fa) (\(v.translit)) — \(v.en)")
                                        .font(.caption)
                                        .padding(4)
                                        .background(PardisColors.mintSoft)
                                        .cornerRadius(PardisRadius.sm)
                                        .onTapGesture { model.showVocab(v) }
                                        .accessibilityLabel("Vocabulary: \(v.fa) means \(v.en), transliteration \(v.translit)")
                                        .accessibilityAddTraits(.isButton)
                                }
                            }
                        }
                    }
                }
            } else {
                Text("Loading story \(slug)...")
            }

            // Transport split into rows for better UX/accessibility (avoid long cramped row)
            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Button("Prev") { model.prevPage() }.disabled(model.currentPage == 0)
                    Button("Next") { model.nextPage() }
                        .buttonStyle(.borderedProminent)
                        .tint(PardisColors.saffron)
                    Spacer()
                    if model.videoUrlFa != nil || model.videoUrlEn != nil {
                        Button(model.isVideoMode ? "Text" : "Video") { model.toggleVideo() }

                        // Mirror Android: download/cache button for offline video (the main remaining Phase1 item).
                        // Only show in video mode; "Cache for offline" calls the new DownloadVideo action.
                        // Once done, player uses local file path (AVPlayer supports file: URLs).
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
                                Text("✓ Video + assets cached")
                                    .foregroundStyle(PardisColors.mint)
                                Button("Clear") {
                                    model.clearAssets()
                                }
                                .foregroundStyle(.red)
                            }
                        }
                    }
                }

                if !model.isVideoMode {
                    HStack(spacing: 8) {
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
                                    // Retained player in the VM: plays reliably, applies rate, auto-advances on end.
                                    model.playAudio(urlString: u, rate: model.playbackRate, autoAdvance: true)
                                }
                            }
                        }
                        // Lang group
                        Text("Lang:").font(.caption)
                        HStack(spacing: 4) {
                            Button(model.preferredNarrationLang == "fa" ? "FA ✓" : "FA") { model.setNarrationLang(lang: "fa") }
                            Button(model.preferredNarrationLang == "en" ? "EN ✓" : "EN") { model.setNarrationLang(lang: "en") }
                        }
                        // Rate group - compact
                        Text("Rate:").font(.caption)
                        HStack(spacing: 4) {
                            Button("0.5x") { model.setPlaybackRate(rate:0.5) }
                            Button("1x") { model.setPlaybackRate(rate:1.0) }
                            Button("1.5x") { model.setPlaybackRate(rate:1.5) }
                            Button("2x") { model.setPlaybackRate(rate:2.0) }
                        }
                        // Clear if cached
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
        }
        .padding()
        .background(PardisColors.background.ignoresSafeArea())
        .sheet(item: $model.selectedVocab) { v in
            VStack(alignment: .leading, spacing: 8) {
                Text("Vocab").font(.headline).foregroundStyle(PardisColors.indigo)
                Text("\(v.fa)  (\(v.translit))").font(.title3)
                Text(v.en).font(.body)
                if !v.context.isEmpty {
                    Text("in: \(v.context)").font(.caption).foregroundStyle(PardisColors.inkMuted)
                }
                if let audio = v.audioUrl {
                    Button("▶ Play pronunciation") {
                        // Retained player in the VM so it isn't deallocated before it plays.
                        model.playAudio(urlString: audio, rate: 1.0, autoAdvance: false)
                    }.padding(.top, 4)
                }
                Button("Close") { model.dismissVocab() }
                    .padding(.top)
                    .tint(PardisColors.saffron)
            }
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

// VocabItem comes from the Shared framework; .sheet(item:) needs Identifiable.
extension VocabItem: Identifiable {
    public var id: String { fa }
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