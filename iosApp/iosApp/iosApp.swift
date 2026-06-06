import SwiftUI
import Shared

@main
struct PardisiOSApp: App {
    init() {
        // Initialize shared KMP logic + DI early.
        // Platform modules can supply iOS-specific config (e.g. future auth tokens, or overrides).
        // The Supabase public config is provided via iosMain actuals (no literals in common).
        // For offline assets + pages cache on iOS: pass [IosPlatformModuleKt.iosOfflineAssetCacheModule] (and wire driver).
        // Example for auth: provide SupabaseClient(SupabaseConfig(anonKey: ..., userToken: jwt))
        SharedInit.shared.doInit(platformModules: [IosPlatformModuleKt.iosOfflineAssetCacheModule])
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}