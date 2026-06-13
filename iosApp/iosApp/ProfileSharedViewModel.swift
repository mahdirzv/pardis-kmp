import Foundation
import Shared
import SwiftUI

/// iOS adapter for the shared ProfileViewModel — mirrors `LibrarySharedViewModel`.
/// Drives the launch gate (onboarding picker vs. main shell) and the switch-profile flow,
/// matching the Android `PardisApp` profile gate. Uses @Observable + activate-in-.task.
@MainActor
@Observable
final class ProfileSharedViewModel {
    private let viewModel: ProfileViewModel

    var profiles: [ChildProfile] = []
    var selectedProfile: ChildProfile? = nil
    var isLoading = true

    init(viewModel: ProfileViewModel = PardisViewModelProvider.shared.profileViewModel()) {
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

    func select(_ id: String) {
        viewModel.onAction(action: ProfileActionSelect(id: id))
    }

    private func apply(_ state: ProfileUiState) {
        self.profiles = state.profiles
        self.selectedProfile = state.selectedProfile
        self.isLoading = state.isLoading
    }
}
