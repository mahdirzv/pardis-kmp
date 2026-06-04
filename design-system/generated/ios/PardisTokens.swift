import SwiftUI

/// Generated from design-system/tokens.json + web neutral.ts
/// Pardis palette only. Use in SwiftUI.
struct PardisColors {
    static let background = Color(hex: "#FAF6EE")
    static let surface = Color.white
    static let saffron = Color(hex: "#F08A2D")
    static let saffronDeep = Color(hex: "#C46A12")
    static let saffronSoft = Color(hex: "#FFE9D2")
    static let indigo = Color(hex: "#2436A1")
    static let indigoDeep = Color(hex: "#1A256E")
    static let indigoSoft = Color(hex: "#E8EBFB")
    static let mint = Color(hex: "#34B57F")
    static let mintSoft = Color(hex: "#DEF5E9")
    static let lilac = Color(hex: "#ECE6FB")
    static let ink = Color(hex: "#14111B")
    static let inkSoft = Color(hex: "#4B4760")
    static let inkMuted = Color(hex: "#8A8499")
    static let border = Color(hex: "#ECE3D0")
}

struct PardisSpacing {
    static let xs: CGFloat = 4
    static let sm: CGFloat = 8
    static let md: CGFloat = 16
    static let lg: CGFloat = 24
    static let xl: CGFloat = 32
}

struct PardisRadius {
    static let sm: CGFloat = 8
    static let md: CGFloat = 12
    static let lg: CGFloat = 18
}

struct PardisMotion {
    static let fast: Double = 150
    static let normal: Double = 250
}

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (1, 1, 1, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue:  Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}