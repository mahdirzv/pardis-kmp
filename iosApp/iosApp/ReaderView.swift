import SwiftUI
import Shared
import AVKit
import AVFoundation

/// Reader v2, mirroring Android `PardisReaderScreen` component by component:
/// ReaderTopBar, ReaderPageDots, illustration/video box, prose (EN + tappable-glossary Farsi),
/// vocab help row, ReaderVideoControls, ReaderDock, ReaderWordCard sheet.
/// Note: the illustration box omits the paisley pattern overlay like other iOS screens
/// (needs asset-catalog motif images - known follow-up).

/// Strips inline cue tags like " [intro]" from prose, mirroring Android's `cueTagRegex`.
private func stripCueTags(_ text: String) -> String {
    text.replacingOccurrences(of: "\\s*\\[[^\\]]*]", with: "", options: .regularExpression)
}

private struct SelectedVocab: Identifiable {
    let vocab: VocabItem
    var id: String { "\(vocab.fa)-\(vocab.translit)-\(vocab.en)" }
}

struct ReaderScreen: View {
    let slug: String
    var onFinish: (String) -> Void = { _ in }

    @State private var model = ReaderSharedViewModel()
    @State private var selectedVocab: SelectedVocab? = nil
    @State private var displayLang = "both" // both | en | fa
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
        ZStack {
            PardisColors.background.ignoresSafeArea()
            if model.isLoading && model.pages.isEmpty {
                ProgressView().tint(PardisColors.saffron)
            } else if let err = model.errorMessage {
                VStack(spacing: PardisSpacing.sm) {
                    Text("Error: \(err)")
                        .font(PardisFonts.body(size: PardisTypography.base, weight: .regular))
                        .foregroundStyle(PardisColors.error)
                    Button(action: { model.load(slug: slug) }) {
                        Text("Retry")
                            .font(PardisFonts.body(size: PardisTypography.sm, weight: .semibold))
                            .foregroundStyle(PardisColors.inkOnDark)
                            .padding(.horizontal, PardisSpacing.lg)
                            .padding(.vertical, PardisSpacing.sm)
                            .background(PardisColors.saffron)
                            .clipShape(Capsule(style: .continuous))
                    }
                    .buttonStyle(.plain)
                }
                .padding(PardisSpacing.lg)
            } else if model.pages.isEmpty {
                Text("No pages loaded for \(slug)")
                    .font(PardisFonts.body(size: PardisTypography.base, weight: .regular))
                    .foregroundStyle(PardisColors.inkSoft)
            } else {
                readerContent
            }
        }
        // Native nav bar inside the modal: a visible (blurred) toolbar background means page content
        // scrolls — and blurs — behind it for free on iOS 17, replacing the hand-rolled chrome.
        .navigationBarTitleDisplayMode(.inline)
        .toolbarBackground(.visible, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button(action: { dismiss() }) { Image(systemName: "chevron.down") }
                    .accessibilityLabel("Close")
            }
            ToolbarItem(placement: .principal) {
                VStack(spacing: PardisSpacing.xxs) {
                    Text(storyTitle)
                        .font(PardisFonts.display(size: PardisTypography.sm, weight: .heavy))
                        .foregroundStyle(PardisColors.ink)
                        .lineLimit(1)
                    if !model.pages.isEmpty {
                        Text("PAGE \(model.currentPage + 1) OF \(model.pages.count)")
                            .font(PardisFonts.mono(size: PardisTypography.xs, weight: .semibold))
                            .kerning(0.6)
                            .foregroundStyle(PardisColors.inkMuted)
                    }
                }
            }
            ToolbarItem(placement: .topBarTrailing) {
                Button(action: {}) { Image(systemName: "bookmark") }
                    .accessibilityLabel("Bookmark")
            }
        }
        }
        .sheet(item: $selectedVocab, onDismiss: { model.dismissVocab() }) { selection in
            ReaderWordCard(
                vocab: selection.vocab,
                storyTitle: model.storyTitle,
                onHear: selection.vocab.audioUrl != nil ? {
                    model.playAudio(urlString: selection.vocab.audioUrl!, rate: 1.0, autoAdvance: false)
                } : nil,
                onClose: {
                    selectedVocab = nil
                    model.dismissVocab()
                }
            )
            .presentationDetents([.medium])
            .presentationDragIndicator(.visible)
        }
        .task { await model.activate() }
        .onAppear { model.load(slug: slug) }
        .onDisappear { model.stopAudio() }
        // The shared VM owns selectedVocab (set by ShowVocab, e.g. from a glossary tap);
        // mirror it into the local sheet-driving state.
        .onChange(of: model.selectedVocab) { _, vocab in
            selectedVocab = vocab.map(SelectedVocab.init(vocab:))
        }
    }

    private var storyTitle: String {
        model.storyTitle.isEmpty ? "Reader" : model.storyTitle
    }

    private var readerContent: some View {
        let page = model.pages[safe: model.currentPage] ?? model.pages[0]
        let videoUrl: String? = model.isVideoMode
            ? (model.localVideoUrlFa ?? model.localVideoUrlEn ?? model.videoUrlFa ?? model.videoUrlEn)
            : nil
        let onLastPage = model.currentPage == model.pages.count - 1
        let hasVideo = model.videoUrlFa != nil || model.videoUrlEn != nil

        return VStack(spacing: 0) {
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    Spacer().frame(height: PardisSpacing.sm)

                    // Illustration (or video player when in video mode)
                    ZStack(alignment: .topLeading) {
                        if let videoUrl {
                            VideoPlayerView(
                                videoUrl: videoUrl,
                                cues: model.cues,
                                currentPage: model.currentPage,
                                onPageChange: { model.goToPage(Int32($0)) }
                            )
                        } else {
                            PardisAsyncImageFrame(
                                url: (model.localIllustrationUrls[Int(page.page)] ?? page.illustrationUrl)
                                    .flatMap { $0.hasPrefix("/") ? URL(fileURLWithPath: $0) : URL(string: $0) },
                                accessibilityLabel: "Illustration for page \(page.page)",
                                width: nil,
                                height: 290,
                                placeholderText: "No illustration",
                                cornerRadius: 0
                            )
                            Text("\(model.currentPage + 1) / \(model.pages.count)")
                                .font(PardisFonts.mono(size: PardisTypography.xs, weight: .bold))
                                .foregroundStyle(PardisColors.inkOnDark)
                                .padding(.horizontal, 10)
                                .padding(.vertical, 4)
                                .background(PardisColors.scrim)
                                .clipShape(Capsule(style: .continuous))
                                .padding(12)
                        }
                    }
                    .frame(height: 290)
                    .clipShape(RoundedRectangle(cornerRadius: PardisRadius.xl, style: .continuous))

                    Spacer().frame(height: PardisSpacing.md)

                    // Prose
                    if displayLang != "fa" {
                        Text(stripCueTags(page.paragraphsEn.joined(separator: "\n\n")))
                            .font(PardisFonts.display(size: PardisTypography.lg, weight: .medium))
                            .foregroundStyle(PardisColors.ink)
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }
                    if displayLang != "en" {
                        if displayLang == "both" {
                            Spacer().frame(height: PardisSpacing.md)
                            Rectangle().fill(PardisColors.border).frame(height: 1)
                            Spacer().frame(height: PardisSpacing.md)
                        }
                        FarsiGlossaryText(
                            faText: stripCueTags(page.paragraphsFa.joined(separator: "\n\n")),
                            vocab: page.vocabulary,
                            onTap: { model.showVocab($0) }
                        )
                    }
                    if !page.vocabulary.isEmpty {
                        Spacer().frame(height: PardisSpacing.md)
                        HStack(spacing: 5) {
                            PardisIcon(kind: .languages, size: 13, color: PardisColors.inkFaint)
                            Text("Tap a highlighted word to learn it")
                                .font(PardisFonts.body(size: PardisTypography.sm, weight: .medium))
                                .foregroundStyle(PardisColors.inkFaint)
                        }
                    }

                    // Secondary controls: video toggle + offline (only when the story has video)
                    if hasVideo {
                        Spacer().frame(height: PardisSpacing.md)
                        ReaderVideoControls(model: model)
                    }
                    Spacer().frame(height: PardisSpacing.md)
                }
                .padding(.horizontal, PardisSpacing.lg)
            }

            ReaderDock(
                total: model.pages.count,
                current: model.currentPage,
                onGoTo: { model.goToPage(Int32($0)) },
                displayLang: displayLang,
                onLangChange: { lang in
                    displayLang = lang
                    if lang != "both" { model.setNarrationLang(lang: lang) }
                },
                playing: model.isNarrating,
                onPlayPause: { model.isNarrating ? model.stopAudio() : playNarration() },
                onPrev: {
                    model.stopAudio()
                    model.prevPage()
                },
                prevEnabled: model.currentPage > 0,
                onNext: {
                    model.stopAudio()
                    if onLastPage { onFinish(model.storySlug) } else { model.nextPage() }
                },
                nextIsFinish: onLastPage
            )
        }
    }

    /// Resolve the current page's narration clip (preferring offline-cached files and the
    /// preferred language, falling back to the other track) and play it with auto-advance.
    private func playNarration() {
        guard let page = model.pages[safe: model.currentPage] else { return }
        let pageNum = page.page
        let faKey = "fa-\(pageNum)"
        let enKey = "en-\(pageNum)"
        let localNar = model.preferredNarrationLang == "fa"
            ? model.localNarrationUrls[faKey] ?? model.localNarrationUrls[enKey]
            : model.localNarrationUrls[enKey] ?? model.localNarrationUrls[faKey]
        let url = localNar ?? (
            model.preferredNarrationLang == "fa"
                ? (page.narrationFa?.url ?? page.narrationEn?.url)
                : (page.narrationEn?.url ?? page.narrationFa?.url)
        )
        guard let url else { return }
        model.playAudio(urlString: url, rate: model.playbackRate, autoAdvance: true)
    }
}

