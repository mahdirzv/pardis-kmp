import SwiftUI
import Shared

@main
struct PardisiOSApp: App {
    init() {
        // Register brand fonts before any view renders (reads family names from the bundled .ttf).
        PardisFontRegistry.registerAll()
        // Initialize shared KMP logic + DI early.
        // Platform modules can supply iOS-specific config (e.g. future auth tokens, or overrides).
        // The Supabase public config is provided via iosMain actuals (no literals in common).
        // For offline assets + pages/progress cache on iOS: pass the platform module that wires
        // both the real asset cache and the SQLDelight driver.
        // Example for auth: provide SupabaseClient(SupabaseConfig(anonKey: ..., userToken: jwt))
        SharedInit.shared.doInit(platformModules: [IosPlatformModuleKt.iosOfflineAssetCacheModule])
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}