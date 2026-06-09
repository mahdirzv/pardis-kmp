import SwiftUI
import CoreText

/// Registers the bundled brand fonts programmatically and reads each font's real family name from
/// the file itself — so `PardisFonts` never depends on a hand-guessed name or on Info.plist /
/// XcodeGen resource quirks. Call `registerAll()` once at app startup (before any view renders).
///
/// This is the single source of truth for font loading on iOS: if a file isn't in the bundle, that
/// is loudly logged (and the CI "Verify fonts bundled" step fails the build).
enum PardisFontRegistry {
    enum Role: String, CaseIterable { case display, body, persian, mono }

    private static let fileForRole: [Role: String] = [
        .display: "bricolage_grotesque",   // Bricolage Grotesque
        .body: "plus_jakarta_sans",        // Plus Jakarta Sans
        .persian: "vazirmatn",             // Vazirmatn
        .mono: "jetbrains_mono",           // JetBrains Mono
    ]

    private static var familyByRole: [Role: String] = [:]

    static func registerAll() {
        for role in Role.allCases {
            guard let file = fileForRole[role] else { continue }
            guard let url = Bundle.main.url(forResource: file, withExtension: "ttf") else {
                print("PARDIS-FONT ❌ \(file).ttf NOT in bundle")
                continue
            }
            guard let provider = CGDataProvider(url: url as CFURL), let cg = CGFont(provider) else {
                print("PARDIS-FONT ❌ \(file).ttf unreadable")
                continue
            }
            // Register by URL (.process scope); ignore the "already registered" error on re-init.
            CTFontManagerRegisterFontsForURL(url as CFURL, .process, nil)
            // Read the real family name straight from the file so PardisFonts can reference it.
            let ctFont = CTFontCreateWithGraphicsFont(cg, 12, nil, nil)
            let family = CTFontCopyFamilyName(ctFont) as String
            familyByRole[role] = family
            print("PARDIS-FONT ✅ \(file).ttf → family \"\(family)\"")
        }
    }

    /// The real registered family name for a role, or nil if the font failed to load.
    static func family(_ role: Role) -> String? { familyByRole[role] }

    /// One-line status (role=family | … | role=MISSING) surfaced via an invisible accessibility
    /// element so the UI test can read + log exactly which fonts loaded — a measurement, not a guess.
    static var summary: String {
        Role.allCases.map { "\($0.rawValue)=\(familyByRole[$0] ?? "MISSING")" }.joined(separator: " | ")
    }
}
