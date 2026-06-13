import SwiftUI
import UIKit

/// Generated from design-system/tokens.json + web neutral.ts
/// Pardis palette only. Use in SwiftUI.
struct PardisColors {
    // Scheme-reactive tokens — dark values are the canonical handoff palette (app.css
    // [data-theme="dark"]): neutrals → dark* tokens; accent soft/tint backgrounds darken; accent
    // deep text colors brighten. Base accents (saffron/indigo/…), on-dark overlays and scrims stay
    // constant. surface tints map to the matching accent-soft dark value.
    static let background = Color.dynamic(light: Color(hex: "#FAF6EE"), dark: Color(hex: "#141019"))
    static let backgroundAlt = Color.dynamic(light: Color(hex: "#F3EEDD"), dark: Color(hex: "#1D1825"))
    static let backgroundStage = Color.dynamic(light: Color(hex: "#EDE6D6"), dark: Color(hex: "#241E2D"))
    static let surface = Color.dynamic(light: .white, dark: Color(hex: "#211C2B"))
    static let surface2 = Color.dynamic(light: Color(hex: "#FDFAF0"), dark: Color(hex: "#2A2435"))
    static let surfaceSoft = Color.dynamic(light: Color(hex: "#E8EBFB"), dark: Color(hex: "#1F2547"))
    static let surfaceMint = Color.dynamic(light: Color(hex: "#DEF5E9"), dark: Color(hex: "#143027"))
    static let surfacePeach = Color.dynamic(light: Color(hex: "#FFE9D2"), dark: Color(hex: "#3A2A18"))
    static let surfaceLilac = Color.dynamic(light: Color(hex: "#ECE6FB"), dark: Color(hex: "#261F44"))
    static let darkBackground = Color(hex: "#141019")
    static let darkBackgroundAlt = Color(hex: "#1D1825")
    static let darkSurface = Color(hex: "#211C2B")
    static let darkSurface2 = Color(hex: "#2A2435")
    static let darkBorder = Color(hex: "#322C40")
    static let darkBorderSoft = Color(hex: "#28222F")
    static let darkBorderStrong = Color(hex: "#443C56")
    static let darkInk = Color(hex: "#F4F1FA")
    static let darkInkSoft = Color(hex: "#C3BDD2")
    static let darkInkMuted = Color(hex: "#8E87A0")
    static let darkInkFaint = Color(hex: "#5F596F")
    // Base accents stay constant; *Soft/*Tint backgrounds darken and *Deep text colors brighten in
    // dark, per the canonical handoff. indigoDeep also anchors the night/lapis gradients, which are
    // pinned to literal hexes in PardisGradients so the gradients don't follow the text brightening.
    static let saffron = Color(hex: "#F08A2D")
    static let saffronDeep = Color.dynamic(light: Color(hex: "#C46A12"), dark: Color(hex: "#F2B074"))
    static let saffronSoft = Color.dynamic(light: Color(hex: "#FFE9D2"), dark: Color(hex: "#3A2A18"))
    static let saffronTint = Color.dynamic(light: Color(hex: "#FFF4E5"), dark: Color(hex: "#241B12"))
    static let indigo = Color(hex: "#2436A1")
    static let indigoDeep = Color.dynamic(light: Color(hex: "#1A256E"), dark: Color(hex: "#AEB9F4"))
    static let indigoDarker = Color(hex: "#0F1849")
    static let indigoSoft = Color.dynamic(light: Color(hex: "#E8EBFB"), dark: Color(hex: "#1F2547"))
    static let indigoTint = Color.dynamic(light: Color(hex: "#F0F2FC"), dark: Color(hex: "#181C36"))
    static let mint = Color(hex: "#2FA876")
    static let mintDeep = Color.dynamic(light: Color(hex: "#1F7A52"), dark: Color(hex: "#7FD9B0"))
    static let mintSoft = Color.dynamic(light: Color(hex: "#DEF5E9"), dark: Color(hex: "#143027"))
    static let lilac = Color(hex: "#8B6FE6")
    static let lilacDeep = Color.dynamic(light: Color(hex: "#5235B6"), dark: Color(hex: "#C5B4F4"))
    static let lilacSoft = Color.dynamic(light: Color(hex: "#ECE6FB"), dark: Color(hex: "#261F44"))
    static let rose = Color(hex: "#E1547A")
    static let roseDeep = Color.dynamic(light: Color(hex: "#B83A5E"), dark: Color(hex: "#F18AA6"))
    static let roseSoft = Color.dynamic(light: Color(hex: "#FCDEE6"), dark: Color(hex: "#3A1E28"))
    static let sun = Color(hex: "#F4B53A")
    static let sunDeep = Color.dynamic(light: Color(hex: "#9A6B12"), dark: Color(hex: "#F2C14E"))
    static let sunSoft = Color.dynamic(light: Color(hex: "#FCEAB6"), dark: Color(hex: "#34280F"))
    static let ink = Color.dynamic(light: Color(hex: "#14111B"), dark: Color(hex: "#F4F1FA"))
    static let inkSoft = Color.dynamic(light: Color(hex: "#4B4760"), dark: Color(hex: "#C3BDD2"))
    static let inkMuted = Color.dynamic(light: Color(hex: "#8A8499"), dark: Color(hex: "#8E87A0"))
    static let inkFaint = Color.dynamic(light: Color(hex: "#B6B0C0"), dark: Color(hex: "#5F596F"))
    static let inkOnDark = Color.white
    static let border = Color.dynamic(light: Color(hex: "#ECE3D0"), dark: Color(hex: "#322C40"))
    static let borderSoft = Color.dynamic(light: Color(hex: "#F2ECDD"), dark: Color(hex: "#28222F"))
    static let borderStrong = Color.dynamic(light: Color(hex: "#DDD2BC"), dark: Color(hex: "#443C56"))
    static let error = Color.dynamic(light: Color(hex: "#EF4444"), dark: Color(hex: "#FF8A80"))
    static let errorDark = Color(hex: "#FF8A80")
    static let errorContainerDark = Color(hex: "#6D2C2C")
    static let onErrorContainerDark = Color(hex: "#FFDAD4")
    static let success = Color(hex: "#2FA876")
    static let warning = Color(hex: "#F4B53A")
}