/// 42pt circular icon button on the alt background; `rotation` lets the chevron point
/// down (close, 90°) or back (previous, 180°), mirroring Android's rotate/flip variants.
private struct ReaderIconButton: View {
    let icon: PardisIconKind
    let label: String
    let action: () -> Void
    var rotation: Double = 0
    var dim = false

    var body: some View {
        Button(action: action) {
            ZStack {
                Circle().fill(PardisColors.backgroundAlt).frame(width: 42, height: 42)
                PardisIcon(kind: icon, size: 20, color: dim ? PardisColors.inkFaint : PardisColors.ink)
                    .rotationEffect(.degrees(rotation))
            }
        }
        .buttonStyle(.plain)
        .accessibilityLabel(label)
    }
}

/// Page stepper that fills the available width: evenly spread dots reach both edges, past pages are
/// tinted, and the current page's dot expands into a pill. Replaces the bottom player's progress
/// track. Each dot has an enlarged (24pt) vertical tap target.
private struct ReaderStepper: View {
    let total: Int
    let current: Int
    let onGoTo: (Int) -> Void

    var body: some View {
        HStack(spacing: PardisSpacing.xs) {
            ForEach(0..<max(total, 1), id: \.self) { i in
                let color: Color = i == current
                    ? PardisColors.saffron
                    : (i < current ? PardisColors.saffronSoft : PardisColors.border)
                Capsule(style: .continuous)
                    .fill(color)
                    .frame(height: 6)
                    // Inactive dots stay small with tight gaps; the current page's dot absorbs all
                    // the remaining width. minHeight enlarges the tap target.
                    .frame(maxWidth: i == current ? .infinity : 10, minHeight: 22)
                    .contentShape(Rectangle())
                    .onTapGesture { onGoTo(i) }
            }
        }
        .frame(maxWidth: .infinity)
        .animation(.snappy(duration: 0.28), value: current)
    }
}

