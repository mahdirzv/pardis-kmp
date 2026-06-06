import Foundation
import Shared
import SwiftUI

/// Modern iOS adapter following kmpSkill.md recommendation:
/// - Use @Observable (iOS 17+)
/// - @State in the View (not @StateObject)
/// - Activate collection inside .task { } for proper lifetime
@MainActor
@Observable
final class LibrarySharedViewModel {
    private let viewModel: LibraryViewModel

    // Exposed as observable properties (no @Published needed with @Observable)
    var stories: [Story] = []
    var isLoading = false
    var errorMessage: String?
    var cachedStorySlugs: Set<String> = []
    var searchQuery: String = ""
    var showOnlyCached = false
    var localCoverUrls: [String: String] = [:]
    var ageBands: [String] = []
    var selectedAgeBand: String? = nil
    var downloadProgress: [String: String] = [:]
    var downloadedSizeLabels: [String: String] = [:]
    var failedDownloads: Set<String> = []
    var totalCachedLabel: String = ""

    init(viewModel: LibraryViewModel = PardisViewModelProvider.shared.libraryViewModel()) {
        self.viewModel = viewModel
    }

    func activate() async {
        for await state in viewModel.uiState {
            apply(state)
        }
    }

    func refresh() {
        viewModel.onAction(action: LibraryActionRefresh.shared)
    }

    func search(query: String) {
        viewModel.onAction(action: LibraryActionSearch(query: query))
    }

    func toggleShowOnlyCached() {
        viewModel.onAction(action: LibraryActionToggleShowOnlyCached.shared)
    }

    func setAgeBand(_ band: String?) {
        viewModel.onAction(action: LibraryActionSetAgeBand(band: band))
    }

    func downloadStory(_ slug: String) {
        viewModel.onAction(action: LibraryActionDownloadStory(slug: slug))
    }

    func cancelDownload(_ slug: String) {
        viewModel.onAction(action: LibraryActionCancelDownload(slug: slug))
    }

    func removeDownload(_ slug: String) {
        viewModel.onAction(action: LibraryActionRemoveDownload(slug: slug))
    }

    private func apply(_ state: LibraryUiState) {
        self.stories = state.stories
        self.isLoading = state.isLoading
        self.errorMessage = state.errorMessage
        self.cachedStorySlugs = Set(state.cachedStorySlugs)
        self.searchQuery = state.searchQuery
        self.showOnlyCached = state.showOnlyCached
        self.localCoverUrls = state.localCoverUrls
        self.ageBands = state.ageBands
        self.selectedAgeBand = state.selectedAgeBand
        self.downloadProgress = state.downloadProgress
        self.downloadedSizeLabels = state.downloadedSizeLabels
        self.failedDownloads = Set(state.failedDownloads)
        self.totalCachedLabel = state.totalCachedLabel
    }
}