struct PardisSpacing {
    static let none: CGFloat = 0
    static let hairline: CGFloat = 1
    static let xxs: CGFloat = 2
    static let xs: CGFloat = 4
    static let sm: CGFloat = 8
    static let md: CGFloat = 16
    static let lg: CGFloat = 24
    static let xl: CGFloat = 32
    static let xxl: CGFloat = 40
}

struct PardisRadius {
    static let xs: CGFloat = 8
    static let sm: CGFloat = 12
    static let base: CGFloat = 16
    static let md: CGFloat = 20
    static let lg: CGFloat = 26
    static let xl: CGFloat = 34
    static let xxl: CGFloat = 34
    static let full: CGFloat = 999
}

struct PardisMotion {
    static let fast: Double = 150
    static let normal: Double = 250
    static let slow: Double = 400
}

struct PardisTypography {
    // sizes in points (use .system(size: ...))
    static let xs: CGFloat = 11
    static let sm: CGFloat = 13
    static let base: CGFloat = 15
    static let lg: CGFloat = 18
    static let xl: CGFloat = 23
    static let xxl: CGFloat = 30
    static let xxxl: CGFloat = 34
    // weights
    static let weightNormal: Font.Weight = .regular
    static let weightMedium: Font.Weight = .medium
    static let weightSemiBold: Font.Weight = .bold
    static let weightBold: Font.Weight = .heavy
    // line heights (use .lineSpacing or modifier)
    static let lineHeightDisplay: CGFloat = 0.98
    static let lineHeightHeading: CGFloat = 1.08
    static let lineHeightTight: CGFloat = 1.35
    static let lineHeightNormal: CGFloat = 1.55
    static let lineHeightRelaxed: CGFloat = 1.7
}

struct PardisShadows {
    // Reference values for .shadow() or custom
    static let xsRadius: CGFloat = 1
    static let smRadius: CGFloat = 4
    static let mdRadius: CGFloat = 12
    static let lgRadius: CGFloat = 24
}

extension Color {
    /// A color that resolves to `light` or `dark` based on the active interface style, so the
    /// static `PardisColors` tokens flip with the color scheme. A manual override (the in-app
    /// dark-mode toggle) drives this via `.preferredColorScheme`, which sets the trait the
    /// dynamic `UIColor` reads. Call sites stay unchanged — `PardisColors.background` just resolves
    /// per scheme.
    static func dynamic(light: Color, dark: Color) -> Color {
        Color(UIColor { traits in
            traits.userInterfaceStyle == .dark ? UIColor(dark) : UIColor(light)
        })
    }

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