private struct ReaderVideoControls: View {
    let model: ReaderSharedViewModel

    var body: some View {
        HStack(spacing: PardisSpacing.sm) {
            PardisFilterPill(
                title: model.isVideoMode ? "Read text" : "Watch video",
                selected: model.isVideoMode,
                action: { model.toggleVideo() }
            )
            if model.isVideoMode {
                let hasLocal = model.localVideoUrlFa != nil || model.localVideoUrlEn != nil
                if !hasLocal {
                    PardisFilterPill(
                        title: model.downloadProgress ?? (model.isDownloadingVideo ? "Downloading…" : "Save offline"),
                        selected: false,
                        action: { if !model.isDownloadingVideo { model.downloadVideo(lang: "fa") } }
                    )
                } else {
                    PardisMetaPill(text: "Saved offline", background: PardisColors.mintSoft, foreground: PardisColors.mintDeep)
                    PardisFilterPill(title: "Clear", selected: false, action: { model.clearAssets() })
                }
            }
        }
    }
}

private struct ReaderDock: View {
    let total: Int
    let current: Int
    let onGoTo: (Int) -> Void
    let displayLang: String
    let onLangChange: (String) -> Void
    let playing: Bool
    let onPlayPause: () -> Void
    let onPrev: () -> Void
    let prevEnabled: Bool
    let onNext: () -> Void
    let nextIsFinish: Bool

