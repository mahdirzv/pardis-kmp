import Foundation
import Shared

/// Adapter over the shared `StoryDetailViewModel`, same pattern as `LibrarySharedViewModel`:
/// @Observable (iOS 17+), @State in the View, state collection inside .task { }.
@MainActor
@Observable
final class DetailSharedViewModel {
    private let viewModel: StoryDetailViewModel

    var story: Story?
    var words: [VocabItem] = []
    var canResume = false
    var isLoading = false
    var errorMessage: String?

    init(viewModel: StoryDetailViewModel = PardisViewModelProvider.shared.storyDetailViewModel()) {
        self.viewModel = viewModel
    }

    // iOS has no ViewModelStore to trigger the Kotlin VM's internal clear(), so cancel its
    // scope here when the SwiftUI view (and this @State adapter) is torn down.
    deinit { viewModel.dispose() }

    func activate() async {
        for await state in viewModel.uiState {
            apply(state)
        }
    }

    func load(slug: String) {
        viewModel.onAction(action: StoryDetailActionLoadStory(slug: slug))
    }

    func retry() {
        viewModel.onAction(action: StoryDetailActionRetry.shared)
    }

    private func apply(_ state: StoryDetailUiState) {
        self.story = state.story
        self.words = state.words
        self.canResume = state.canResume
        self.isLoading = state.isLoading
        self.errorMessage = state.errorMessage
    }
}
