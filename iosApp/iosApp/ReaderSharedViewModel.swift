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
    var localIllustrationUrls: [Int: String] = [:]
    var localNarrationUrls: [String: String] = [:] // "fa-3" etc.
    var cues: [SubtitleCue] = []
    var selectedVocab: VocabItem? = nil

    init(viewModel: ReaderViewModel = PardisViewModelProvider.shared.readerViewModel()) {
        self.viewModel = viewModel
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
        self.localIllustrationUrls = state.localIllustrationUrls
        self.localNarrationUrls = state.localNarrationUrls
        self.cues = state.cues
        self.selectedVocab = state.selectedVocab
    }
}