    var body: some View {
        VStack(spacing: 0) {
            ReaderStepper(total: total, current: current, onGoTo: onGoTo)
            Spacer().frame(height: PardisSpacing.md)
            HStack {
                ReaderSegmented(value: displayLang, onChange: onLangChange)
                Spacer()
                HStack(spacing: 8) {
                    ReaderIconButton(
                        icon: .chevRight,
                        label: "Previous",
                        action: { if prevEnabled { onPrev() } },
                        rotation: 180,
                        dim: !prevEnabled
                    )
                    Button(action: onPlayPause) {
                        ZStack {
                            Circle().fill(PardisColors.saffron).frame(width: 58, height: 58)
                            PardisIcon(kind: playing ? .pause : .play, size: 24, color: PardisColors.inkOnDark)
                        }
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel(playing ? "Pause" : "Play")
                    ReaderIconButton(
                        icon: nextIsFinish ? .check : .chevRight,
                        label: nextIsFinish ? "Finish" : "Next",
                        action: onNext
                    )
                }
            }
        }
        .padding(.horizontal, PardisSpacing.lg)
        .padding(.top, PardisSpacing.md)
        .padding(.bottom, PardisSpacing.lg)
        .background(
            UnevenRoundedRectangle(topLeadingRadius: 24, topTrailingRadius: 24, style: .continuous)
                .fill(PardisColors.surface)
                .ignoresSafeArea(edges: .bottom)
        )
    }
}

private struct ReaderSegmented: View {
    let value: String
    let onChange: (String) -> Void

    private let options: [(key: String, label: String)] = [("en", "EN"), ("both", "Both"), ("fa", "فا")]

    var body: some View {
        HStack(spacing: 2) {
            ForEach(options, id: \.key) { option in
                let selected = value == option.key
                Button(action: { onChange(option.key) }) {
                    Text(option.label)
                        .font(PardisFonts.body(size: PardisTypography.sm, weight: selected ? .bold : .medium))
                        .foregroundStyle(selected ? PardisColors.ink : PardisColors.inkMuted)
                        .padding(.horizontal, 13)
                        .padding(.vertical, 7)
                        .background(selected ? PardisColors.surface : .clear)
                        .clipShape(Capsule(style: .continuous))
                }
                .buttonStyle(.plain)
            }
        }
        .padding(3)
        .background(PardisColors.backgroundAlt)
        .clipShape(Capsule(style: .continuous))
    }
}

/// Renders a Farsi paragraph with glossary words highlighted + tappable, mirroring Android's
/// LinkAnnotation approach: vocab substrings become AttributedString links with a custom
/// scheme, and the in-place OpenURLAction routes taps back to `onTap` (SwiftUI owns hit-testing,
/// robust inside scroll + RTL).
private struct FarsiGlossaryText: View {
    let faText: String
    let vocab: [VocabItem]
    let onTap: (VocabItem) -> Void

    var body: some View {
        Text(annotated)
            .font(PardisFonts.persian(size: PardisTypography.base, weight: .semibold))
            .frame(maxWidth: .infinity, alignment: .trailing)
            .multilineTextAlignment(.trailing)
            .environment(\.layoutDirection, .rightToLeft)
            .environment(\.openURL, OpenURLAction { url in
                if url.scheme == "pardis-vocab",
                   let index = Int(url.host() ?? ""),
                   let item = vocab[safe: index] {
                    onTap(item)
                    return .handled
                }
                return .systemAction
            })
    }

    private var annotated: AttributedString {
        var result = AttributedString()
        var remaining = Substring(faText)
        var guardCount = 0
        while !remaining.isEmpty && guardCount < 400 {
            guardCount += 1
            // Earliest glossary hit in the remaining text (mirror of Android's scan).
            var hit: (range: Range<Substring.Index>, index: Int)? = nil
            for (index, item) in vocab.enumerated() where !item.fa.isEmpty {
                if let range = remaining.range(of: item.fa) {
                    if hit == nil || range.lowerBound < hit!.range.lowerBound {
                        hit = (range, index)
                    }
                }
            }
            guard let hit else {
                result.append(plain(String(remaining)))
                break
            }
            if hit.range.lowerBound > remaining.startIndex {
                result.append(plain(String(remaining[remaining.startIndex..<hit.range.lowerBound])))
            }
            var word = AttributedString(vocab[hit.index].fa)
            word.foregroundColor = PardisColors.indigoDeep
            word.underlineStyle = .single
            word.link = URL(string: "pardis-vocab://\(hit.index)")
            result.append(word)
            remaining = remaining[hit.range.upperBound...]
        }
        return result
    }

