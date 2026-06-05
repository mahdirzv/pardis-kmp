import SwiftUI

/// Generated from design-system/tokens.json + web neutral.ts
/// Pardis palette only. Use in SwiftUI.
struct PardisColors {
    static let background = Color(hex: "#FAF6EE")
    static let backgroundAlt = Color(hex: "#F3EEDD")
    static let surface = Color.white
    static let surface2 = Color(hex: "#FDFAF0")
    static let surfaceSoft = Color(hex: "#E8EBFB")
    static let surfaceMint = Color(hex: "#DEF5E9")
    static let surfacePeach = Color(hex: "#FFE9D2")
    static let surfaceLilac = Color(hex: "#ECE6FB")
    static let saffron = Color(hex: "#F08A2D")
    static let saffronDeep = Color(hex: "#C46A12")
    static let saffronSoft = Color(hex: "#FFE9D2")
    static let saffronTint = Color(hex: "#FFF4E5")
    static let indigo = Color(hex: "#2436A1")
    static let indigoDeep = Color(hex: "#1A256E")
    static let indigoSoft = Color(hex: "#E8EBFB")
    static let indigoTint = Color(hex: "#E8EBFB")
    static let mint = Color(hex: "#34B57F")
    static let mintDeep = Color(hex: "#1F7A52")
    static let mintSoft = Color(hex: "#DEF5E9")
    static let lilac = Color(hex: "#8B6FE6")
    static let lilacDeep = Color(hex: "#5235B6")
    static let lilacSoft = Color(hex: "#ECE6FB")
    static let ink = Color(hex: "#14111B")
    static let inkSoft = Color(hex: "#4B4760")
    static let inkMuted = Color(hex: "#8A8499")
    static let inkFaint = Color(hex: "#B8B2C5")
    static let inkOnDark = Color.white
    static let border = Color(hex: "#ECE3D0")
    static let borderStrong = Color(hex: "#D9CDB1")
    static let error = Color(hex: "#EF4444")
    static let success = Color(hex: "#34B57F")
    static let warning = Color(hex: "#F4B53A")
}

struct PardisSpacing {
    static let xs: CGFloat = 4
    static let sm: CGFloat = 8
    static let md: CGFloat = 16
    static let lg: CGFloat = 24
    static let xl: CGFloat = 32
    static let xxl: CGFloat = 40
}

struct PardisRadius {
    static let sm: CGFloat = 8
    static let md: CGFloat = 12
    static let lg: CGFloat = 18
    static let xl: CGFloat = 24
    static let full: CGFloat = 999
}

struct PardisMotion {
    static let fast: Double = 150
    static let normal: Double = 250
    static let slow: Double = 400
}

struct PardisTypography {
    // sizes in points (use .system(size: ...))
    static let xs: CGFloat = 12
    static let sm: CGFloat = 14
    static let base: CGFloat = 16
    static let lg: CGFloat = 18
    static let xl: CGFloat = 20
    static let xxl: CGFloat = 24
    static let xxxl: CGFloat = 30
    // weights
    static let weightNormal: Font.Weight = .regular
    static let weightMedium: Font.Weight = .medium
    static let weightSemiBold: Font.Weight = .semibold
    static let weightBold: Font.Weight = .bold
    // line heights (use .lineSpacing or modifier)
    static let lineHeightNormal: CGFloat = 1.55
    static let lineHeightRelaxed: CGFloat = 1.7
}

struct PardisShadows {
    // Reference values for .shadow() or custom
    static let smRadius: CGFloat = 2
    static let mdRadius: CGFloat = 8
    static let lgRadius: CGFloat = 20
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