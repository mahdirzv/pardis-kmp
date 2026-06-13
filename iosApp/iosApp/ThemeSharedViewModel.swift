import Foundation
import Shared

/// Adapter over the shared `ThemeViewModel`, same pattern as `ProfileSharedViewModel`:
/// @Observable (iOS 17+), @State in the view, state collection inside .task { }. App-lifetime.
@MainActor
@Observable
final class ThemeSharedViewModel {
    private let viewModel: ThemeViewModel

    var preference: ThemePreference = .system
    var isLoading = true

    init(viewModel: ThemeViewModel = PardisViewModelProvider.shared.themeViewModel()) {
        self.viewModel = viewModel
    }

    // iOS has no ViewModelStore to trigger the Kotlin VM's internal clear(); cancel its scope.
    deinit { viewModel.dispose() }

    func activate() async {
        for await state in viewModel.uiState {
            apply(state)
        }
    }

    func setPreference(_ preference: ThemePreference) {
        viewModel.onAction(action: ThemeActionSetPreference(preference: preference))
    }

    private func apply(_ state: ThemeUiState) {
        self.preference = state.preference
        self.isLoading = state.isLoading
    }
}