    private func plain(_ text: String) -> AttributedString {
        var part = AttributedString(text)
        part.foregroundColor = PardisColors.inkSoft
        return part
    }
}

/// Tappable-Farsi-word card content, presented as a native sheet (Android renders its own
/// scrim + bottom panel; the SwiftUI sheet provides both). Omits the paisley overlay on the
/// word panel like other iOS screens.
private struct ReaderWordCard: View {
    let vocab: VocabItem
    let storyTitle: String
    let onHear: (() -> Void)?
    let onClose: () -> Void

    @State private var added = false

    var body: some View {
        VStack(spacing: 0) {
            VStack(spacing: PardisSpacing.xxs) {
                Text(vocab.fa)
                    .font(PardisFonts.persian(size: PardisTypography.xxxl, weight: .heavy))
                    .foregroundStyle(PardisColors.indigoDeep)
                Text(vocab.translit)
                    .font(PardisFonts.body(size: PardisTypography.sm, weight: .regular))
                    .foregroundStyle(PardisColors.indigo)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 22)
            .padding(.horizontal, 20)
            .background(PardisColors.indigoTint)
            .clipShape(RoundedRectangle(cornerRadius: PardisRadius.lg, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: PardisRadius.lg, style: .continuous)
                    .stroke(PardisColors.indigoSoft, lineWidth: 1)
            )

            Spacer().frame(height: PardisSpacing.md)
            Text("\u{201C}\(vocab.en)\u{201D}")
                .font(PardisFonts.display(size: PardisTypography.xl, weight: .bold))
                .foregroundStyle(PardisColors.ink)
                .multilineTextAlignment(.center)
            if !storyTitle.isEmpty {
                Text("from \(storyTitle)")
                    .font(PardisFonts.body(size: PardisTypography.sm, weight: .medium))
                    .foregroundStyle(PardisColors.inkMuted)
            }
            Spacer().frame(height: PardisSpacing.md)

            HStack(spacing: PardisSpacing.sm) {
                Button(action: { onHear?() }) {
                    HStack(spacing: 8) {
                        PardisIcon(kind: .volume, size: 18, color: PardisColors.saffronDeep)
                        Text("Hear it")
                            .font(PardisFonts.display(size: PardisTypography.base, weight: .bold))
                            .foregroundStyle(PardisColors.saffronDeep)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 13)
                    .background(PardisColors.saffronSoft)
                    .clipShape(Capsule(style: .continuous))
                }
                .buttonStyle(.plain)
                .disabled(onHear == nil)

                Button(action: { added = true }) {
                    HStack(spacing: 8) {
                        PardisIcon(kind: added ? .check : .sprout, size: 18, color: PardisColors.inkOnDark)
                        Text(added ? "In your garden" : "Add to garden")
                            .font(PardisFonts.display(size: PardisTypography.base, weight: .bold))
                            .foregroundStyle(PardisColors.inkOnDark)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 13)
                    .background(added ? PardisColors.mint : PardisColors.ink)
                    .clipShape(Capsule(style: .continuous))
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.horizontal, PardisSpacing.lg)
        .padding(.top, PardisSpacing.lg)
        .padding(.bottom, PardisSpacing.xl)
        .presentationBackground(PardisColors.surface)
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

        // Periodic time observer for cue-driven page sync (every ~300ms).
        // Capture the coordinator WEAKLY: the observer block is retained by the player, the player is
        // retained by the coordinator, so a strong capture here would form a cycle (coordinator ->
        // player -> block -> coordinator) and `deinit` would never fire to remove the observer.
        let interval = CMTime(seconds: 0.3, preferredTimescale: 600)
        context.coordinator.timeObserver = player.addPeriodicTimeObserver(forInterval: interval, queue: .main) { [weak coordinator = context.coordinator] time in
            guard let coordinator else { return }
            let pos = time.seconds
            if let matching = coordinator.cues.first(where: { pos >= $0.startSec && pos < $0.endSec }) {
                let page = Int(matching.pageIndex) // SubtitleCue.pageIndex is Kotlin Int -> Swift Int32
                if page != coordinator.lastSyncedPage {
                    coordinator.lastSyncedPage = page
                    coordinator.onPageChange(page)
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
