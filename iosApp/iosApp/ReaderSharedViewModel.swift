import AVFoundation
import Foundation
import Shared
import SwiftUI

/// Modern iOS adapter following kmpSkill.md recommendation:
/// - Use @Observable (iOS 17+)
/// - @State in the View (not @StateObject)
/// - Activate collection inside .task { } for proper lifetime
@MainActor
@Observable
final class ReaderSharedViewModel {
    private let viewModel: ReaderViewModel

    // Exposed as observable properties
    var storySlug: String = ""
    var pages: [StoryPage] = []
    var currentPage: Int = 0
    var isVideoMode: Bool = false
    var isLoading = false
    var errorMessage: String?
    var videoUrlFa: String?
    var videoUrlEn: String?
    // Local cached paths for offline video (preferred by player when present).
    var localVideoUrlFa: String?
    var localVideoUrlEn: String?
    var isDownloadingVideo = false
    var downloadProgress: String? = nil
    var localIllustrationUrls: [Int: String] = [:]
    var localNarrationUrls: [String: String] = [:] // "fa-3" etc.
    var preferredNarrationLang: String = "fa"
    var playbackRate: Float = 1.0
    var cues: [SubtitleCue] = []
    var selectedVocab: VocabItem? = nil

    // Retained one-shot audio player for narration / vocab pronunciation. A local AVPlayer would be
    // deallocated when the button closure returns (so it wouldn't reliably play); we keep a single
    // strong reference and clean up its end-observer to avoid leaking observers on every tap.
    private var audioPlayer: AVPlayer?
    private var audioEndObserver: NSObjectProtocol?

    init(viewModel: ReaderViewModel = PardisViewModelProvider.shared.readerViewModel()) {
        self.viewModel = viewModel
    }

    /// Play a one-shot audio clip (narration or vocab). Retains the player so it actually plays,
    /// applies the playback rate, and (for narration) auto-advances to the next page on completion.
    func playAudio(urlString: String, rate: Float = 1.0, autoAdvance: Bool = false) {
        guard let url = URL(string: urlString) else { return }
        stopAudio() // tear down any prior clip + its observer first

        let player = AVPlayer(url: url)
        audioPlayer = player
        if let item = player.currentItem {
            audioEndObserver = NotificationCenter.default.addObserver(
                forName: .AVPlayerItemDidPlayToEndTime,
                object: item,
                queue: .main
            ) { [weak self] _ in
                guard let self else { return }
                if autoAdvance && !self.isVideoMode && self.currentPage < self.pages.count - 1 {
                    self.nextPage()
                }
                self.stopAudio()
            }
        }
        player.play()
        player.rate = rate
    }

    /// Stop any playing clip and remove its end-observer (idempotent).
    func stopAudio() {
        audioPlayer?.pause()
        if let observer = audioEndObserver {
            NotificationCenter.default.removeObserver(observer)
            audioEndObserver = nil
        }
        audioPlayer = nil
    }

    func activate() async {
        for await state in viewModel.uiState {
            apply(state)
        }
    }

    func load(slug: String) {
        viewModel.onAction(action: ReaderActionLoadStory(slug: slug))
    }

    func nextPage() {
        viewModel.onAction(action: ReaderActionNextPage.shared)
    }

    func prevPage() {
        viewModel.onAction(action: ReaderActionPrevPage.shared)
    }

    func goToPage(_ page: Int32) {
        viewModel.onAction(action: ReaderActionGoToPage(page: page))
    }

    func toggleVideo() {
        viewModel.onAction(action: ReaderActionToggleVideo.shared)
    }

    func downloadVideo(lang: String = "fa") {
        viewModel.onAction(action: ReaderActionDownloadVideo(lang: lang))
    }

    func setNarrationLang(lang: String) {
        viewModel.onAction(action: ReaderActionSetNarrationLang(lang: lang))
    }

    func setPlaybackRate(rate: Float) {
        viewModel.onAction(action: ReaderActionSetPlaybackRate(rate: rate))
    }

    func clearAssets() {
        viewModel.onAction(action: ReaderActionClearAssets.shared)
    }

    func playNarration() {
        viewModel.onAction(action: ReaderActionPlayNarration.shared)
    }

    func showVocab(_ v: VocabItem) {
        viewModel.onAction(action: ReaderActionShowVocab(vocab: v))
    }

    func dismissVocab() {
        viewModel.onAction(action: ReaderActionDismissVocab.shared)
    }

    private func apply(_ state: ReaderUiState) {
        self.storySlug = state.storySlug
        self.pages = state.pages
        self.currentPage = Int(state.currentPage)
        self.isVideoMode = state.isVideoMode
        self.isLoading = state.isLoading
        self.errorMessage = state.errorMessage
        self.videoUrlFa = state.videoUrlFa
        self.videoUrlEn = state.videoUrlEn
        self.localVideoUrlFa = state.localVideoUrlFa
        self.localVideoUrlEn = state.localVideoUrlEn
        self.isDownloadingVideo = state.isDownloadingVideo
        self.downloadProgress = state.downloadProgress
        // Kotlin Map<Int,String> bridges to [KotlinInt:String]; convert keys to Swift Int.
        self.localIllustrationUrls = Dictionary(
            uniqueKeysWithValues: state.localIllustrationUrls.map { (Int(truncating: $0.key), $0.value) }
        )
        self.localNarrationUrls = state.localNarrationUrls
        self.preferredNarrationLang = state.preferredNarrationLang
        self.playbackRate = state.playbackRate
        self.cues = state.cues
        self.selectedVocab = state.selectedVocab
    }
}