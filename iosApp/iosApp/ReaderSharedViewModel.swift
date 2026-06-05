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

    private func apply(_ state: ReaderUiState) {
        self.storySlug = state.storySlug
        self.pages = state.pages
        self.currentPage = Int(state.currentPage)
        self.isVideoMode = state.isVideoMode
        self.isLoading = state.isLoading
        self.errorMessage = state.errorMessage
    }
}