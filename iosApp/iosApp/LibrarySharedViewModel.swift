import Foundation
import Shared
import SwiftUI

@MainActor
final class LibrarySharedViewModel: ObservableObject {
    @Published private(set) var stories: [Story] = []
    @Published private(set) var isLoading = false
    @Published private(set) var errorMessage: String?

    private let viewModel: LibraryViewModel

    var uiStateFlow: SkieSwiftStateFlow<LibraryUiState> {
        viewModel.uiState
    }

    init(viewModel: LibraryViewModel = PardisViewModelProvider.shared.libraryViewModel()) {
        self.viewModel = viewModel
    }

    func refresh() {
        viewModel.onAction(action: LibraryActionRefresh.shared)
    }

    func apply(_ state: LibraryUiState) {
        self.stories = state.stories
        self.isLoading = state.isLoading
        self.errorMessage = state.errorMessage
    }
}