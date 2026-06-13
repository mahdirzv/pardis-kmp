import Foundation
import Shared

/// Adapter over the shared `StoryFinishViewModel`, same pattern as the other shared adapters.
@MainActor
@Observable
final class FinishSharedViewModel {
    private let viewModel: StoryFinishViewModel

    var title = ""
    var nextSlug: String?
    var words: [VocabItem] = []
    var isLoading = false
    var errorMessage: String?

    init(viewModel: StoryFinishViewModel = PardisViewModelProvider.shared.storyFinishViewModel()) {
        self.viewModel = viewModel
    }

    func activate() async {
        for await state in viewModel.uiState {
            apply(state)
        }
    }

    func load(slug: String) {
        viewModel.onAction(action: StoryFinishActionLoad(slug: slug))
    }

    private func apply(_ state: StoryFinishUiState) {
        self.title = state.title
        self.nextSlug = state.nextSlug
        self.words = state.words
        self.isLoading = state.isLoading
        self.errorMessage = state.errorMessage
    }
}
