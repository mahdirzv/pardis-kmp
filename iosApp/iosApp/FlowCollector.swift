import SwiftUI
import Shared  // or the framework name

// Minimal helper to observe Skie StateFlow in SwiftUI (using SKIE for async bridging)
extension View {
    func collect<T>(flow: SkieSwiftStateFlow<T>, onEach: @escaping (T) -> Void) -> some View {
        self.task {
            for await value in flow {
                await MainActor.run {
                    onEach(value)
                }
            }
        }
    }
}