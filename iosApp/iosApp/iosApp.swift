import SwiftUI
import Shared

@main
struct PardisiOSApp: App {
    init() {
        // Provide any iOS platform config (e.g. future auth tokens)
        let platformModules: [Any] = [] // In real: build Koin modules from Swift
        // SharedInit is called from a provider or directly if no platform deps
        // For scaffold, assume SharedInit.init() is called; in practice use Beforely-like platformModules
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}